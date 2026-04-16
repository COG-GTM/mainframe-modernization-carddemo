"""Shared fixtures for transaction parser tests."""

from decimal import Decimal

import pytest

from oe129bc.models.transaction import RECORD_LENGTH, TransactionRecord


@pytest.fixture
def valid_record() -> TransactionRecord:
    """A fully-valid TransactionRecord with realistic data."""
    return TransactionRecord(
        tran_id="0000000000000001",
        tran_type_cd="01",
        tran_cat_cd="5411",
        tran_source="ONLINE",
        tran_desc="GROCERY STORE PURCHASE",
        tran_amt=Decimal("1234.56"),
        tran_merchant_id="123456789",
        tran_merchant_name="WHOLE FOODS MARKET",
        tran_merchant_city="ARLINGTON",
        tran_merchant_zip="22201",
        tran_card_num="4111111111111111",
        tran_orig_ts="2024-01-15 10:30:00.000000",
        tran_proc_ts="2024-01-15 10:30:05.000000",
        filler="",
    )


@pytest.fixture
def valid_ascii_record_bytes() -> bytes:
    """A valid 350-byte ASCII record with overpunch-encoded amount.

    Amount +00001234.56 -> 9 integer digits '000001234' + 2 fractional '56'
    With positive overpunch on last digit (6 -> 'F'): '0000012345F'
    """
    tran_id = "0000000000000001"  # 16
    tran_type_cd = "01"  # 2
    tran_cat_cd = "5411"  # 4
    tran_source = "ONLINE    "  # 10
    tran_desc = "GROCERY STORE PURCHASE" + " " * 78  # 100
    tran_amt = "0000012345F"  # 11 (overpunch: +1234.56)
    tran_merchant_id = "123456789"  # 9
    tran_merchant_name = "WHOLE FOODS MARKET" + " " * 32  # 50
    tran_merchant_city = "ARLINGTON" + " " * 41  # 50
    tran_merchant_zip = "22201     "  # 10
    tran_card_num = "4111111111111111"  # 16
    tran_orig_ts = "2024-01-15 10:30:00.000000"  # 26
    tran_proc_ts = "2024-01-15 10:30:05.000000"  # 26
    filler = " " * 20  # 20

    record_str = (
        tran_id
        + tran_type_cd
        + tran_cat_cd
        + tran_source
        + tran_desc
        + tran_amt
        + tran_merchant_id
        + tran_merchant_name
        + tran_merchant_city
        + tran_merchant_zip
        + tran_card_num
        + tran_orig_ts
        + tran_proc_ts
        + filler
    )
    assert len(record_str) == RECORD_LENGTH, f"Expected {RECORD_LENGTH}, got {len(record_str)}"
    return record_str.encode("ascii")


@pytest.fixture
def valid_ebcdic_record_bytes() -> bytes:
    """A valid 350-byte EBCDIC record with zoned-decimal amount.

    Amount +00001234.56 -> zoned decimal:
    F0 F0 F0 F0 F0 F1 F2 F3 F4 F5 C6
    (9 integer digits + 2 fractional, positive sign on last byte)
    """
    tran_id = "0000000000000001"
    tran_type_cd = "01"
    tran_cat_cd = "5411"
    tran_source = "ONLINE    "
    tran_desc = "GROCERY STORE PURCHASE" + " " * 78
    tran_merchant_id = "123456789"
    tran_merchant_name = "WHOLE FOODS MARKET" + " " * 32
    tran_merchant_city = "ARLINGTON" + " " * 41
    tran_merchant_zip = "22201     "
    tran_card_num = "4111111111111111"
    tran_orig_ts = "2024-01-15 10:30:00.000000"
    tran_proc_ts = "2024-01-15 10:30:05.000000"
    filler = " " * 20

    # Build the record as EBCDIC bytes
    codec = "cp037"

    parts = bytearray()
    parts.extend(tran_id.encode(codec))
    parts.extend(tran_type_cd.encode(codec))
    parts.extend(tran_cat_cd.encode(codec))
    parts.extend(tran_source.encode(codec))
    parts.extend(tran_desc.encode(codec))

    # Zoned decimal for +000001234.56 = digits 00000123456 with positive sign
    zoned_amt = bytes([
        0xF0, 0xF0, 0xF0, 0xF0, 0xF0,  # 00000
        0xF1, 0xF2, 0xF3, 0xF4, 0xF5,  # 12345
        0xC6,                             # +6 (positive, last digit 6)
    ])
    parts.extend(zoned_amt)

    parts.extend(tran_merchant_id.encode(codec))
    parts.extend(tran_merchant_name.encode(codec))
    parts.extend(tran_merchant_city.encode(codec))
    parts.extend(tran_merchant_zip.encode(codec))
    parts.extend(tran_card_num.encode(codec))
    parts.extend(tran_orig_ts.encode(codec))
    parts.extend(tran_proc_ts.encode(codec))
    parts.extend(filler.encode(codec))

    assert len(parts) == RECORD_LENGTH, f"Expected {RECORD_LENGTH}, got {len(parts)}"
    return bytes(parts)


@pytest.fixture
def negative_amount_ascii_bytes() -> bytes:
    """A 350-byte ASCII record with a negative amount (-00000050.00).

    Negative overpunch on last digit (0 -> '}'): '000000050}'
    """
    tran_id = "0000000000000002"
    tran_type_cd = "02"
    tran_cat_cd = "5812"
    tran_source = "POS       "
    tran_desc = "REFUND TRANSACTION" + " " * 82
    tran_amt = "0000000500}"  # 11 bytes: -50.00 = 000000050|00, overpunch last 0 -> }
    tran_merchant_id = "987654321"
    tran_merchant_name = "RESTAURANT ABC" + " " * 36
    tran_merchant_city = "BOSTON" + " " * 44
    tran_merchant_zip = "02101     "
    tran_card_num = "5500000000000004"
    tran_orig_ts = "2024-02-20 14:00:00.000000"
    tran_proc_ts = "2024-02-20 14:00:10.000000"
    filler = " " * 20

    record_str = (
        tran_id + tran_type_cd + tran_cat_cd + tran_source + tran_desc
        + tran_amt + tran_merchant_id + tran_merchant_name + tran_merchant_city
        + tran_merchant_zip + tran_card_num + tran_orig_ts + tran_proc_ts + filler
    )
    assert len(record_str) == RECORD_LENGTH
    return record_str.encode("ascii")
