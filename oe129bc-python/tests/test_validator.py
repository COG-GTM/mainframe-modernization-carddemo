"""Tests for the transaction validation module (COTRN02C.cbl rules)."""

from decimal import Decimal

from oe129bc.models.transaction import TransactionRecord
from oe129bc.services.validator import (
    validate_amount_format,
    validate_transaction,
)


def _make_record(**overrides) -> TransactionRecord:
    """Helper: create a valid record, then override specific fields."""
    defaults = {
        "tran_id": "0000000000000001",
        "tran_type_cd": "01",
        "tran_cat_cd": "5411",
        "tran_source": "ONLINE",
        "tran_desc": "TEST PURCHASE",
        "tran_amt": Decimal("100.00"),
        "tran_merchant_id": "123456789",
        "tran_merchant_name": "TEST MERCHANT",
        "tran_merchant_city": "TEST CITY",
        "tran_merchant_zip": "12345",
        "tran_card_num": "4111111111111111",
        "tran_orig_ts": "2024-01-15",
        "tran_proc_ts": "2024-01-15",
        "filler": "",
    }
    defaults.update(overrides)
    return TransactionRecord(**defaults)


class TestValidateValidRecord:
    """Test that a fully-valid record passes validation."""

    def test_valid_record_passes(self) -> None:
        record = _make_record()
        result = validate_transaction(record)
        assert result.is_valid
        assert result.errors == []

    def test_valid_record_with_timestamps(self) -> None:
        record = _make_record(
            tran_orig_ts="2024-06-15 12:00:00.000000",
            tran_proc_ts="2024-06-15 12:00:01.000000",
        )
        result = validate_transaction(record)
        assert result.is_valid


class TestValidateEmptyFields:
    """Test required-field validation (COTRN02C: 'can NOT be empty')."""

    def test_empty_tran_id(self) -> None:
        result = validate_transaction(_make_record(tran_id=""))
        assert not result.is_valid
        assert any(e.field_name == "tran_id" and e.rule == "required" for e in result.errors)

    def test_empty_type_cd(self) -> None:
        result = validate_transaction(_make_record(tran_type_cd=""))
        assert not result.is_valid
        assert any(e.field_name == "tran_type_cd" for e in result.errors)

    def test_empty_cat_cd(self) -> None:
        result = validate_transaction(_make_record(tran_cat_cd=""))
        assert not result.is_valid
        assert any(e.field_name == "tran_cat_cd" for e in result.errors)

    def test_empty_source(self) -> None:
        result = validate_transaction(_make_record(tran_source=""))
        assert not result.is_valid
        assert any(e.field_name == "tran_source" for e in result.errors)

    def test_empty_desc(self) -> None:
        result = validate_transaction(_make_record(tran_desc=""))
        assert not result.is_valid
        assert any(e.field_name == "tran_desc" for e in result.errors)

    def test_empty_merchant_id(self) -> None:
        result = validate_transaction(_make_record(tran_merchant_id=""))
        assert not result.is_valid
        assert any(e.field_name == "tran_merchant_id" for e in result.errors)

    def test_empty_merchant_name(self) -> None:
        result = validate_transaction(_make_record(tran_merchant_name=""))
        assert not result.is_valid
        assert any(e.field_name == "tran_merchant_name" for e in result.errors)

    def test_empty_merchant_city(self) -> None:
        result = validate_transaction(_make_record(tran_merchant_city=""))
        assert not result.is_valid
        assert any(e.field_name == "tran_merchant_city" for e in result.errors)

    def test_empty_merchant_zip(self) -> None:
        result = validate_transaction(_make_record(tran_merchant_zip=""))
        assert not result.is_valid
        assert any(e.field_name == "tran_merchant_zip" for e in result.errors)

    def test_empty_card_num(self) -> None:
        result = validate_transaction(_make_record(tran_card_num=""))
        assert not result.is_valid
        assert any(e.field_name == "tran_card_num" for e in result.errors)

    def test_empty_orig_ts(self) -> None:
        result = validate_transaction(_make_record(tran_orig_ts=""))
        assert not result.is_valid
        assert any(e.field_name == "tran_orig_ts" for e in result.errors)

    def test_empty_proc_ts(self) -> None:
        result = validate_transaction(_make_record(tran_proc_ts=""))
        assert not result.is_valid
        assert any(e.field_name == "tran_proc_ts" for e in result.errors)


class TestValidateNumericFields:
    """Test numeric validation rules (COTRN02C: 'must be Numeric')."""

    def test_non_numeric_type_cd(self) -> None:
        """Type CD 'AB' is not numeric."""
        result = validate_transaction(_make_record(tran_type_cd="AB"))
        assert not result.is_valid
        assert any(e.field_name == "tran_type_cd" and e.rule == "numeric" for e in result.errors)

    def test_non_numeric_cat_cd(self) -> None:
        """Category CD 'ABCD' is not numeric."""
        result = validate_transaction(_make_record(tran_cat_cd="ABCD"))
        assert not result.is_valid
        assert any(e.field_name == "tran_cat_cd" and e.rule == "numeric" for e in result.errors)

    def test_non_numeric_merchant_id(self) -> None:
        """Merchant ID 'ABC123DEF' is not numeric."""
        result = validate_transaction(_make_record(tran_merchant_id="ABC123DEF"))
        assert not result.is_valid
        assert any(e.field_name == "tran_merchant_id" and e.rule == "numeric" for e in result.errors)


class TestValidateAmountRange:
    """Test amount range validation (PIC S9(09)V99 bounds)."""

    def test_amount_over_max(self) -> None:
        """Amount exceeding 999999999.99 is invalid."""
        result = validate_transaction(_make_record(tran_amt=Decimal("1000000000.00")))
        assert not result.is_valid
        assert any(e.field_name == "tran_amt" and e.rule == "range" for e in result.errors)

    def test_amount_under_min(self) -> None:
        """Amount below -999999999.99 is invalid."""
        result = validate_transaction(_make_record(tran_amt=Decimal("-1000000000.00")))
        assert not result.is_valid
        assert any(e.field_name == "tran_amt" and e.rule == "range" for e in result.errors)

    def test_amount_too_many_decimals(self) -> None:
        """Amount with >2 decimal places is invalid."""
        result = validate_transaction(_make_record(tran_amt=Decimal("123.456")))
        assert not result.is_valid
        assert any(e.field_name == "tran_amt" and e.rule == "precision" for e in result.errors)


class TestValidateDateFormat:
    """Test date validation (COTRN02C: YYYY-MM-DD position checks + CSUTLDTC)."""

    def test_invalid_orig_date_format(self) -> None:
        """Origination date not in YYYY-MM-DD format."""
        result = validate_transaction(_make_record(tran_orig_ts="01/15/2024"))
        assert not result.is_valid
        assert any(e.field_name == "tran_orig_ts" for e in result.errors)

    def test_invalid_proc_date_format(self) -> None:
        """Processing date not in YYYY-MM-DD format."""
        result = validate_transaction(_make_record(tran_proc_ts="2024.01.15"))
        assert not result.is_valid
        assert any(e.field_name == "tran_proc_ts" for e in result.errors)

    def test_invalid_orig_date_value(self) -> None:
        """Origination date Feb 30 is not a real date."""
        result = validate_transaction(_make_record(tran_orig_ts="2024-02-30"))
        assert not result.is_valid
        assert any(e.field_name == "tran_orig_ts" and e.rule == "invalid_date" for e in result.errors)

    def test_invalid_proc_date_value(self) -> None:
        """Processing date month 13 is not valid."""
        result = validate_transaction(_make_record(tran_proc_ts="2024-13-01"))
        assert not result.is_valid
        assert any(e.field_name == "tran_proc_ts" and e.rule == "invalid_date" for e in result.errors)

    def test_valid_leap_day(self) -> None:
        """Feb 29 in a leap year is valid."""
        result = validate_transaction(_make_record(tran_orig_ts="2024-02-29", tran_proc_ts="2024-02-29"))
        assert result.is_valid

    def test_invalid_leap_day(self) -> None:
        """Feb 29 in a non-leap year is invalid."""
        result = validate_transaction(_make_record(tran_orig_ts="2023-02-29"))
        assert not result.is_valid

    def test_date_with_timestamp_suffix(self) -> None:
        """Date with time portion like '2024-01-15 10:30:00' is valid."""
        result = validate_transaction(
            _make_record(
                tran_orig_ts="2024-01-15 10:30:00.000000",
                tran_proc_ts="2024-01-15 10:30:05.000000",
            )
        )
        assert result.is_valid


class TestValidateAmountFormat:
    """Test the COBOL amount input format validator."""

    def test_valid_positive(self) -> None:
        assert validate_amount_format("+00001234.56") == []

    def test_valid_negative(self) -> None:
        assert validate_amount_format("-00001234.56") == []

    def test_valid_zero(self) -> None:
        assert validate_amount_format("+00000000.00") == []

    def test_missing_sign(self) -> None:
        errors = validate_amount_format("000001234.56")
        assert any("sign" in e for e in errors)

    def test_missing_decimal(self) -> None:
        errors = validate_amount_format("+001234567890")
        assert len(errors) > 0

    def test_too_short(self) -> None:
        errors = validate_amount_format("+123.45")
        assert len(errors) > 0

    def test_non_numeric_digits(self) -> None:
        errors = validate_amount_format("+0000ABCD.00")
        assert len(errors) > 0


class TestMultipleErrors:
    """Test that validation accumulates multiple errors."""

    def test_multiple_empty_fields(self) -> None:
        """Record with multiple empty required fields collects all errors."""
        record = _make_record(
            tran_type_cd="",
            tran_cat_cd="",
            tran_source="",
            tran_merchant_id="",
        )
        result = validate_transaction(record)
        assert not result.is_valid
        assert len(result.errors) >= 4
        field_names = {e.field_name for e in result.errors}
        assert "tran_type_cd" in field_names
        assert "tran_cat_cd" in field_names
        assert "tran_source" in field_names
        assert "tran_merchant_id" in field_names
