"""Tests for the transaction parser module."""

from decimal import Decimal

import pytest

from oe129bc.services.parser import (
    ParseError,
    _decode_zoned_decimal_ascii,
    _decode_zoned_decimal_ebcdic,
    _detect_encoding,
    decode_comp3,
    parse_transaction,
    parse_transactions,
)


class TestParseTransactionASCII:
    """Test parsing of ASCII-encoded records."""

    def test_parse_valid_ascii_record(self, valid_ascii_record_bytes: bytes) -> None:
        """Parse a well-formed ASCII record."""
        record = parse_transaction(valid_ascii_record_bytes, encoding="ascii")
        assert record.tran_id == "0000000000000001"
        assert record.tran_type_cd == "01"
        assert record.tran_cat_cd == "5411"
        assert record.tran_source == "ONLINE"
        assert record.tran_desc == "GROCERY STORE PURCHASE"
        assert record.tran_amt == Decimal("1234.56")
        assert record.tran_merchant_id == "123456789"
        assert record.tran_merchant_name == "WHOLE FOODS MARKET"
        assert record.tran_merchant_city == "ARLINGTON"
        assert record.tran_merchant_zip == "22201"
        assert record.tran_card_num == "4111111111111111"
        assert record.tran_orig_ts == "2024-01-15 10:30:00.000000"
        assert record.tran_proc_ts == "2024-01-15 10:30:05.000000"

    def test_parse_negative_amount_ascii(self, negative_amount_ascii_bytes: bytes) -> None:
        """Parse an ASCII record with a negative amount (overpunch)."""
        record = parse_transaction(negative_amount_ascii_bytes, encoding="ascii")
        assert record.tran_amt == Decimal("-50.00")
        assert record.tran_id == "0000000000000002"

    def test_parse_wrong_length_raises(self) -> None:
        """Raise ParseError for non-350-byte input."""
        with pytest.raises(ParseError, match="exactly 350 bytes"):
            parse_transaction(b"too short", encoding="ascii")

    def test_parse_empty_raises(self) -> None:
        """Raise ParseError for empty input."""
        with pytest.raises(ParseError, match="exactly 350 bytes"):
            parse_transaction(b"", encoding="ascii")


class TestParseTransactionEBCDIC:
    """Test parsing of EBCDIC-encoded records."""

    def test_parse_valid_ebcdic_record(self, valid_ebcdic_record_bytes: bytes) -> None:
        """Parse a well-formed EBCDIC record."""
        record = parse_transaction(valid_ebcdic_record_bytes, encoding="ebcdic")
        assert record.tran_id == "0000000000000001"
        assert record.tran_type_cd == "01"
        assert record.tran_cat_cd == "5411"
        assert record.tran_amt == Decimal("1234.56")
        assert record.tran_merchant_id == "123456789"
        assert record.tran_card_num == "4111111111111111"


class TestZonedDecimalEBCDIC:
    """Test EBCDIC zoned decimal decoding."""

    def test_positive_amount(self) -> None:
        """Decode a positive EBCDIC zoned decimal."""
        # +000001234.56 -> F0 F0 F0 F0 F0 F1 F2 F3 F4 F5 C6
        raw = bytes([0xF0, 0xF0, 0xF0, 0xF0, 0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xC6])
        assert _decode_zoned_decimal_ebcdic(raw) == Decimal("1234.56")

    def test_negative_amount(self) -> None:
        """Decode a negative EBCDIC zoned decimal."""
        # -000001234.56 -> F0 F0 F0 F0 F0 F1 F2 F3 F4 F5 D6
        raw = bytes([0xF0, 0xF0, 0xF0, 0xF0, 0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xD6])
        assert _decode_zoned_decimal_ebcdic(raw) == Decimal("-1234.56")

    def test_zero_amount(self) -> None:
        """Decode zero in EBCDIC zoned decimal."""
        raw = bytes([0xF0] * 10 + [0xC0])
        assert _decode_zoned_decimal_ebcdic(raw) == Decimal("0.00")

    def test_max_amount(self) -> None:
        """Decode the maximum PIC S9(09)V99 value."""
        # +999999999.99 -> F9 F9 F9 F9 F9 F9 F9 F9 F9 F9 C9
        raw = bytes([0xF9] * 10 + [0xC9])
        assert _decode_zoned_decimal_ebcdic(raw) == Decimal("999999999.99")

    def test_wrong_length_raises(self) -> None:
        """Raise ParseError for wrong byte count."""
        with pytest.raises(ParseError, match="11 bytes"):
            _decode_zoned_decimal_ebcdic(bytes([0xF0] * 5))

    def test_invalid_digit_nibble_raises(self) -> None:
        """Raise ParseError for invalid digit nibble."""
        raw = bytes([0xFA] + [0xF0] * 10)
        with pytest.raises(ParseError, match="Invalid digit nibble"):
            _decode_zoned_decimal_ebcdic(raw)


class TestZonedDecimalASCII:
    """Test ASCII zoned decimal decoding."""

    def test_positive_overpunch(self) -> None:
        """Decode positive amount with trailing overpunch."""
        # +000001234.56: digits '0000012345' + overpunch 'F' (+6)
        raw = b"0000012345F"
        assert _decode_zoned_decimal_ascii(raw) == Decimal("1234.56")

    def test_negative_overpunch(self) -> None:
        """Decode negative amount with trailing overpunch."""
        # -000000050.00: S9(09)V99 = 000000050|00, overpunch last 0 -> '}'
        raw = b"0000000500}"
        assert _decode_zoned_decimal_ascii(raw) == Decimal("-50.00")

    def test_positive_zero_overpunch(self) -> None:
        """Decode zero with positive overpunch."""
        # +000000000.00: digits '0000000000' + overpunch '{' (+0)
        raw = b"0000000000{"
        assert _decode_zoned_decimal_ascii(raw) == Decimal("0.00")

    def test_unsigned_digits(self) -> None:
        """All-digit field is treated as unsigned positive."""
        raw = b"00000123456"
        assert _decode_zoned_decimal_ascii(raw) == Decimal("1234.56")


class TestComp3:
    """Test COMP-3 packed decimal decoding."""

    def test_positive_comp3(self) -> None:
        """Decode a positive COMP-3 value."""
        # +000001234.56 -> digits 00000123456, sign C
        # Nibbles: 0 0 0 0 0 1 2 3 4 5 6 C -> bytes 00 00 01 23 45 6C
        raw = bytes([0x00, 0x00, 0x01, 0x23, 0x45, 0x6C])
        assert decode_comp3(raw) == Decimal("1234.56")

    def test_negative_comp3(self) -> None:
        """Decode a negative COMP-3 value."""
        # -000001234.56 -> nibbles 0 0 0 0 0 1 2 3 4 5 6 D -> bytes 00 00 01 23 45 6D
        raw = bytes([0x00, 0x00, 0x01, 0x23, 0x45, 0x6D])
        assert decode_comp3(raw) == Decimal("-1234.56")

    def test_zero_comp3(self) -> None:
        """Decode zero in COMP-3."""
        raw = bytes([0x00, 0x00, 0x00, 0x00, 0x00, 0x0C])
        assert decode_comp3(raw) == Decimal("0.00")

    def test_wrong_length_comp3(self) -> None:
        """Raise ParseError for wrong COMP-3 byte count."""
        with pytest.raises(ParseError, match="expected 6 bytes"):
            decode_comp3(bytes([0x00] * 3))


class TestBatchParser:
    """Test batch parsing of multiple records."""

    def test_parse_single_record_batch(self, valid_ascii_record_bytes: bytes) -> None:
        """Batch parsing a single record returns a list of one."""
        records = parse_transactions(valid_ascii_record_bytes, encoding="ascii")
        assert len(records) == 1
        assert records[0].tran_id == "0000000000000001"

    def test_parse_multiple_records(self, valid_ascii_record_bytes: bytes, negative_amount_ascii_bytes: bytes) -> None:
        """Batch parsing two concatenated records."""
        batch_data = valid_ascii_record_bytes + negative_amount_ascii_bytes
        records = parse_transactions(batch_data, encoding="ascii")
        assert len(records) == 2
        assert records[0].tran_id == "0000000000000001"
        assert records[1].tran_id == "0000000000000002"
        assert records[0].tran_amt == Decimal("1234.56")
        assert records[1].tran_amt == Decimal("-50.00")

    def test_parse_empty_batch(self) -> None:
        """Batch parsing empty data returns empty list."""
        records = parse_transactions(b"", encoding="ascii")
        assert records == []

    def test_parse_non_multiple_raises(self) -> None:
        """Batch parsing data not a multiple of 350 raises ParseError."""
        with pytest.raises(ParseError, match="not a multiple"):
            parse_transactions(b"x" * 351, encoding="ascii")

    def test_parse_three_records(self, valid_ascii_record_bytes: bytes) -> None:
        """Batch parsing three identical records."""
        batch_data = valid_ascii_record_bytes * 3
        records = parse_transactions(batch_data, encoding="ascii")
        assert len(records) == 3
        for r in records:
            assert r.tran_id == "0000000000000001"


class TestEncodingDetection:
    """Test automatic EBCDIC vs ASCII detection."""

    def test_detect_ascii(self, valid_ascii_record_bytes: bytes) -> None:
        """ASCII record is detected as ASCII."""
        assert _detect_encoding(valid_ascii_record_bytes) == "ascii"

    def test_detect_ebcdic(self, valid_ebcdic_record_bytes: bytes) -> None:
        """EBCDIC record is detected as EBCDIC."""
        assert _detect_encoding(valid_ebcdic_record_bytes) == "ebcdic"

    def test_detect_empty(self) -> None:
        """Empty data defaults to ASCII."""
        assert _detect_encoding(b"") == "ascii"

    def test_auto_detect_parses_correctly(self, valid_ascii_record_bytes: bytes) -> None:
        """Auto-detected encoding produces correct parse result."""
        record = parse_transaction(valid_ascii_record_bytes)
        assert record.tran_id == "0000000000000001"
        assert record.tran_amt == Decimal("1234.56")
