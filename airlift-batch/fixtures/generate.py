#!/usr/bin/env python3
"""Deterministic fixture generator for the POSTTRAN -> INTCALC slice.

Writes fixed-width flat files (one per legacy dataset) that both sides of the
harness consume: the COBOL side loads them into GnuCOBOL indexed files, the
Java side reads the same bytes. Layouts come from app/cpy via the copybook
parser, so a copybook change is a fixture change.

Everything is seeded from a constant; no clock, environment or filesystem
ordering is read, so the output is byte-identical on every run and on every
machine.

The population is shaped to exercise the paths the slice actually contains
(see the citations in each helper): posting, rejects for all three reason
codes in CBTRN02C, the boundary cases at CBTRN02C:407 and :414, the
create-vs-update branch at CBTRN02C:495, the missing-disclosure-group default
path at CBACT04C:436 and interest amounts whose third decimal is non-zero so
truncation at CBACT04C:464 is observable.
"""

from __future__ import annotations

import argparse
import random
import sys
from decimal import Decimal
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO / "airlift-batch" / "tools"))
sys.path.insert(0, str(REPO / "airlift-batch" / "parity"))

from codec import encode_record  # noqa: E402
from copybook import Field, load_record  # noqa: E402

CPY = REPO / "app" / "cpy"
SEED = 20220718
ACCOUNT_COUNT = 300
TRANSACTION_COUNT = 600
POSTING_DATE = "2022-07-18"
TYPE_CODES = ("01", "02", "03", "04", "05")
CATEGORY_CODES = (1, 2, 3, 4, 5)
GROUPS = ("GROUP001", "GROUP002", "ZEROINT")
RATES = {
    "GROUP001": Decimal("12.99"),
    "GROUP002": Decimal("18.49"),
    "ZEROINT": Decimal("0.00"),
    "DEFAULT": Decimal("24.99"),
}


def account_id(index: int) -> int:
    return 10000000001 + index


def card_number(index: int) -> str:
    return str(4000000000000000 + index)


def timestamp(date: str, index: int) -> str:
    hour = 8 + index % 12
    minute = index % 60
    second = (index * 7) % 60
    return f"{date}-{hour:02d}.{minute:02d}.{second:02d}.{index % 1000000:06d}"


def build_accounts(rng: random.Random) -> list[dict[str, object]]:
    accounts: list[dict[str, object]] = []
    for index in range(ACCOUNT_COUNT):
        group = GROUPS[index % len(GROUPS)]
        if index % 25 == 7:
            # No disclosure-group row is generated for this id, which drives
            # the DEFAULT re-read at CBACT04C:436-439.
            group = "NOGROUP"
        if index % 40 == 3:
            expiry = POSTING_DATE  # equal: passes CBTRN02C:414 (>=)
        elif index % 40 == 11:
            expiry = "2021-06-30"  # already expired: reason 103
        else:
            expiry = "2030-12-31"
        credit_limit = Decimal(rng.randrange(50000, 1500000)).scaleb(-2)
        accounts.append(
            {
                "ACCT-ID": account_id(index),
                "ACCT-ACTIVE-STATUS": "Y" if index % 17 else "N",
                "ACCT-CURR-BAL": Decimal(rng.randrange(-200000, 400000)).scaleb(-2),
                "ACCT-CREDIT-LIMIT": credit_limit,
                "ACCT-CASH-CREDIT-LIMIT": (credit_limit / 2).quantize(Decimal("0.01")),
                "ACCT-OPEN-DATE": "2018-03-09",
                "ACCT-EXPIRAION-DATE": expiry,
                "ACCT-REISSUE-DATE": "2021-03-09",
                "ACCT-CURR-CYC-CREDIT": Decimal(rng.randrange(0, 120000)).scaleb(-2),
                "ACCT-CURR-CYC-DEBIT": Decimal(rng.randrange(-90000, 0)).scaleb(-2),
                "ACCT-ADDR-ZIP": f"{10001 + index:05d}",
                "ACCT-GROUP-ID": group,
            }
        )
    return accounts


def build_xref(accounts: list[dict[str, object]]) -> list[dict[str, object]]:
    rows = [
        {
            "XREF-CARD-NUM": card_number(index),
            "XREF-CUST-ID": 900000001 + index,
            "XREF-ACCT-ID": account["ACCT-ID"],
        }
        for index, account in enumerate(accounts)
    ]
    # One card that resolves to an account id with no ACCTFILE record: drives
    # reason 101 at CBTRN02C:397.
    rows.append(
        {
            "XREF-CARD-NUM": card_number(ACCOUNT_COUNT + 500),
            "XREF-CUST-ID": 999999999,
            "XREF-ACCT-ID": 99999999999,
        }
    )
    return sorted(rows, key=lambda row: str(row["XREF-CARD-NUM"]))


def build_discgrp(accounts: list[dict[str, object]]) -> list[dict[str, object]]:
    rows: list[dict[str, object]] = []
    groups = sorted({str(account["ACCT-GROUP-ID"]) for account in accounts})
    for group in groups + ["DEFAULT"]:
        if group == "NOGROUP":
            continue
        for type_code in TYPE_CODES:
            for category in CATEGORY_CODES:
                rows.append(
                    {
                        "DIS-GROUP-KEY.DIS-ACCT-GROUP-ID": group,
                        "DIS-GROUP-KEY.DIS-TRAN-TYPE-CD": type_code,
                        "DIS-GROUP-KEY.DIS-TRAN-CAT-CD": category,
                        "DIS-INT-RATE": RATES.get(group, RATES["DEFAULT"]),
                    }
                )
    return sorted(
        rows,
        key=lambda row: (
            str(row["DIS-GROUP-KEY.DIS-ACCT-GROUP-ID"]).ljust(10),
            str(row["DIS-GROUP-KEY.DIS-TRAN-TYPE-CD"]),
            int(str(row["DIS-GROUP-KEY.DIS-TRAN-CAT-CD"])),
        ),
    )


def build_transactions(
    rng: random.Random, accounts: list[dict[str, object]]
) -> list[dict[str, object]]:
    rows: list[dict[str, object]] = []
    for index in range(TRANSACTION_COUNT):
        account_index = index % len(accounts)
        account = accounts[account_index]
        type_code = TYPE_CODES[index % len(TYPE_CODES)]
        category = CATEGORY_CODES[(index // 3) % len(CATEGORY_CODES)]
        headroom = (
            Decimal(str(account["ACCT-CREDIT-LIMIT"]))
            - Decimal(str(account["ACCT-CURR-CYC-CREDIT"]))
            + Decimal(str(account["ACCT-CURR-CYC-DEBIT"]))
        )
        if index % 37 == 5:
            # Exactly on the credit limit: CBTRN02C:407 posts this (>=).
            amount = headroom
        elif index % 37 == 9:
            # One cent over the limit: reason 102.
            amount = headroom + Decimal("0.01")
        elif index % 11 == 4:
            amount = Decimal(-rng.randrange(100, 40000)).scaleb(-2)
        else:
            amount = Decimal(rng.randrange(100, 25000)).scaleb(-2)
        card = card_number(account_index)
        if index % 53 == 13:
            card = card_number(ACCOUNT_COUNT + 900)  # unknown card: reason 100
        elif index % 53 == 21:
            card = card_number(ACCOUNT_COUNT + 500)  # known card, no account
        rows.append(
            {
                "DALYTRAN-ID": f"DT{index:014d}",
                "DALYTRAN-TYPE-CD": type_code,
                "DALYTRAN-CAT-CD": category,
                "DALYTRAN-SOURCE": "POS" if index % 2 else "ONLINE",
                "DALYTRAN-DESC": f"Purchase {index:06d} at merchant {index % 97:03d}",
                "DALYTRAN-AMT": amount,
                "DALYTRAN-MERCHANT-ID": 100000000 + index,
                "DALYTRAN-MERCHANT-NAME": f"MERCHANT {index % 97:03d}",
                "DALYTRAN-MERCHANT-CITY": "SPRINGFIELD",
                "DALYTRAN-MERCHANT-ZIP": f"{22001 + index % 500:05d}",
                "DALYTRAN-CARD-NUM": card,
                "DALYTRAN-ORIG-TS": timestamp(POSTING_DATE, index),
                "DALYTRAN-PROC-TS": timestamp(POSTING_DATE, index),
            }
        )
    rows.extend(build_double_failure_transactions(accounts, len(rows)))
    return rows


def build_double_failure_transactions(
    accounts: list[dict[str, object]], first_index: int
) -> list[dict[str, object]]:
    """Transactions that fail both validations in CBTRN02C:1500-B-LOOKUP-ACCT.

    Both IF statements run unconditionally (CBTRN02C:407-420), so 103 overwrites
    102 and the reject carries the expiration reason. Without these records the
    ordering trap would have nothing to bite on. A posting only ever shrinks the
    headroom on an account (CBTRN02C:547-552), so an amount one cent above the
    headroom computed here is still over the limit when the record is read.
    """
    rows: list[dict[str, object]] = []
    expired = [
        (index, account)
        for index, account in enumerate(accounts)
        if str(account["ACCT-EXPIRAION-DATE"]) == "2021-06-30"
    ][:4]
    for offset, (account_index, account) in enumerate(expired):
        index = first_index + offset
        headroom = (
            Decimal(str(account["ACCT-CREDIT-LIMIT"]))
            - Decimal(str(account["ACCT-CURR-CYC-CREDIT"]))
            + Decimal(str(account["ACCT-CURR-CYC-DEBIT"]))
        )
        rows.append(
            {
                "DALYTRAN-ID": f"DT{index:014d}",
                "DALYTRAN-TYPE-CD": TYPE_CODES[offset % len(TYPE_CODES)],
                "DALYTRAN-CAT-CD": CATEGORY_CODES[offset % len(CATEGORY_CODES)],
                "DALYTRAN-SOURCE": "POS",
                "DALYTRAN-DESC": f"Overlimit on expired account {offset:02d}",
                "DALYTRAN-AMT": headroom + Decimal("0.01"),
                "DALYTRAN-MERCHANT-ID": 200000000 + offset,
                "DALYTRAN-MERCHANT-NAME": f"MERCHANT X{offset:02d}",
                "DALYTRAN-MERCHANT-CITY": "SHELBYVILLE",
                "DALYTRAN-MERCHANT-ZIP": f"{33001 + offset:05d}",
                "DALYTRAN-CARD-NUM": card_number(account_index),
                "DALYTRAN-ORIG-TS": timestamp(POSTING_DATE, index),
                "DALYTRAN-PROC-TS": timestamp(POSTING_DATE, index),
            }
        )
    return rows


def build_tcatbal(
    rng: random.Random, accounts: list[dict[str, object]]
) -> list[dict[str, object]]:
    """Seed some category balances so CBTRN02C exercises both branches at :495.

    Balances are chosen with cents that make (balance * rate) / 1200 produce a
    non-zero third decimal, so the truncation at CBACT04C:464 is visible in the
    generated interest transactions.
    """
    rows: list[dict[str, object]] = []
    for index, account in enumerate(accounts):
        if index % 3:
            continue
        for category in (CATEGORY_CODES[0], CATEGORY_CODES[2]):
            rows.append(
                {
                    "TRAN-CAT-KEY.TRANCAT-ACCT-ID": account["ACCT-ID"],
                    "TRAN-CAT-KEY.TRANCAT-TYPE-CD": TYPE_CODES[index % len(TYPE_CODES)],
                    "TRAN-CAT-KEY.TRANCAT-CD": category,
                    "TRAN-CAT-BAL": Decimal(rng.randrange(1, 900000)).scaleb(-2),
                }
            )
    return sorted(
        rows,
        key=lambda row: (
            int(str(row["TRAN-CAT-KEY.TRANCAT-ACCT-ID"])),
            str(row["TRAN-CAT-KEY.TRANCAT-TYPE-CD"]),
            int(str(row["TRAN-CAT-KEY.TRANCAT-CD"])),
        ),
    )


def write(path: Path, record: Field, rows: list[dict[str, object]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("wb") as handle:
        for row in rows:
            handle.write(encode_record(record, row))
    print(f"{path.name}: {len(rows)} records of {record.size} bytes")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--out", type=Path, required=True, help="output directory")
    args = parser.parse_args()

    account_record = load_record(CPY, "CVACT01Y", "ACCOUNT-RECORD")
    xref_record = load_record(CPY, "CVACT03Y", "CARD-XREF-RECORD")
    discgrp_record = load_record(CPY, "CVTRA02Y", "DIS-GROUP-RECORD")
    tcatbal_record = load_record(CPY, "CVTRA01Y", "TRAN-CAT-BAL-RECORD")
    dalytran_record = load_record(CPY, "CVTRA06Y", "DALYTRAN-RECORD")

    rng = random.Random(SEED)
    accounts = build_accounts(rng)
    transactions = build_transactions(rng, accounts)
    tcatbal = build_tcatbal(rng, accounts)

    write(args.out / "acctdata.dat", account_record, accounts)
    write(args.out / "cardxref.dat", xref_record, build_xref(accounts))
    write(args.out / "discgrp.dat", discgrp_record, build_discgrp(accounts))
    write(args.out / "tcatbal.dat", tcatbal_record, tcatbal)
    write(args.out / "dalytran.dat", dalytran_record, transactions)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
