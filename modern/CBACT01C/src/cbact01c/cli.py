"""Batch CLI entry-point — modern equivalent of CBACT01C COBOL program.

Functional parity:
  1. Open the account data source (PostgreSQL table *or* flat file).
  2. Read records sequentially (ordered by ACCT-ID, matching VSAM KSDS order).
  3. Display each record's fields in the same format as the COBOL DISPLAY verbs.
  4. Print start/end banners and abort on errors (exit-code 999).

Usage::

    # Load flat file into PostgreSQL, then display:
    cbact01c load   --file acctdata.txt [--dsn postgresql://…]
    cbact01c run    [--dsn postgresql://…]

    # One-shot: load + display (mirrors the full JCL job):
    cbact01c job    --file acctdata.txt [--dsn postgresql://…]
"""

from __future__ import annotations

import logging
import sys

import click
from sqlalchemy import select

from cbact01c.db import get_engine
from cbact01c.loader import load_file
from cbact01c.models import AccountRecord, Base

logger = logging.getLogger("cbact01c")

_ABEND_CODE = 999


def _display_record(rec: AccountRecord) -> list[str]:
    """Format one account record the same way the COBOL 1100-DISPLAY-ACCT-RECORD does."""
    lines = [
        f"ACCT-ID                 :{rec.acct_id:011d}",
        f"ACCT-ACTIVE-STATUS      :{rec.acct_active_status}",
        f"ACCT-CURR-BAL           :{rec.acct_curr_bal}",
        f"ACCT-CREDIT-LIMIT       :{rec.acct_credit_limit}",
        f"ACCT-CASH-CREDIT-LIMIT  :{rec.acct_cash_credit_limit}",
        f"ACCT-OPEN-DATE          :{rec.acct_open_date}",
        f"ACCT-EXPIRAION-DATE     :{rec.acct_expiraion_date}",
        f"ACCT-REISSUE-DATE       :{rec.acct_reissue_date}",
        f"ACCT-CURR-CYC-CREDIT    :{rec.acct_curr_cyc_credit}",
        f"ACCT-CURR-CYC-DEBIT     :{rec.acct_curr_cyc_debit}",
        f"ACCT-GROUP-ID           :{rec.acct_group_id}",
        "-------------------------------------------------",
    ]
    return lines


def run_batch(dsn: str | None, output: list[str] | None = None) -> int:
    """Read all accounts from PostgreSQL and display them.

    Mirrors COBOL PROCEDURE DIVISION: open → sequential read → display → close.

    Parameters
    ----------
    dsn:
        Database connection string (falls back to ``DATABASE_URL`` env var).
    output:
        If provided, append display lines here instead of printing to stdout.
        Useful for testing / audit capture.

    Returns
    -------
    0 on success, 999 on error (matching COBOL ABEND code).
    """
    sink = output if output is not None else None

    def _emit(line: str) -> None:
        if sink is not None:
            sink.append(line)
        else:
            print(line)

    _emit("START OF EXECUTION OF PROGRAM CBACT01C")

    try:
        engine = get_engine(dsn)
        Base.metadata.create_all(engine)
    except Exception as exc:
        _emit("ERROR OPENING ACCTFILE")
        _emit(f"FILE STATUS IS: {exc}")
        _emit("ABENDING PROGRAM")
        return _ABEND_CODE

    from sqlalchemy.orm import Session

    with Session(engine) as session:
        try:
            stmt = select(AccountRecord).order_by(AccountRecord.acct_id)
            results = session.execute(stmt).scalars().all()
        except Exception as exc:
            _emit("ERROR READING ACCOUNT FILE")
            _emit(f"FILE STATUS IS: {exc}")
            _emit("ABENDING PROGRAM")
            return _ABEND_CODE

        for rec in results:
            for line in _display_record(rec):
                _emit(line)

    _emit("END OF EXECUTION OF PROGRAM CBACT01C")
    return 0


# ---------------------------------------------------------------------------
# Click CLI
# ---------------------------------------------------------------------------

@click.group()
def main() -> None:
    """CBACT01C — read and print account data (modern Python equivalent)."""
    logging.basicConfig(level=logging.INFO, format="%(message)s")


@main.command()
@click.option("--file", "filepath", required=True, help="Path to acctdata.txt flat file.")
@click.option("--dsn", default=None, help="PostgreSQL DSN (default: $DATABASE_URL).")
def load(filepath: str, dsn: str | None) -> None:
    """Load account flat-file data into PostgreSQL."""
    from sqlalchemy.orm import Session as SASession

    engine = get_engine(dsn)
    with SASession(engine) as session:
        count = load_file(filepath, session)
    click.echo(f"Loaded {count} account records.")


@main.command()
@click.option("--dsn", default=None, help="PostgreSQL DSN (default: $DATABASE_URL).")
def run(dsn: str | None) -> None:
    """Read and display all account records (equivalent to running CBACT01C)."""
    rc = run_batch(dsn)
    sys.exit(rc)


@main.command()
@click.option("--file", "filepath", required=True, help="Path to acctdata.txt flat file.")
@click.option("--dsn", default=None, help="PostgreSQL DSN (default: $DATABASE_URL).")
def job(filepath: str, dsn: str | None) -> None:
    """Load flat file then display — mirrors the full READACCT JCL job."""
    from sqlalchemy.orm import Session as SASession

    engine = get_engine(dsn)
    with SASession(engine) as session:
        count = load_file(filepath, session)
    click.echo(f"Loaded {count} account records.")
    rc = run_batch(dsn)
    sys.exit(rc)


if __name__ == "__main__":
    main()
