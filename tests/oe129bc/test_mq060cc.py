"""Tests for MQ060CC output/response message record model."""

from __future__ import annotations

from decimal import Decimal

import pytest
from pydantic import ValidationError

from src.oe129bc.models.mq060cc import MQ060CCRecord


def _valid_mq060cc_kwargs() -> dict:
    """Return a dict of valid keyword arguments for MQ060CCRecord."""
    return {
        "msg_id": "RSP00000000000000000001",
        "msg_type": "TRSP",
        "timestamp": "2024-01-15T10:30:01.000000",
        "orig_msg_id": "MSG00000000000000000001",
        "card_num": "4111111111111111",
        "acct_id": 12345678901,
        "cust_id": 123456789,
        "tran_amt": Decimal("150.75"),
        "resp_cd": "APPR",
        "resp_reason_cd": "0000",
        "resp_msg": "TRANSACTION APPROVED",
        "auth_cd": "A12345",
        "avl_bal": Decimal("4849.25"),
        "credit_limit": Decimal("5000.00"),
        "acct_status": "Y",
        "card_status": "Y",
        "proc_ts": "2024-01-15T10:30:01.000000",
    }


class TestMQ060CCCreation:
    """Test creation with valid data."""

    def test_create_with_valid_data(self) -> None:
        record = MQ060CCRecord(**_valid_mq060cc_kwargs())
        assert record.msg_id == "RSP00000000000000000001"
        assert record.msg_type == "TRSP"
        assert record.orig_msg_id == "MSG00000000000000000001"
        assert record.card_num == "4111111111111111"
        assert record.acct_id == 12345678901
        assert record.cust_id == 123456789
        assert record.tran_amt == Decimal("150.75")
        assert record.resp_cd == "APPR"
        assert record.resp_reason_cd == "0000"
        assert record.resp_msg == "TRANSACTION APPROVED"
        assert record.auth_cd == "A12345"
        assert record.avl_bal == Decimal("4849.25")
        assert record.credit_limit == Decimal("5000.00")
        assert record.acct_status == "Y"
        assert record.card_status == "Y"

    def test_create_with_zero_balances(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["tran_amt"] = Decimal("0.00")
        kwargs["avl_bal"] = Decimal("0.00")
        kwargs["credit_limit"] = Decimal("0.00")
        record = MQ060CCRecord(**kwargs)
        assert record.tran_amt == Decimal("0.00")
        assert record.avl_bal == Decimal("0.00")
        assert record.credit_limit == Decimal("0.00")

    def test_create_with_negative_balance(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["avl_bal"] = Decimal("-200.50")
        record = MQ060CCRecord(**kwargs)
        assert record.avl_bal == Decimal("-200.50")

    def test_create_with_max_valid_values(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["acct_id"] = 99_999_999_999
        kwargs["cust_id"] = 999_999_999
        kwargs["tran_amt"] = Decimal("999999999.99")
        kwargs["avl_bal"] = Decimal("9999999999.99")
        kwargs["credit_limit"] = Decimal("9999999999.99")
        record = MQ060CCRecord(**kwargs)
        assert record.acct_id == 99_999_999_999
        assert record.avl_bal == Decimal("9999999999.99")

    def test_create_decline_response(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["resp_cd"] = "DECL"
        kwargs["resp_reason_cd"] = "INSF"
        kwargs["resp_msg"] = "INSUFFICIENT FUNDS"
        kwargs["auth_cd"] = ""
        record = MQ060CCRecord(**kwargs)
        assert record.resp_cd == "DECL"
        assert record.resp_reason_cd == "INSF"


class TestMQ060CCValidation:
    """Test that validation rejects invalid data."""

    def test_rejects_acct_id_too_large(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["acct_id"] = 100_000_000_000
        with pytest.raises(ValidationError) as exc_info:
            MQ060CCRecord(**kwargs)
        assert "acct_id" in str(exc_info.value)

    def test_rejects_negative_acct_id(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["acct_id"] = -1
        with pytest.raises(ValidationError):
            MQ060CCRecord(**kwargs)

    def test_rejects_cust_id_too_large(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["cust_id"] = 1_000_000_000
        with pytest.raises(ValidationError) as exc_info:
            MQ060CCRecord(**kwargs)
        assert "cust_id" in str(exc_info.value)

    def test_rejects_tran_amt_too_large(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["tran_amt"] = Decimal("9999999999.99")
        with pytest.raises(ValidationError) as exc_info:
            MQ060CCRecord(**kwargs)
        assert "tran_amt" in str(exc_info.value)

    def test_rejects_avl_bal_too_large(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["avl_bal"] = Decimal("99999999999.99")
        with pytest.raises(ValidationError) as exc_info:
            MQ060CCRecord(**kwargs)
        assert "avl_bal" in str(exc_info.value)

    def test_rejects_credit_limit_too_large(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["credit_limit"] = Decimal("99999999999.99")
        with pytest.raises(ValidationError) as exc_info:
            MQ060CCRecord(**kwargs)
        assert "credit_limit" in str(exc_info.value)

    def test_rejects_msg_id_too_long(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["msg_id"] = "X" * 25
        with pytest.raises(ValidationError):
            MQ060CCRecord(**kwargs)

    def test_rejects_resp_msg_too_long(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["resp_msg"] = "X" * 81
        with pytest.raises(ValidationError):
            MQ060CCRecord(**kwargs)

    def test_rejects_acct_status_too_long(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["acct_status"] = "YY"
        with pytest.raises(ValidationError):
            MQ060CCRecord(**kwargs)

    def test_rejects_missing_required_field(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        del kwargs["resp_cd"]
        with pytest.raises(ValidationError):
            MQ060CCRecord(**kwargs)


class TestMQ060CCFixedLength:
    """Test fixed-length serialization and round-trip."""

    def test_to_fixed_length_correct_size(self) -> None:
        record = MQ060CCRecord(**_valid_mq060cc_kwargs())
        fixed = record.to_fixed_length()
        assert len(fixed) == MQ060CCRecord._total_length()

    def test_round_trip(self) -> None:
        original = MQ060CCRecord(**_valid_mq060cc_kwargs())
        fixed = original.to_fixed_length()
        restored = MQ060CCRecord.from_fixed_length(fixed)

        assert restored.msg_id == original.msg_id
        assert restored.msg_type == original.msg_type
        assert restored.orig_msg_id == original.orig_msg_id
        assert restored.card_num == original.card_num
        assert restored.acct_id == original.acct_id
        assert restored.cust_id == original.cust_id
        assert restored.tran_amt == original.tran_amt
        assert restored.resp_cd == original.resp_cd
        assert restored.resp_reason_cd == original.resp_reason_cd
        assert restored.resp_msg == original.resp_msg
        assert restored.auth_cd == original.auth_cd
        assert restored.avl_bal == original.avl_bal
        assert restored.credit_limit == original.credit_limit
        assert restored.acct_status == original.acct_status
        assert restored.card_status == original.card_status
        assert restored.proc_ts == original.proc_ts

    def test_round_trip_negative_balance(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["avl_bal"] = Decimal("-1500.25")
        kwargs["tran_amt"] = Decimal("-75.00")
        original = MQ060CCRecord(**kwargs)
        fixed = original.to_fixed_length()
        restored = MQ060CCRecord.from_fixed_length(fixed)
        assert restored.avl_bal == Decimal("-1500.25")
        assert restored.tran_amt == Decimal("-75.00")

    def test_round_trip_zero_values(self) -> None:
        kwargs = _valid_mq060cc_kwargs()
        kwargs["tran_amt"] = Decimal("0.00")
        kwargs["avl_bal"] = Decimal("0.00")
        kwargs["credit_limit"] = Decimal("0.00")
        kwargs["acct_id"] = 0
        kwargs["cust_id"] = 0
        original = MQ060CCRecord(**kwargs)
        fixed = original.to_fixed_length()
        restored = MQ060CCRecord.from_fixed_length(fixed)
        assert restored.acct_id == 0
        assert restored.cust_id == 0
        assert restored.tran_amt == Decimal("0")
        assert restored.avl_bal == Decimal("0")

    def test_from_fixed_length_wrong_size(self) -> None:
        with pytest.raises(ValueError, match="Expected record length"):
            MQ060CCRecord.from_fixed_length("too short")

    def test_double_round_trip(self) -> None:
        """Serialize -> parse -> serialize should produce identical strings."""
        original = MQ060CCRecord(**_valid_mq060cc_kwargs())
        fixed1 = original.to_fixed_length()
        restored = MQ060CCRecord.from_fixed_length(fixed1)
        fixed2 = restored.to_fixed_length()
        assert fixed1 == fixed2

    def test_fixed_length_padding_spaces(self) -> None:
        """Verify short string fields are space-padded on the right."""
        kwargs = _valid_mq060cc_kwargs()
        kwargs["resp_msg"] = "OK"  # 2 chars in an 80-char field
        record = MQ060CCRecord(**kwargs)
        fixed = record.to_fixed_length()
        restored = MQ060CCRecord.from_fixed_length(fixed)
        assert restored.resp_msg == "OK"
