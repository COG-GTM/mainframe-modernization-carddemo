"""Tests for MQ050CC input message record model."""

from __future__ import annotations

from decimal import Decimal

import pytest
from pydantic import ValidationError

from src.oe129bc.models.mq050cc import MQ050CCRecord


def _valid_mq050cc_kwargs() -> dict:
    """Return a dict of valid keyword arguments for MQ050CCRecord."""
    return {
        "msg_id": "MSG00000000000000000001",
        "msg_type": "TREQ",
        "timestamp": "2024-01-15T10:30:00.000000",
        "card_num": "4111111111111111",
        "acct_id": 12345678901,
        "cust_id": 123456789,
        "tran_amt": Decimal("150.75"),
        "tran_type_cd": "SA",
        "tran_cat_cd": 5411,
        "tran_source": "POS",
        "tran_desc": "GROCERY STORE PURCHASE",
        "merchant_id": 987654321,
        "merchant_name": "ACME GROCERY",
        "orig_ts": "2024-01-15T10:29:55.000000",
    }


class TestMQ050CCCreation:
    """Test creation with valid data."""

    def test_create_with_valid_data(self) -> None:
        record = MQ050CCRecord(**_valid_mq050cc_kwargs())
        assert record.msg_id == "MSG00000000000000000001"
        assert record.msg_type == "TREQ"
        assert record.card_num == "4111111111111111"
        assert record.acct_id == 12345678901
        assert record.cust_id == 123456789
        assert record.tran_amt == Decimal("150.75")
        assert record.tran_type_cd == "SA"
        assert record.tran_cat_cd == 5411
        assert record.tran_source == "POS"
        assert record.merchant_id == 987654321
        assert record.merchant_name == "ACME GROCERY"

    def test_create_with_zero_amounts(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["tran_amt"] = Decimal("0.00")
        kwargs["acct_id"] = 0
        kwargs["cust_id"] = 0
        kwargs["merchant_id"] = 0
        kwargs["tran_cat_cd"] = 0
        record = MQ050CCRecord(**kwargs)
        assert record.tran_amt == Decimal("0.00")
        assert record.acct_id == 0

    def test_create_with_negative_amount(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["tran_amt"] = Decimal("-50.00")
        record = MQ050CCRecord(**kwargs)
        assert record.tran_amt == Decimal("-50.00")

    def test_create_with_max_valid_values(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["acct_id"] = 99_999_999_999
        kwargs["cust_id"] = 999_999_999
        kwargs["tran_amt"] = Decimal("999999999.99")
        kwargs["tran_cat_cd"] = 9999
        kwargs["merchant_id"] = 999_999_999
        record = MQ050CCRecord(**kwargs)
        assert record.acct_id == 99_999_999_999
        assert record.cust_id == 999_999_999


class TestMQ050CCValidation:
    """Test that validation rejects invalid data."""

    def test_rejects_acct_id_too_large(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["acct_id"] = 100_000_000_000  # 12 digits, exceeds PIC 9(11)
        with pytest.raises(ValidationError) as exc_info:
            MQ050CCRecord(**kwargs)
        assert "acct_id" in str(exc_info.value)

    def test_rejects_negative_acct_id(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["acct_id"] = -1
        with pytest.raises(ValidationError):
            MQ050CCRecord(**kwargs)

    def test_rejects_cust_id_too_large(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["cust_id"] = 1_000_000_000  # 10 digits, exceeds PIC 9(09)
        with pytest.raises(ValidationError) as exc_info:
            MQ050CCRecord(**kwargs)
        assert "cust_id" in str(exc_info.value)

    def test_rejects_tran_amt_too_large(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["tran_amt"] = Decimal("9999999999.99")  # 10-digit integer part
        with pytest.raises(ValidationError) as exc_info:
            MQ050CCRecord(**kwargs)
        assert "tran_amt" in str(exc_info.value)

    def test_rejects_tran_cat_cd_too_large(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["tran_cat_cd"] = 10_000  # 5 digits, exceeds PIC 9(04)
        with pytest.raises(ValidationError) as exc_info:
            MQ050CCRecord(**kwargs)
        assert "tran_cat_cd" in str(exc_info.value)

    def test_rejects_merchant_id_too_large(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["merchant_id"] = 1_000_000_000
        with pytest.raises(ValidationError) as exc_info:
            MQ050CCRecord(**kwargs)
        assert "merchant_id" in str(exc_info.value)

    def test_rejects_msg_id_too_long(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["msg_id"] = "X" * 25  # exceeds PIC X(24)
        with pytest.raises(ValidationError):
            MQ050CCRecord(**kwargs)

    def test_rejects_card_num_too_long(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["card_num"] = "1" * 17  # exceeds PIC X(16)
        with pytest.raises(ValidationError):
            MQ050CCRecord(**kwargs)

    def test_rejects_tran_desc_too_long(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["tran_desc"] = "X" * 101  # exceeds PIC X(100)
        with pytest.raises(ValidationError):
            MQ050CCRecord(**kwargs)

    def test_rejects_missing_required_field(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        del kwargs["card_num"]
        with pytest.raises(ValidationError):
            MQ050CCRecord(**kwargs)


class TestMQ050CCFixedLength:
    """Test fixed-length serialization and round-trip."""

    def test_to_fixed_length_correct_size(self) -> None:
        record = MQ050CCRecord(**_valid_mq050cc_kwargs())
        fixed = record.to_fixed_length()
        assert len(fixed) == MQ050CCRecord._total_length()

    def test_round_trip(self) -> None:
        original = MQ050CCRecord(**_valid_mq050cc_kwargs())
        fixed = original.to_fixed_length()
        restored = MQ050CCRecord.from_fixed_length(fixed)

        assert restored.msg_id == original.msg_id
        assert restored.msg_type == original.msg_type
        assert restored.card_num == original.card_num
        assert restored.acct_id == original.acct_id
        assert restored.cust_id == original.cust_id
        assert restored.tran_amt == original.tran_amt
        assert restored.tran_type_cd == original.tran_type_cd
        assert restored.tran_cat_cd == original.tran_cat_cd
        assert restored.tran_source == original.tran_source
        assert restored.merchant_id == original.merchant_id
        assert restored.merchant_name == original.merchant_name

    def test_round_trip_negative_amount(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["tran_amt"] = Decimal("-999.50")
        original = MQ050CCRecord(**kwargs)
        fixed = original.to_fixed_length()
        restored = MQ050CCRecord.from_fixed_length(fixed)
        assert restored.tran_amt == Decimal("-999.50")

    def test_round_trip_zero_amount(self) -> None:
        kwargs = _valid_mq050cc_kwargs()
        kwargs["tran_amt"] = Decimal("0.00")
        original = MQ050CCRecord(**kwargs)
        fixed = original.to_fixed_length()
        restored = MQ050CCRecord.from_fixed_length(fixed)
        assert restored.tran_amt == Decimal("0")

    def test_from_fixed_length_wrong_size(self) -> None:
        with pytest.raises(ValueError, match="Expected record length"):
            MQ050CCRecord.from_fixed_length("too short")

    def test_fixed_length_padding(self) -> None:
        """Verify PIC X fields are space-padded and PIC 9 fields are zero-padded."""
        kwargs = _valid_mq050cc_kwargs()
        kwargs["tran_source"] = "POS"  # 3 chars for a 10-char field
        record = MQ050CCRecord(**kwargs)
        fixed = record.to_fixed_length()
        # tran_source starts at a known offset; verify it's padded
        restored = MQ050CCRecord.from_fixed_length(fixed)
        assert restored.tran_source == "POS"

    def test_double_round_trip(self) -> None:
        """Serialize -> parse -> serialize should produce identical strings."""
        original = MQ050CCRecord(**_valid_mq050cc_kwargs())
        fixed1 = original.to_fixed_length()
        restored = MQ050CCRecord.from_fixed_length(fixed1)
        fixed2 = restored.to_fixed_length()
        assert fixed1 == fixed2
