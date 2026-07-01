"""Parse the fixed-width COBOL flat file (acctdata.txt) and load into PostgreSQL.

Record layout mirrors CVACT01Y copybook (300-byte records):

    Offset  Length  Field                    PIC
    ------  ------  -----------------------  -----------
     0       11     ACCT-ID                  9(11)
    11        1     ACCT-ACTIVE-STATUS       X(01)
    12       12     ACCT-CURR-BAL            S9(10)V99
    24       12     ACCT-CREDIT-LIMIT        S9(10)V99
    36       12     ACCT-CASH-CREDIT-LIMIT   S9(10)V99
    48       10     ACCT-OPEN-DATE           X(10)
    58       10     ACCT-EXPIRAION-DATE      X(10)
    68       10     ACCT-REISSUE-DATE        X(10)
    78       12     ACCT-CURR-CYC-CREDIT     S9(10)V99
    90       12     ACCT-CURR-CYC-DEBIT      S9(10)V99
   102       10     ACCT-ADDR-ZIP            X(10)
   112       10     ACCT-GROUP-ID            X(10)
   122      178     FILLER                   X(178)
"""

from __future__ import annotations

import datetime
from pathlib import Path

from sqlalchemy.orm import Session

from cbact01c.models import AccountRecord, Base
from cbact01c.overpunch import decode_signed_overpunch

RECORD_LENGTH = 300

# (offset, length) tuples matching the copybook
_LAYOUT: list[tuple[str, int, int]] = [
    ("acct_id", 0, 11),
    ("acct_active_status", 11, 1),
    ("acct_curr_bal", 12, 12),
    ("acct_credit_limit", 24, 12),
    ("acct_cash_credit_limit", 36, 12),
    ("acct_open_date", 48, 10),
    ("acct_expiraion_date", 58, 10),
    ("acct_reissue_date", 68, 10),
    ("acct_curr_cyc_credit", 78, 12),
    ("acct_curr_cyc_debit", 90, 12),
    ("acct_addr_zip", 102, 10),
    ("acct_group_id", 112, 10),
]

_SIGNED_FIELDS = {
    "acct_curr_bal",
    "acct_credit_limit",
    "acct_cash_credit_limit",
    "acct_curr_cyc_credit",
    "acct_curr_cyc_debit",
}

_DATE_FIELDS = {
    "acct_open_date",
    "acct_expiraion_date",
    "acct_reissue_date",
}


def _parse_date(value: str) -> datetime.date | None:
    value = value.strip()
    if not value:
        return None
    return datetime.date.fromisoformat(value)


def parse_record(line: str) -> dict:
    """Parse a single 300-byte fixed-width record into a dict of typed values."""
    record = line.rstrip("\n\r")
    if len(record) < RECORD_LENGTH:
        record = record.ljust(RECORD_LENGTH)

    fields: dict = {}
    for name, offset, length in _LAYOUT:
        raw = record[offset : offset + length]

        if name == "acct_id":
            fields[name] = int(raw)
        elif name in _SIGNED_FIELDS:
            fields[name] = decode_signed_overpunch(raw, decimal_places=2)
        elif name in _DATE_FIELDS:
            fields[name] = _parse_date(raw)
        else:
            fields[name] = raw.strip()

    return fields


def load_file(path: Path | str, session: Session, *, truncate: bool = True) -> int:
    """Read *path*, parse each line, and insert into the ``account_record`` table.

    Parameters
    ----------
    path:
        Path to the fixed-width flat file.
    session:
        Active SQLAlchemy session.
    truncate:
        If ``True`` (default), delete existing rows before loading.

    Returns the number of records loaded.
    """
    path = Path(path)
    Base.metadata.create_all(session.get_bind())

    if truncate:
        session.query(AccountRecord).delete()

    count = 0
    with path.open("r", encoding="ascii", errors="replace") as fh:
        for line in fh:
            if not line.strip():
                continue
            fields = parse_record(line)
            session.add(AccountRecord(**fields))
            count += 1

    session.commit()
    return count
