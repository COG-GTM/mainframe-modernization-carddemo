"""Tests for the transaction serializer module."""

from decimal import Decimal

import pytest

from oe129bc.models.transaction import RECORD_LENGTH, TransactionRecord
from oe129bc.services.serializer import (
    SerializeError,
    _encode_zoned_decimal_ascii,
    _encode_zoned_decimal_ebcdic,
    encode_comp3,
    serialize_transaction,
)


class TestSerializeASCII:
    """Test ASCII serialization."""

    def test_serialize_produces_350_bytes(self, valid_record: TransactionRecord) -> None:
        """Serialized output is exactly 350 bytes."""
        result = serialize_transaction(valid_record, encoding="ascii")
        assert len(result) == RECORD_LENGTH

    def test_serialize_text_fields_padded(self, valid_record: TransactionRecord) -> None:
        """Text fields are space-padded to their exact width."""
        result = serialize_transaction(valid_record, encoding="ascii")
        # TRAN-ID at offset 0, length 16
        tran_id = result[0:16].decode("ascii")
        assert tran_id == "0000000000000001"
        # TRAN-SOURCE at offset 22, length 10
        tran_source = result[22:32].decode("ascii")
        assert tran_source == "ONLINE    "
        assert len(tran_source) == 10

    def test_serialize_numeric_fields_zero_padded(self, valid_record: TransactionRecord) -> None:
        """Numeric fields are zero-padded to their exact width."""
        result = serialize_transaction(valid_record, encoding="ascii")
        # TRAN-CAT-CD at offset 18, length 4
        cat_cd = result[18:22].decode("ascii")
        assert cat_cd == "5411"
        # TRAN-MERCHANT-ID at offset 143, length 9
        merchant_id = result[143:152].decode("ascii")
        assert merchant_id == "123456789"

    def test_serialize_filler_space_padded(self, valid_record: TransactionRecord) -> None:
        """Filler field is space-padded to 20 bytes."""
        result = serialize_transaction(valid_record, encoding="ascii")
        filler = result[330:350].decode("ascii")
        assert filler == " " * 20


class TestSerializeEBCDIC:
    """Test EBCDIC serialization."""

    def test_serialize_ebcdic_produces_350_bytes(self, valid_record: TransactionRecord) -> None:
        """EBCDIC serialized output is exactly 350 bytes."""
        result = serialize_transaction(valid_record, encoding="ebcdic")
        assert len(result) == RECORD_LENGTH

    def test_serialize_ebcdic_amount_positive(self, valid_record: TransactionRecord) -> None:
        """Positive amount encodes with 0xC zone nibble on last byte."""
        result = serialize_transaction(valid_record, encoding="ebcdic")
        amt_bytes = result[132:143]
        # +1234.56 -> 000001234 56 -> last byte should have zone 0xC
        last_byte = amt_bytes[-1]
        assert (last_byte >> 4) & 0x0F == 0x0C  # Positive sign

    def test_serialize_ebcdic_amount_negative(self) -> None:
        """Negative amount encodes with 0xD zone nibble on last byte."""
        record = TransactionRecord(
            tran_id="1",
            tran_type_cd="01",
            tran_cat_cd="5411",
            tran_source="SRC",
            tran_desc="DESC",
            tran_amt=Decimal("-99.99"),
            tran_merchant_id="123456789",
            tran_merchant_name="NAME",
            tran_merchant_city="CITY",
            tran_merchant_zip="12345",
            tran_card_num="4111111111111111",
            tran_orig_ts="2024-01-01",
            tran_proc_ts="2024-01-01",
        )
        result = serialize_transaction(record, encoding="ebcdic")
        amt_bytes = result[132:143]
        last_byte = amt_bytes[-1]
        assert (last_byte >> 4) & 0x0F == 0x0D  # Negative sign


class TestZonedDecimalEncoding:
    """Test individual zoned decimal encoding functions."""

    def test_encode_positive_ebcdic(self) -> None:
        """Encode +1234.56 as EBCDIC zoned decimal."""
        result = _encode_zoned_decimal_ebcdic(Decimal("1234.56"))
        assert len(result) == 11
        assert result[-1] == 0xC6  # Last digit 6, positive sign

    def test_encode_negative_ebcdic(self) -> None:
        """Encode -1234.56 as EBCDIC zoned decimal."""
        result = _encode_zoned_decimal_ebcdic(Decimal("-1234.56"))
        assert len(result) == 11
        assert result[-1] == 0xD6  # Last digit 6, negative sign

    def test_encode_zero_ebcdic(self) -> None:
        """Encode 0.00 as EBCDIC zoned decimal."""
        result = _encode_zoned_decimal_ebcdic(Decimal("0.00"))
        assert len(result) == 11
        assert result[-1] == 0xC0  # Last digit 0, positive sign

    def test_encode_positive_ascii(self) -> None:
        """Encode +1234.56 as ASCII with overpunch."""
        result = _encode_zoned_decimal_ascii(Decimal("1234.56"))
        assert len(result) == 11
        # Last char should be 'F' (positive overpunch for digit 6)
        assert result[-1:] == b"F"

    def test_encode_negative_ascii(self) -> None:
        """Encode -50.00 as ASCII with overpunch."""
        result = _encode_zoned_decimal_ascii(Decimal("-50.00"))
        assert len(result) == 11
        # Last digit is 0, negative overpunch: '}'
        assert result[-1:] == b"}"

    def test_overflow_raises(self) -> None:
        """Raise SerializeError if value exceeds field capacity."""
        with pytest.raises(SerializeError, match="exceeds"):
            _encode_zoned_decimal_ebcdic(Decimal("9999999999.99"))


class TestComp3Encoding:
    """Test COMP-3 packed decimal encoding."""

    def test_encode_positive(self) -> None:
        """Encode +1234.56 as COMP-3."""
        result = encode_comp3(Decimal("1234.56"))
        assert len(result) == 6
        assert result[-1] & 0x0F == 0x0C  # Positive sign

    def test_encode_negative(self) -> None:
        """Encode -1234.56 as COMP-3."""
        result = encode_comp3(Decimal("-1234.56"))
        assert len(result) == 6
        assert result[-1] & 0x0F == 0x0D  # Negative sign

    def test_encode_zero(self) -> None:
        """Encode 0.00 as COMP-3."""
        result = encode_comp3(Decimal("0.00"))
        assert len(result) == 6
        assert result[-1] & 0x0F == 0x0C
