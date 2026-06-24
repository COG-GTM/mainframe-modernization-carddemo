from datetime import datetime
from decimal import Decimal

import pytest

from carddemo_etl import transforms as T


@pytest.mark.parametrize(
    "raw,expected",
    [
        ("0000005047G", Decimal("504.77")),   # G -> +7
        ("0000009190}", Decimal("-919.00")),  # } -> -0, negative
        ("00000000000", Decimal("0.00")),
        ("0000000001A", Decimal("0.11")),     # A -> +1
        ("0000000001J", Decimal("-0.11")),    # J -> -1
        ("0000000123", Decimal("12.3")),      # no overpunch, trailing digit
    ],
)
def test_parse_signed_decimal(raw, expected):
    decimals = 2 if len(raw) == 11 else 1
    result = T.parse_signed_decimal(raw, decimals)
    assert result == expected
    # Scale must match the implied decimal places (NUMERIC(11,2) fidelity).
    assert -result.as_tuple().exponent == decimals


def test_parse_signed_decimal_invalid():
    with pytest.raises(ValueError):
        T.parse_signed_decimal("000000000$X", 2)


def test_overpunch_full_alphabet():
    pos = "{ABCDEFGHI"
    neg = "}JKLMNOPQR"
    for i, ch in enumerate(pos):
        assert T.parse_signed_decimal("0" + ch, 0) == Decimal(i)
    for i, ch in enumerate(neg):
        assert T.parse_signed_decimal("0" + ch, 0) == Decimal(-i)


def test_clean_text():
    assert T.clean_text("POS TERM  ") == "POS TERM"
    assert T.clean_text("          ") is None
    assert T.clean_text("") is None


def test_parse_int():
    assert T.parse_int("800000000") == 800000000
    assert T.parse_int("000000001") == 1
    assert T.parse_int("         ") is None


def test_parse_numeric_string_preserves_zeros():
    assert T.parse_numeric_string("0001") == "0001"
    assert T.parse_numeric_string("    ") is None


def test_parse_timestamp():
    assert T.parse_timestamp("2022-06-10 19:27:53.000000") == datetime(
        2022, 6, 10, 19, 27, 53
    )
    assert T.parse_timestamp("2022-06-10-19.27.53.000000") == datetime(
        2022, 6, 10, 19, 27, 53
    )
    assert T.parse_timestamp("                          ") is None
    assert T.parse_timestamp("0000-00-00 00:00:00.000000") is None


def test_parse_timestamp_invalid():
    with pytest.raises(ValueError):
        T.parse_timestamp("not-a-timestamp-at-all-xx")


def test_mask_card_number():
    assert T.mask_card_number("4859452612877065") == "************7065"
    assert T.mask_card_number("   ") is None
    assert T.mask_card_number("123") == "123"


def test_decode_ebcdic():
    # 0xC1 == 'A' in cp037.
    assert T.decode_ebcdic(b"\xc1\xc2\xc3") == "ABC"
