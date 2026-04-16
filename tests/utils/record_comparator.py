"""Field-by-field comparison utilities for fixed-length records.

These helpers let tests compare two record strings and report *which*
fields differ rather than just failing with a raw string mismatch.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Sequence


@dataclass(frozen=True)
class FieldSpec:
    """Describes one field within a fixed-length record."""

    name: str
    offset: int
    length: int


@dataclass(frozen=True)
class FieldDiff:
    """A single field-level difference between two records."""

    field_name: str
    expected: str
    actual: str


# ---------------------------------------------------------------------------
# Canonical field layouts derived from COBOL copybooks
# ---------------------------------------------------------------------------

ACCOUNT_FIELDS: list[FieldSpec] = [
    FieldSpec("ACCT-ID", 0, 11),
    FieldSpec("ACCT-ACTIVE-STATUS", 11, 1),
    FieldSpec("ACCT-CURR-BAL", 12, 13),
    FieldSpec("ACCT-CREDIT-LIMIT", 25, 13),
    FieldSpec("ACCT-CASH-CREDIT-LIMIT", 38, 13),
    FieldSpec("ACCT-OPEN-DATE", 51, 10),
    FieldSpec("ACCT-EXPIRAION-DATE", 61, 10),
    FieldSpec("ACCT-REISSUE-DATE", 71, 10),
    FieldSpec("ACCT-CURR-CYC-CREDIT", 81, 13),
    FieldSpec("ACCT-CURR-CYC-DEBIT", 94, 13),
    FieldSpec("ACCT-ADDR-ZIP", 107, 10),
    FieldSpec("ACCT-GROUP-ID", 117, 10),
]

CARD_FIELDS: list[FieldSpec] = [
    FieldSpec("CARD-NUM", 0, 16),
    FieldSpec("CARD-ACCT-ID", 16, 11),
    FieldSpec("CARD-CVV-CD", 27, 3),
    FieldSpec("CARD-EMBOSSED-NAME", 30, 50),
    FieldSpec("CARD-EXPIRAION-DATE", 80, 10),
    FieldSpec("CARD-ACTIVE-STATUS", 90, 1),
]

CARD_XREF_FIELDS: list[FieldSpec] = [
    FieldSpec("XREF-CARD-NUM", 0, 16),
    FieldSpec("XREF-CUST-ID", 16, 9),
    FieldSpec("XREF-ACCT-ID", 25, 11),
]

CUSTOMER_FIELDS: list[FieldSpec] = [
    FieldSpec("CUST-ID", 0, 9),
    FieldSpec("CUST-FIRST-NAME", 9, 25),
    FieldSpec("CUST-MIDDLE-NAME", 34, 25),
    FieldSpec("CUST-LAST-NAME", 59, 25),
    FieldSpec("CUST-ADDR-LINE-1", 84, 50),
    FieldSpec("CUST-ADDR-LINE-2", 134, 50),
    FieldSpec("CUST-ADDR-LINE-3", 184, 50),
    FieldSpec("CUST-ADDR-STATE-CD", 234, 2),
    FieldSpec("CUST-ADDR-COUNTRY-CD", 236, 3),
    FieldSpec("CUST-ADDR-ZIP", 239, 10),
    FieldSpec("CUST-PHONE-NUM-1", 249, 15),
    FieldSpec("CUST-PHONE-NUM-2", 264, 15),
    FieldSpec("CUST-SSN", 279, 9),
    FieldSpec("CUST-GOVT-ISSUED-ID", 288, 20),
    FieldSpec("CUST-DOB-YYYY-MM-DD", 308, 10),
    FieldSpec("CUST-EFT-ACCOUNT-ID", 318, 10),
    FieldSpec("CUST-PRI-CARD-HOLDER-IND", 328, 1),
    FieldSpec("CUST-FICO-CREDIT-SCORE", 329, 3),
]

TRANSACTION_FIELDS: list[FieldSpec] = [
    FieldSpec("TRAN-ID", 0, 16),
    FieldSpec("TRAN-TYPE-CD", 16, 2),
    FieldSpec("TRAN-CAT-CD", 18, 4),
    FieldSpec("TRAN-SOURCE", 22, 10),
    FieldSpec("TRAN-DESC", 32, 100),
    FieldSpec("TRAN-AMT", 132, 12),
    FieldSpec("TRAN-MERCHANT-ID", 144, 9),
    FieldSpec("TRAN-MERCHANT-NAME", 153, 50),
    FieldSpec("TRAN-MERCHANT-CITY", 203, 50),
    FieldSpec("TRAN-MERCHANT-ZIP", 253, 10),
    FieldSpec("TRAN-CARD-NUM", 263, 16),
    FieldSpec("TRAN-ORIG-TS", 279, 26),
    FieldSpec("TRAN-PROC-TS", 305, 26),
]

DAILY_TRANSACTION_FIELDS: list[FieldSpec] = [
    FieldSpec("DALYTRAN-ID", 0, 16),
    FieldSpec("DALYTRAN-TYPE-CD", 16, 2),
    FieldSpec("DALYTRAN-CAT-CD", 18, 4),
    FieldSpec("DALYTRAN-SOURCE", 22, 10),
    FieldSpec("DALYTRAN-DESC", 32, 100),
    FieldSpec("DALYTRAN-AMT", 132, 12),
    FieldSpec("DALYTRAN-MERCHANT-ID", 144, 9),
    FieldSpec("DALYTRAN-MERCHANT-NAME", 153, 50),
    FieldSpec("DALYTRAN-MERCHANT-CITY", 203, 50),
    FieldSpec("DALYTRAN-MERCHANT-ZIP", 253, 10),
    FieldSpec("DALYTRAN-CARD-NUM", 263, 16),
    FieldSpec("DALYTRAN-ORIG-TS", 279, 26),
    FieldSpec("DALYTRAN-PROC-TS", 305, 26),
]

TRAN_TYPE_FIELDS: list[FieldSpec] = [
    FieldSpec("TRAN-TYPE", 0, 2),
    FieldSpec("TRAN-TYPE-DESC", 2, 50),
]

TRAN_CAT_FIELDS: list[FieldSpec] = [
    FieldSpec("TRAN-TYPE-CD", 0, 2),
    FieldSpec("TRAN-CAT-CD", 2, 4),
    FieldSpec("TRAN-CAT-TYPE-DESC", 6, 50),
]

TRAN_CAT_BALANCE_FIELDS: list[FieldSpec] = [
    FieldSpec("TRANCAT-ACCT-ID", 0, 11),
    FieldSpec("TRANCAT-TYPE-CD", 11, 2),
    FieldSpec("TRANCAT-CD", 13, 4),
    FieldSpec("TRAN-CAT-BAL", 17, 12),
]

DISCLOSURE_GROUP_FIELDS: list[FieldSpec] = [
    FieldSpec("DIS-ACCT-GROUP-ID", 0, 10),
    FieldSpec("DIS-TRAN-TYPE-CD", 10, 2),
    FieldSpec("DIS-TRAN-CAT-CD", 12, 4),
    FieldSpec("DIS-INT-RATE", 16, 7),
]

USER_SECURITY_FIELDS: list[FieldSpec] = [
    FieldSpec("SEC-USR-ID", 0, 8),
    FieldSpec("SEC-USR-FNAME", 8, 20),
    FieldSpec("SEC-USR-LNAME", 28, 20),
    FieldSpec("SEC-USR-PWD", 48, 8),
    FieldSpec("SEC-USR-TYPE", 56, 1),
]


# ---------------------------------------------------------------------------
# Comparison functions
# ---------------------------------------------------------------------------

def extract_field(record: str, spec: FieldSpec) -> str:
    """Extract a single field value from a record string."""
    return record[spec.offset : spec.offset + spec.length]


def compare_records(
    expected: str,
    actual: str,
    fields: Sequence[FieldSpec],
) -> list[FieldDiff]:
    """Compare two record strings field-by-field.

    Returns a (possibly empty) list of :class:`FieldDiff` for every
    field that does not match.
    """
    diffs: list[FieldDiff] = []
    for spec in fields:
        exp_val = extract_field(expected, spec)
        act_val = extract_field(actual, spec)
        if exp_val != act_val:
            diffs.append(FieldDiff(field_name=spec.name, expected=exp_val, actual=act_val))
    return diffs


def assert_records_equal(
    expected: str,
    actual: str,
    fields: Sequence[FieldSpec],
    *,
    msg: str = "",
) -> None:
    """Assert two records match on every declared field.

    Raises ``AssertionError`` with a human-readable diff summary if any
    fields disagree.
    """
    diffs = compare_records(expected, actual, fields)
    if diffs:
        lines = [msg or "Record mismatch:"]
        for d in diffs:
            lines.append(f"  {d.field_name}: expected={d.expected!r}  actual={d.actual!r}")
        raise AssertionError("\n".join(lines))
