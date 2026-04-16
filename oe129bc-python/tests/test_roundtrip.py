"""Round-trip tests: serialize → parse → compare."""

from decimal import Decimal

from oe129bc.models.transaction import RECORD_LENGTH, TransactionRecord
from oe129bc.services.parser import decode_comp3, parse_transaction, parse_transactions
from oe129bc.services.serializer import encode_comp3, serialize_transaction


class TestASCIIRoundTrip:
    """Verify serialize → parse round-trip in ASCII encoding."""

    def test_roundtrip_preserves_all_fields(self, valid_record: TransactionRecord) -> None:
        """All fields survive a serialize → parse cycle."""
        serialized = serialize_transaction(valid_record, encoding="ascii")
        assert len(serialized) == RECORD_LENGTH
        parsed = parse_transaction(serialized, encoding="ascii")

        assert parsed.tran_id == valid_record.tran_id
        assert parsed.tran_type_cd == valid_record.tran_type_cd
        assert parsed.tran_cat_cd == valid_record.tran_cat_cd
        assert parsed.tran_source == valid_record.tran_source
        assert parsed.tran_desc == valid_record.tran_desc
        assert parsed.tran_amt == valid_record.tran_amt
        assert parsed.tran_merchant_id == valid_record.tran_merchant_id
        assert parsed.tran_merchant_name == valid_record.tran_merchant_name
        assert parsed.tran_merchant_city == valid_record.tran_merchant_city
        assert parsed.tran_merchant_zip == valid_record.tran_merchant_zip
        assert parsed.tran_card_num == valid_record.tran_card_num
        assert parsed.tran_orig_ts == valid_record.tran_orig_ts
        assert parsed.tran_proc_ts == valid_record.tran_proc_ts

    def test_roundtrip_negative_amount(self) -> None:
        """Negative amount survives round-trip."""
        record = TransactionRecord(
            tran_id="0000000000000099",
            tran_type_cd="02",
            tran_cat_cd="5812",
            tran_source="POS",
            tran_desc="REFUND",
            tran_amt=Decimal("-500.75"),
            tran_merchant_id="999888777",
            tran_merchant_name="REFUND STORE",
            tran_merchant_city="CHICAGO",
            tran_merchant_zip="60601",
            tran_card_num="5500000000000004",
            tran_orig_ts="2024-03-15",
            tran_proc_ts="2024-03-15",
        )
        serialized = serialize_transaction(record, encoding="ascii")
        parsed = parse_transaction(serialized, encoding="ascii")
        assert parsed.tran_amt == Decimal("-500.75")

    def test_roundtrip_zero_amount(self) -> None:
        """Zero amount survives round-trip."""
        record = TransactionRecord(
            tran_id="0000000000000003",
            tran_type_cd="03",
            tran_cat_cd="0000",
            tran_source="BATCH",
            tran_desc="ZERO TEST",
            tran_amt=Decimal("0.00"),
            tran_merchant_id="000000001",
            tran_merchant_name="ZERO MERCHANT",
            tran_merchant_city="ZERO CITY",
            tran_merchant_zip="00000",
            tran_card_num="4222222222222222",
            tran_orig_ts="2024-03-01",
            tran_proc_ts="2024-03-01",
        )
        serialized = serialize_transaction(record, encoding="ascii")
        parsed = parse_transaction(serialized, encoding="ascii")
        assert parsed.tran_amt == Decimal("0.00")

    def test_roundtrip_max_amount(self) -> None:
        """Maximum PIC S9(09)V99 amount survives round-trip."""
        record = TransactionRecord(
            tran_id="MAX_AMT_TEST",
            tran_type_cd="99",
            tran_cat_cd="9999",
            tran_source="MAXTEST",
            tran_desc="MAXIMUM AMOUNT TEST",
            tran_amt=Decimal("999999999.99"),
            tran_merchant_id="999999999",
            tran_merchant_name="MAX MERCHANT",
            tran_merchant_city="MAX CITY",
            tran_merchant_zip="99999",
            tran_card_num="9999999999999999",
            tran_orig_ts="2099-12-31",
            tran_proc_ts="2099-12-31",
        )
        serialized = serialize_transaction(record, encoding="ascii")
        parsed = parse_transaction(serialized, encoding="ascii")
        assert parsed.tran_amt == Decimal("999999999.99")


class TestEBCDICRoundTrip:
    """Verify serialize → parse round-trip in EBCDIC encoding."""

    def test_roundtrip_ebcdic_preserves_fields(self, valid_record: TransactionRecord) -> None:
        """All fields survive an EBCDIC serialize → parse cycle."""
        serialized = serialize_transaction(valid_record, encoding="ebcdic")
        assert len(serialized) == RECORD_LENGTH
        parsed = parse_transaction(serialized, encoding="ebcdic")

        assert parsed.tran_id == valid_record.tran_id
        assert parsed.tran_type_cd == valid_record.tran_type_cd
        assert parsed.tran_cat_cd == valid_record.tran_cat_cd
        assert parsed.tran_source == valid_record.tran_source
        assert parsed.tran_amt == valid_record.tran_amt
        assert parsed.tran_merchant_id == valid_record.tran_merchant_id
        assert parsed.tran_merchant_name == valid_record.tran_merchant_name
        assert parsed.tran_card_num == valid_record.tran_card_num

    def test_roundtrip_ebcdic_negative(self) -> None:
        """Negative amount survives EBCDIC round-trip."""
        record = TransactionRecord(
            tran_id="NEG_EBCDIC",
            tran_type_cd="01",
            tran_cat_cd="1234",
            tran_source="TEST",
            tran_desc="NEGATIVE EBCDIC",
            tran_amt=Decimal("-12345.67"),
            tran_merchant_id="111222333",
            tran_merchant_name="EBCDIC SHOP",
            tran_merchant_city="NYC",
            tran_merchant_zip="10001",
            tran_card_num="4111111111111111",
            tran_orig_ts="2024-06-01",
            tran_proc_ts="2024-06-01",
        )
        serialized = serialize_transaction(record, encoding="ebcdic")
        parsed = parse_transaction(serialized, encoding="ebcdic")
        assert parsed.tran_amt == Decimal("-12345.67")


class TestBatchRoundTrip:
    """Verify batch serialize → parse round-trip."""

    def test_batch_roundtrip(self) -> None:
        """Multiple records survive batch round-trip."""
        records = [
            TransactionRecord(
                tran_id=f"{i:016d}",
                tran_type_cd=f"{i % 10:02d}",
                tran_cat_cd=f"{1000 + i:04d}",
                tran_source="BATCH",
                tran_desc=f"BATCH RECORD {i}",
                tran_amt=Decimal(f"{i * 100}.{i % 100:02d}"),
                tran_merchant_id=f"{i:09d}",
                tran_merchant_name=f"MERCHANT {i}",
                tran_merchant_city=f"CITY {i}",
                tran_merchant_zip=f"{10000 + i}",
                tran_card_num=f"{4000000000000000 + i}",
                tran_orig_ts="2024-01-01",
                tran_proc_ts="2024-01-01",
            )
            for i in range(1, 6)
        ]

        batch_bytes = b"".join(serialize_transaction(r, encoding="ascii") for r in records)
        parsed = parse_transactions(batch_bytes, encoding="ascii")

        assert len(parsed) == 5
        for i, (orig, roundtripped) in enumerate(zip(records, parsed)):
            assert roundtripped.tran_id == orig.tran_id, f"Record {i}: tran_id mismatch"
            assert roundtripped.tran_amt == orig.tran_amt, f"Record {i}: tran_amt mismatch"
            assert roundtripped.tran_merchant_id == orig.tran_merchant_id, f"Record {i}: merchant_id mismatch"


class TestComp3RoundTrip:
    """Verify COMP-3 encode → decode round-trip."""

    def test_comp3_positive_roundtrip(self) -> None:
        packed = encode_comp3(Decimal("1234.56"))
        unpacked = decode_comp3(packed)
        assert unpacked == Decimal("1234.56")

    def test_comp3_negative_roundtrip(self) -> None:
        packed = encode_comp3(Decimal("-9876.54"))
        unpacked = decode_comp3(packed)
        assert unpacked == Decimal("-9876.54")

    def test_comp3_zero_roundtrip(self) -> None:
        packed = encode_comp3(Decimal("0.00"))
        unpacked = decode_comp3(packed)
        assert unpacked == Decimal("0.00")

    def test_comp3_max_roundtrip(self) -> None:
        packed = encode_comp3(Decimal("999999999.99"))
        unpacked = decode_comp3(packed)
        assert unpacked == Decimal("999999999.99")

    def test_comp3_min_roundtrip(self) -> None:
        packed = encode_comp3(Decimal("-999999999.99"))
        unpacked = decode_comp3(packed)
        assert unpacked == Decimal("-999999999.99")
