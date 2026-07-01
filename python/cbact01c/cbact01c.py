"""Python port of batch program CBACT01C.

Reproduces the COBOL procedure flow of ``app/cbl/CBACT01C.cbl``:

    * log  "START OF EXECUTION OF PROGRAM CBACT01C"
    * open the account master (here: a DB session)
    * read every account sequentially in ACCT-ID ascending order
    * print each record's labelled fields (paragraph 1100-DISPLAY-ACCT-RECORD)
    * log  "END OF EXECUTION OF PROGRAM CBACT01C"

Error handling mirrors the COBOL file-status logic: normal cursor exhaustion is
end-of-file (a clean exit), while any unexpected DB/read error is reported like
paragraph 9910-DISPLAY-IO-STATUS and the program exits non-zero to mirror the
9999-ABEND-PROGRAM / CEE3ABD abend.

This replaces the JCL job ``app/jcl/READACCT.jcl``.
"""

from __future__ import annotations

import sys
from typing import Callable, Iterable, Iterator

from sqlalchemy import select
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import Session

from cbact01c.account import Account
from cbact01c.db import create_db_engine, create_session_factory

PROGRAM_ID = "CBACT01C"

# Non-zero return code used to mirror the COBOL abend (9999-ABEND-PROGRAM).
ABEND_RETURN_CODE = 12

# Ordered (label, accessor) pairs, matching paragraph 1100-DISPLAY-ACCT-RECORD
# exactly: same labels, same order. Each label is 25 characters wide (COBOL
# concatenates the literal and the value with no separator).
_MONEY = lambda v: f"{v:.2f}"  # noqa: E731 - render Decimal with 2 dp
_FIELDS: list[tuple[str, Callable[[Account], str]]] = [
    ("ACCT-ID                 :", lambda a: f"{a.acct_id:011d}"),
    ("ACCT-ACTIVE-STATUS      :", lambda a: a.acct_active_status),
    ("ACCT-CURR-BAL           :", lambda a: _MONEY(a.acct_curr_bal)),
    ("ACCT-CREDIT-LIMIT       :", lambda a: _MONEY(a.acct_credit_limit)),
    ("ACCT-CASH-CREDIT-LIMIT  :", lambda a: _MONEY(a.acct_cash_credit_limit)),
    ("ACCT-OPEN-DATE          :", lambda a: a.acct_open_date),
    ("ACCT-EXPIRAION-DATE     :", lambda a: a.acct_expiraion_date),
    ("ACCT-REISSUE-DATE       :", lambda a: a.acct_reissue_date),
    ("ACCT-CURR-CYC-CREDIT    :", lambda a: _MONEY(a.acct_curr_cyc_credit)),
    ("ACCT-CURR-CYC-DEBIT     :", lambda a: _MONEY(a.acct_curr_cyc_debit)),
    ("ACCT-GROUP-ID           :", lambda a: a.acct_group_id),
]

SEPARATOR = "-" * 49


def format_account_record(account: Account) -> list[str]:
    """Return the labelled output lines for one account (paragraph 1100)."""
    lines = [f"{label}{accessor(account)}" for label, accessor in _FIELDS]
    lines.append(SEPARATOR)
    return lines


def display_account_record(account: Account, out: Callable[[str], None]) -> None:
    """Print one account's labelled fields, mirroring 1100-DISPLAY-ACCT-RECORD."""
    for line in format_account_record(account):
        out(line)


def iter_accounts(session: Session) -> Iterator[Account]:
    """Yield every account ordered by ACCT-ID ascending, streaming the cursor.

    Uses ``yield_per`` so records are fetched incrementally, matching the COBOL
    sequential READ loop rather than loading the whole file into memory.
    """
    stmt = select(Account).order_by(Account.acct_id.asc())
    yield from session.scalars(stmt).yield_per(100)


def run_batch(
    accounts: Iterable[Account],
    out: Callable[[str], None] = print,
    err: Callable[[str], None] = lambda m: print(m, file=sys.stderr),
) -> int:
    """Run the CBACT01C procedure over an iterable of accounts.

    Returns 0 on success. On an unexpected read error, reports the failure like
    9910-DISPLAY-IO-STATUS / 9999-ABEND-PROGRAM and returns a non-zero code.
    """
    out(f"START OF EXECUTION OF PROGRAM {PROGRAM_ID}")
    try:
        # Normal exhaustion of the iterator is end-of-file: a clean exit.
        for account in accounts:
            display_account_record(account, out)
    except SQLAlchemyError as exc:
        err("ERROR READING ACCOUNT FILE")
        _display_io_status(exc, err)
        _abend(err)
        return ABEND_RETURN_CODE
    out(f"END OF EXECUTION OF PROGRAM {PROGRAM_ID}")
    return 0


def _display_io_status(exc: Exception, err: Callable[[str], None]) -> None:
    """Report an I/O failure, mirroring paragraph 9910-DISPLAY-IO-STATUS."""
    err(f"FILE STATUS IS: {exc}")


def _abend(err: Callable[[str], None]) -> None:
    """Emit the abend banner, mirroring paragraph 9999-ABEND-PROGRAM."""
    err("ABENDING PROGRAM")


def main(argv: list[str] | None = None) -> int:
    """CLI entry point. Opens a DB session and runs the batch."""
    engine = create_db_engine()
    session_factory = create_session_factory(engine)
    try:
        with session_factory() as session:
            return run_batch(iter_accounts(session))
    except SQLAlchemyError as exc:
        # Failure opening the "file" (establishing the session/connection).
        print("ERROR OPENING ACCTFILE", file=sys.stderr)
        _display_io_status(exc, lambda m: print(m, file=sys.stderr))
        _abend(lambda m: print(m, file=sys.stderr))
        return ABEND_RETURN_CODE


if __name__ == "__main__":
    raise SystemExit(main())
