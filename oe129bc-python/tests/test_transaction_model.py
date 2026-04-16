"""Tests for the TransactionRecord Pydantic model."""

from decimal import Decimal

from oe129bc.models.transaction import RECORD_LENGTH, TRAN_RECORD_FIELDS, TransactionRecord


class TestTransactionRecordCreation:
    """Test basic model creation and field constraints."""

    def test_create_valid_record(self, valid_record: TransactionRecord) -> None:
        """A valid TransactionRecord can be created with all fields."""
        assert valid_record.tran_id == "0000000000000001"
        assert valid_record.tran_type_cd == "01"
        assert valid_record.tran_cat_cd == "5411"
        assert valid_record.tran_source == "ONLINE"
        assert valid_record.tran_desc == "GROCERY STORE PURCHASE"
        assert valid_record.tran_amt == Decimal("1234.56")
        assert valid_record.tran_merchant_id == "123456789"
        assert valid_record.tran_merchant_name == "WHOLE FOODS MARKET"
        assert valid_record.tran_merchant_city == "ARLINGTON"
        assert valid_record.tran_merchant_zip == "22201"
        assert valid_record.tran_card_num == "4111111111111111"
        assert valid_record.tran_orig_ts == "2024-01-15 10:30:00.000000"
        assert valid_record.tran_proc_ts == "2024-01-15 10:30:05.000000"

    def test_negative_amount(self) -> None:
        """A record with a negative amount is valid."""
        record = TransactionRecord(
            tran_id="0000000000000002",
            tran_type_cd="02",
            tran_cat_cd="5812",
            tran_source="POS",
            tran_desc="REFUND",
            tran_amt=Decimal("-50.00"),
            tran_merchant_id="987654321",
            tran_merchant_name="SHOP",
            tran_merchant_city="NYC",
            tran_merchant_zip="10001",
            tran_card_num="5500000000000004",
            tran_orig_ts="2024-02-20",
            tran_proc_ts="2024-02-20",
        )
        assert record.tran_amt == Decimal("-50.00")

    def test_zero_amount(self) -> None:
        """A record with zero amount is valid."""
        record = TransactionRecord(
            tran_id="0000000000000003",
            tran_type_cd="03",
            tran_cat_cd="0000",
            tran_source="BATCH",
            tran_desc="ZERO AMOUNT TEST",
            tran_amt=Decimal("0.00"),
            tran_merchant_id="000000001",
            tran_merchant_name="TEST MERCHANT",
            tran_merchant_city="TEST CITY",
            tran_merchant_zip="00000",
            tran_card_num="4222222222222222",
            tran_orig_ts="2024-03-01",
            tran_proc_ts="2024-03-01",
        )
        assert record.tran_amt == Decimal("0.00")

    def test_filler_defaults_to_empty(self) -> None:
        """Filler field defaults to empty string."""
        record = TransactionRecord(
            tran_id="1",
            tran_type_cd="01",
            tran_cat_cd="5411",
            tran_source="SRC",
            tran_desc="DESC",
            tran_amt=Decimal("1.00"),
            tran_merchant_id="123456789",
            tran_merchant_name="NAME",
            tran_merchant_city="CITY",
            tran_merchant_zip="12345",
            tran_card_num="4111111111111111",
            tran_orig_ts="2024-01-01",
            tran_proc_ts="2024-01-01",
        )
        assert record.filler == ""

    def test_max_amount(self) -> None:
        """Amount at the PIC S9(09)V99 maximum."""
        record = TransactionRecord(
            tran_id="MAX",
            tran_type_cd="99",
            tran_cat_cd="9999",
            tran_source="TEST",
            tran_desc="MAX AMOUNT",
            tran_amt=Decimal("999999999.99"),
            tran_merchant_id="999999999",
            tran_merchant_name="MAX",
            tran_merchant_city="MAX",
            tran_merchant_zip="99999",
            tran_card_num="9999999999999999",
            tran_orig_ts="2099-12-31",
            tran_proc_ts="2099-12-31",
        )
        assert record.tran_amt == Decimal("999999999.99")


class TestFieldSpecLayout:
    """Test that field specs match the 350-byte COBOL layout exactly."""

    def test_record_length_is_350(self) -> None:
        """TRAN-RECORD is exactly 350 bytes."""
        assert RECORD_LENGTH == 350

    def test_field_count(self) -> None:
        """There are 14 fields in TRAN-RECORD (including FILLER)."""
        assert len(TRAN_RECORD_FIELDS) == 14

    def test_fields_cover_full_record(self) -> None:
        """All field specs cover the entire 350-byte record without gaps or overlaps."""
        last_end = 0
        for spec in TRAN_RECORD_FIELDS:
            assert spec.offset == last_end, f"Gap before {spec.name}: expected offset {last_end}, got {spec.offset}"
            last_end = spec.offset + spec.length
        assert last_end == RECORD_LENGTH

    def test_tran_id_spec(self) -> None:
        assert TRAN_RECORD_FIELDS[0].name == "tran_id"
        assert TRAN_RECORD_FIELDS[0].offset == 0
        assert TRAN_RECORD_FIELDS[0].length == 16

    def test_tran_amt_spec(self) -> None:
        spec = TRAN_RECORD_FIELDS[5]
        assert spec.name == "tran_amt"
        assert spec.offset == 132
        assert spec.length == 11
        assert spec.field_type == "signed_decimal"

    def test_filler_spec(self) -> None:
        spec = TRAN_RECORD_FIELDS[-1]
        assert spec.name == "filler"
        assert spec.offset == 330
        assert spec.length == 20
