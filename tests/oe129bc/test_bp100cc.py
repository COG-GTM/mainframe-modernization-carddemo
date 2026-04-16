"""Tests for BP100CC record layout model."""

from datetime import date
from decimal import Decimal

import pytest

from src.oe129bc.models.bp100cc import BP100CCRecord


class TestBP100CCRecordCreation:
    """Tests for creating BP100CC records."""

    def test_minimal_creation(self) -> None:
        record = BP100CCRecord(
            account_id="00000000001",
            card_number="4111111111111111",
            customer_id="000000001",
        )
        assert record.account_id == "00000000001"
        assert record.card_number == "4111111111111111"
        assert record.customer_id == "000000001"

    def test_default_values(self) -> None:
        record = BP100CCRecord(
            account_id="00000000001",
            card_number="4111111111111111",
            customer_id="000000001",
        )
        assert record.account_status == "A"
        assert record.credit_limit == Decimal("0.00")
        assert record.current_balance == Decimal("0.00")
        assert record.expiration_date is None
        assert record.open_date is None
        assert record.return_code == "00"
        assert record.error_message == ""

    def test_full_creation(self) -> None:
        record = BP100CCRecord(
            account_id="00000000042",
            card_number="5500000000000004",
            customer_id="000000099",
            account_status="A",
            credit_limit=Decimal("10000.00"),
            current_balance=Decimal("2500.75"),
            expiration_date=date(2027, 12, 31),
            open_date=date(2020, 1, 15),
            return_code="00",
            error_message="",
        )
        assert record.credit_limit == Decimal("10000.00")
        assert record.current_balance == Decimal("2500.75")
        assert record.expiration_date == date(2027, 12, 31)
        assert record.open_date == date(2020, 1, 15)


class TestBP100CCRecordValidation:
    """Tests for BP100CC field validation."""

    def test_account_id_max_length(self) -> None:
        with pytest.raises(Exception):
            BP100CCRecord(
                account_id="1" * 12,  # exceeds max_length=11
                card_number="4111111111111111",
                customer_id="000000001",
            )

    def test_card_number_max_length(self) -> None:
        with pytest.raises(Exception):
            BP100CCRecord(
                account_id="00000000001",
                card_number="1" * 17,  # exceeds max_length=16
                customer_id="000000001",
            )

    def test_customer_id_max_length(self) -> None:
        with pytest.raises(Exception):
            BP100CCRecord(
                account_id="00000000001",
                card_number="4111111111111111",
                customer_id="1" * 10,  # exceeds max_length=9
            )

    def test_credit_limit_non_negative(self) -> None:
        with pytest.raises(Exception):
            BP100CCRecord(
                account_id="00000000001",
                card_number="4111111111111111",
                customer_id="000000001",
                credit_limit=Decimal("-1.00"),
            )

    def test_account_status_values(self) -> None:
        for status in ("A", "C", "S"):
            record = BP100CCRecord(
                account_id="00000000001",
                card_number="4111111111111111",
                customer_id="000000001",
                account_status=status,
            )
            assert record.account_status == status

    def test_required_fields(self) -> None:
        with pytest.raises(Exception):
            BP100CCRecord()  # type: ignore[call-arg]


class TestBP100CCRecordSerialization:
    """Tests for BP100CC serialization and deserialization."""

    def test_dict_round_trip(self) -> None:
        record = BP100CCRecord(
            account_id="00000000001",
            card_number="4111111111111111",
            customer_id="000000001",
            credit_limit=Decimal("5000.00"),
            current_balance=Decimal("-150.25"),
        )
        data = record.model_dump()
        restored = BP100CCRecord(**data)
        assert restored == record

    def test_json_round_trip(self) -> None:
        record = BP100CCRecord(
            account_id="00000000001",
            card_number="4111111111111111",
            customer_id="000000001",
            expiration_date=date(2027, 6, 30),
        )
        json_str = record.model_dump_json()
        restored = BP100CCRecord.model_validate_json(json_str)
        assert restored == record

    def test_json_schema_generation(self) -> None:
        schema = BP100CCRecord.model_json_schema()
        assert "properties" in schema
        assert "account_id" in schema["properties"]
        assert "card_number" in schema["properties"]
        assert "customer_id" in schema["properties"]
