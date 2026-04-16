"""Tests for error response Pydantic models."""

import json

import pytest

from src.oe129bc.models.error_response import ErrorResponse, FieldValidationError


class TestFieldValidationError:
    """Tests for the FieldValidationError model."""

    def test_creation(self) -> None:
        err = FieldValidationError(
            field_name="card_number",
            message="Card number must be 16 digits",
            code="INVALID_FORMAT",
        )
        assert err.field_name == "card_number"
        assert err.message == "Card number must be 16 digits"
        assert err.code == "INVALID_FORMAT"

    def test_default_code(self) -> None:
        err = FieldValidationError(
            field_name="account_id",
            message="Account ID is required",
        )
        assert err.code == "VALIDATION_ERROR"

    def test_serialization(self) -> None:
        err = FieldValidationError(
            field_name="expiry_date",
            message="Date is in the past",
            code="OUT_OF_RANGE",
        )
        data = err.model_dump()
        assert data == {
            "field_name": "expiry_date",
            "message": "Date is in the past",
            "code": "OUT_OF_RANGE",
        }

    def test_json_round_trip(self) -> None:
        err = FieldValidationError(
            field_name="amount",
            message="Amount exceeds limit",
        )
        json_str = err.model_dump_json()
        restored = FieldValidationError.model_validate_json(json_str)
        assert restored == err


class TestErrorResponse:
    """Tests for the ErrorResponse model."""

    def test_basic_creation(self) -> None:
        resp = ErrorResponse(
            code="VSAM_RECORD_NOT_FOUND",
            message="Record not found",
        )
        assert resp.code == "VSAM_RECORD_NOT_FOUND"
        assert resp.message == "Record not found"
        assert resp.field_name is None
        assert resp.details is None

    def test_with_field_name(self) -> None:
        resp = ErrorResponse(
            code="VALIDATION_ERROR",
            message="Invalid input",
            field_name="account_id",
        )
        assert resp.field_name == "account_id"

    def test_with_details(self) -> None:
        detail = FieldValidationError(
            field_name="card_number",
            message="Required field",
            code="REQUIRED_FIELD",
        )
        resp = ErrorResponse(
            code="VALIDATION_ERROR",
            message="Validation failed",
            details=[detail],
        )
        assert resp.details is not None
        assert len(resp.details) == 1
        assert resp.details[0].field_name == "card_number"

    def test_serialization_json(self) -> None:
        resp = ErrorResponse(
            code="VSAM_FILE_NOT_FOUND",
            message="File not found",
            field_name="data_file",
        )
        parsed = json.loads(resp.model_dump_json())
        assert parsed["code"] == "VSAM_FILE_NOT_FOUND"
        assert parsed["message"] == "File not found"
        assert parsed["field_name"] == "data_file"
        assert parsed["details"] is None

    def test_json_round_trip(self) -> None:
        resp = ErrorResponse(
            code="VSAM_FILE_LOCKED",
            message="File is locked",
            details=[
                FieldValidationError(
                    field_name="file",
                    message="Retry later",
                    code="LOCKED",
                ),
            ],
        )
        json_str = resp.model_dump_json()
        restored = ErrorResponse.model_validate_json(json_str)
        assert restored == resp


class TestErrorResponseFromVsamStatus:
    """Tests for the from_vsam_status factory method."""

    def test_known_status_code(self) -> None:
        resp = ErrorResponse.from_vsam_status("23")
        assert resp.code == "VSAM_RECORD_NOT_FOUND"
        assert "not found" in resp.message.lower()

    def test_known_status_with_custom_message(self) -> None:
        resp = ErrorResponse.from_vsam_status("35", message="ACCTFILE unavailable")
        assert resp.code == "VSAM_FILE_NOT_FOUND"
        assert resp.message == "ACCTFILE unavailable"

    def test_known_status_with_field_name(self) -> None:
        resp = ErrorResponse.from_vsam_status("23", field_name="account_id")
        assert resp.field_name == "account_id"

    def test_success_status(self) -> None:
        resp = ErrorResponse.from_vsam_status("00")
        assert resp.code == "VSAM_SUCCESS"
        assert "success" in resp.message.lower()

    def test_unknown_status_code(self) -> None:
        resp = ErrorResponse.from_vsam_status("88")
        assert "VSAM_UNKNOWN_88" in resp.code
        assert "88" in resp.message

    @pytest.mark.parametrize(
        ("status", "expected_code"),
        [
            ("00", "VSAM_SUCCESS"),
            ("02", "VSAM_DUPLICATE_KEY"),
            ("10", "VSAM_END_OF_FILE"),
            ("22", "VSAM_DUPLICATE_KEY_ON_WRITE"),
            ("23", "VSAM_RECORD_NOT_FOUND"),
            ("35", "VSAM_FILE_NOT_FOUND"),
            ("97", "VSAM_FILE_LOCKED"),
        ],
    )
    def test_all_known_codes_produce_correct_error_code(
        self, status: str, expected_code: str
    ) -> None:
        resp = ErrorResponse.from_vsam_status(status)
        assert resp.code == expected_code


class TestErrorResponseValidationError:
    """Tests for the validation_error factory method."""

    def test_single_error(self) -> None:
        errors = [
            FieldValidationError(
                field_name="card_number",
                message="Must be 16 digits",
            ),
        ]
        resp = ErrorResponse.validation_error(errors)
        assert resp.code == "VALIDATION_ERROR"
        assert resp.details is not None
        assert len(resp.details) == 1

    def test_multiple_errors(self) -> None:
        errors = [
            FieldValidationError(field_name="card_number", message="Required"),
            FieldValidationError(field_name="expiry_date", message="Invalid format"),
            FieldValidationError(field_name="cvv", message="Must be 3 digits"),
        ]
        resp = ErrorResponse.validation_error(errors)
        assert resp.details is not None
        assert len(resp.details) == 3

    def test_custom_message(self) -> None:
        resp = ErrorResponse.validation_error([], message="No valid fields")
        assert resp.message == "No valid fields"
