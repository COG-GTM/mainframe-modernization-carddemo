"""Parse fixed-width ASCII files from app/data/ASCII/ and insert into PostgreSQL.

Field layouts are derived from the COBOL copybooks:
  - CVACT01Y.cpy  → accounts (RECLN 300)
  - CVACT03Y.cpy  → card_xrefs (RECLN 50)
  - CVTRA06Y.cpy  → daily_transactions (RECLN 350)
  - CVTRA01Y.cpy  → tran_cat_balances (RECLN 50)

COBOL zoned-decimal sign conventions used in ASCII data files:
  Positive last-digit overpunch: {=0 A=1 B=2 C=3 D=4 E=5 F=6 G=7 H=8 I=9
  Negative last-digit overpunch: }=0 J=1 K=2 L=3 M=4 N=5 O=6 P=7 Q=8 R=9
"""

import os
import sys
from decimal import Decimal
from pathlib import Path

# Allow running as: python -m scripts.seed_data  (from modernized/)
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from sqlalchemy.orm import Session

from app.database import SessionLocal, engine
from app.models import (  # noqa: F401  — ensures tables are registered
    Account,
    CardXref,
    DailyTransaction,
    TranCatBalance,
)
from app.database import Base

POSITIVE_OVERPUNCH = {
    "{": "0", "A": "1", "B": "2", "C": "3", "D": "4",
    "E": "5", "F": "6", "G": "7", "H": "8", "I": "9",
}
NEGATIVE_OVERPUNCH = {
    "}": "0", "J": "1", "K": "2", "L": "3", "M": "4",
    "N": "5", "O": "6", "P": "7", "Q": "8", "R": "9",
}


def parse_signed_decimal(raw: str, decimal_places: int = 2) -> Decimal:
    """Interpret a COBOL zoned-decimal field with implicit V99 and sign overpunch."""
    raw = raw.strip()
    if not raw:
        return Decimal(0)

    last_char = raw[-1]
    sign = 1
    if last_char in POSITIVE_OVERPUNCH:
        raw = raw[:-1] + POSITIVE_OVERPUNCH[last_char]
    elif last_char in NEGATIVE_OVERPUNCH:
        raw = raw[:-1] + NEGATIVE_OVERPUNCH[last_char]
        sign = -1

    raw = raw.lstrip("0") or "0"
    if decimal_places > 0:
        if len(raw) <= decimal_places:
            raw = raw.zfill(decimal_places + 1)
        integer_part = raw[:-decimal_places]
        decimal_part = raw[-decimal_places:]
        return Decimal(f"{sign * int(integer_part)}.{decimal_part}")
    return Decimal(sign * int(raw))


def parse_unsigned_int(raw: str) -> int:
    raw = raw.strip()
    if not raw:
        return 0
    return int(raw)


DATA_DIR = Path(__file__).resolve().parent.parent.parent / "app" / "data" / "ASCII"


def seed_accounts(db: Session) -> int:
    """Parse acctdata.txt according to CVACT01Y.cpy layout (RECLN 300)."""
    filepath = DATA_DIR / "acctdata.txt"
    if not filepath.exists():
        print(f"WARNING: {filepath} not found, skipping accounts.")
        return 0

    count = 0
    for line in filepath.read_text().splitlines():
        if len(line) < 122:
            continue
        acct = Account(
            acct_id=parse_unsigned_int(line[0:11]),
            active_status=line[11:12],
            curr_bal=parse_signed_decimal(line[12:24]),
            credit_limit=parse_signed_decimal(line[24:36]),
            cash_credit_limit=parse_signed_decimal(line[36:48]),
            open_date=line[48:58],
            expiration_date=line[58:68],
            reissue_date=line[68:78],
            curr_cyc_credit=parse_signed_decimal(line[78:90]),
            curr_cyc_debit=parse_signed_decimal(line[90:102]),
            addr_zip=line[102:112].strip(),
            group_id=line[112:122].strip(),
        )
        db.merge(acct)
        count += 1
    db.commit()
    print(f"Seeded {count} accounts.")
    return count


def seed_card_xrefs(db: Session) -> int:
    """Parse cardxref.txt according to CVACT03Y.cpy layout (RECLN 50)."""
    filepath = DATA_DIR / "cardxref.txt"
    if not filepath.exists():
        print(f"WARNING: {filepath} not found, skipping card_xrefs.")
        return 0

    count = 0
    for line in filepath.read_text().splitlines():
        if len(line) < 36:
            continue
        xref = CardXref(
            card_num=line[0:16].strip(),
            cust_id=parse_unsigned_int(line[16:25]),
            acct_id=parse_unsigned_int(line[25:36]),
        )
        db.merge(xref)
        count += 1
    db.commit()
    print(f"Seeded {count} card xrefs.")
    return count


def seed_daily_transactions(db: Session) -> int:
    """Parse dailytran.txt according to CVTRA06Y.cpy layout (RECLN 350)."""
    filepath = DATA_DIR / "dailytran.txt"
    if not filepath.exists():
        print(f"WARNING: {filepath} not found, skipping daily_transactions.")
        return 0

    count = 0
    for line in filepath.read_text().splitlines():
        if len(line) < 330:
            continue
        tran = DailyTransaction(
            tran_id=line[0:16].strip(),
            type_cd=line[16:18],
            cat_cd=parse_unsigned_int(line[18:22]),
            source=line[22:32].strip(),
            description=line[32:132].strip(),
            amount=parse_signed_decimal(line[132:143]),
            merchant_id=parse_unsigned_int(line[143:152]),
            merchant_name=line[152:202].strip(),
            merchant_city=line[202:252].strip(),
            merchant_zip=line[252:262].strip(),
            card_num=line[262:278].strip(),
            orig_ts=line[278:304].strip(),
            proc_ts=None,
            processed=False,
        )
        db.add(tran)
        count += 1
    db.commit()
    print(f"Seeded {count} daily transactions.")
    return count


def seed_tran_cat_balances(db: Session) -> int:
    """Parse tcatbal.txt according to CVTRA01Y.cpy layout (RECLN 50)."""
    filepath = DATA_DIR / "tcatbal.txt"
    if not filepath.exists():
        print(f"WARNING: {filepath} not found, skipping tran_cat_balances.")
        return 0

    count = 0
    for line in filepath.read_text().splitlines():
        if len(line) < 28:
            continue
        tcb = TranCatBalance(
            acct_id=parse_unsigned_int(line[0:11]),
            type_cd=line[11:13],
            cat_cd=parse_unsigned_int(line[13:17]),
            balance=parse_signed_decimal(line[17:28]),
        )
        db.merge(tcb)
        count += 1
    db.commit()
    print(f"Seeded {count} tran_cat_balances.")
    return count


def main() -> None:
    Base.metadata.create_all(bind=engine)
    db: Session = SessionLocal()
    try:
        seed_accounts(db)
        seed_card_xrefs(db)
        seed_daily_transactions(db)
        seed_tran_cat_balances(db)
    finally:
        db.close()
    print("Seed complete.")


if __name__ == "__main__":
    main()
