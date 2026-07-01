"""Fixed-width parsing for the CVACT01Y account record layout.

The CardDemo sample data (``app/data/ASCII/acctdata.txt`` and the EBCDIC
equivalents) stores numeric fields as COBOL *zoned decimal* (``PIC S9(n)V99``
DISPLAY) records. Each such field is a run of digit characters whose final byte
carries an "overpunch" that encodes both the last digit and the sign.

This module knows the 300-byte layout and turns a raw record into an
:class:`~cbact01c.account.Account`.
"""

from __future__ import annotations

from decimal import Decimal

from cbact01c.account import Account

RECORD_LENGTH = 300

# Overpunch tables for zoned-decimal sign encoding.
_POSITIVE = "{ABCDEFGHI"  # {->0, A->1, ... I->9  (positive)
_NEGATIVE = "}JKLMNOPQR"  # }->0, J->1, ... R->9  (negative)


def decode_zoned_decimal(raw: str, decimals: int = 2) -> Decimal:
    """Decode a COBOL zoned-decimal (signed DISPLAY) field into a Decimal.

    ``raw`` is the full field including the trailing overpunched byte.
    ``decimals`` is the number of implied decimal places (V99 -> 2).
    """
    field = raw.strip()
    if not field:
        return Decimal(0).scaleb(-decimals)  # e.g. Decimal('0.00') for V99

    last = field[-1]
    negative = False
    if last in _POSITIVE:
        last_digit = _POSITIVE.index(last)
    elif last in _NEGATIVE:
        last_digit = _NEGATIVE.index(last)
        negative = True
    elif last.isdigit():
        last_digit = int(last)
    else:
        raise ValueError(f"invalid zoned-decimal byte {last!r} in {raw!r}")

    digits = field[:-1] + str(last_digit)
    if not digits.isdigit():
        raise ValueError(f"non-numeric zoned-decimal field {raw!r}")

    value = Decimal(digits).scaleb(-decimals)
    return -value if negative else value


def parse_account_record(record: str) -> Account:
    """Parse one fixed-width 300-byte account record into an ``Account``.

    Trailing FILLER (bytes 122-300) is ignored, matching the migration spec.
    ``ACCT-ADDR-ZIP`` and ``ACCT-GROUP-ID`` keep their raw (right-stripped)
    string values, matching COBOL's ``PIC X`` behaviour.
    """
    if len(record) < 122:
        raise ValueError(
            f"account record too short: got {len(record)} bytes, need >= 122"
        )

    def text(start: int, width: int) -> str:
        return record[start : start + width].rstrip()

    def money(start: int) -> Decimal:
        return decode_zoned_decimal(record[start : start + 12], decimals=2)

    return Account(
        acct_id=int(record[0:11]),
        acct_active_status=record[11:12],
        acct_curr_bal=money(12),
        acct_credit_limit=money(24),
        acct_cash_credit_limit=money(36),
        acct_open_date=text(48, 10),
        acct_expiraion_date=text(58, 10),
        acct_reissue_date=text(68, 10),
        acct_curr_cyc_credit=money(78),
        acct_curr_cyc_debit=money(90),
        acct_addr_zip=text(102, 10),
        acct_group_id=text(112, 10),
    )
