"""Standardized error response models for the OE129BC validation program port.

These Pydantic models provide a uniform structure for reporting errors
back to callers, whether from VSAM file operations or field-level
validation failures.
"""

from __future__ import annotations

from pydantic import BaseModel, Field


class FieldValidationError(BaseModel):
    """A single field-level validation error.

    Mirrors the field-validation pattern used in the original COBOL
    CICS screens (e.g., highlighted fields with error attributes).

    Attributes:
        field_name: The name of the field that failed validation.
        message: Human-readable description of the validation failure.
        code: Machine-readable error code for programmatic handling.
    """

    field_name: str = Field(
        ...,
        description="Name of the field that failed validation",
        examples=["card_number"],
    )
    message: str = Field(
        ...,
        description="Human-readable validation error message",
        examples=["Card number must be exactly 16 digits"],
    )
    code: str = Field(
        default="VALIDATION_ERROR",
        description="Machine-readable error code",
        examples=["INVALID_FORMAT", "REQUIRED_FIELD", "OUT_OF_RANGE"],
    )


class ErrorResponse(BaseModel):
    """Standardized error response returned by the OE129BC port.

    Provides a consistent envelope for all error conditions including
    VSAM I/O errors, validation failures, and application-level errors.

    Attributes:
        code: Application-level error code string.
        message: Top-level human-readable error description.
        field_name: Optional field name when the error relates to a
            specific input field.
        details: Optional list of field-level validation errors for
            requests that fail multiple validations.
    """

    code: str = Field(
        ...,
        description="Application-level error code",
        examples=["VSAM_RECORD_NOT_FOUND", "VALIDATION_ERROR"],
    )
    message: str = Field(
        ...,
        description="Human-readable error description",
        examples=["Record not found for the specified key"],
    )
    field_name: str | None = Field(
        default=None,
        description="Field name associated with the error, if applicable",
        examples=["account_id"],
    )
    details: list[FieldValidationError] | None = Field(
        default=None,
        description="Field-level validation errors, if applicable",
    )

    @classmethod
    def from_vsam_status(
        cls,
        status_code: str,
        message: str | None = None,
        field_name: str | None = None,
    ) -> ErrorResponse:
        """Create an ErrorResponse from a VSAM status code string.

        Args:
            status_code: Two-character VSAM file status code.
            message: Optional override message. If not provided, the
                default description for the status code is used.
            field_name: Optional field name context.

        Returns:
            An ErrorResponse populated from the VSAM status code.
        """
        from src.oe129bc.models.vsam_status import VsamStatusCode

        try:
            vsam_code = VsamStatusCode(status_code)
            error_message = message or vsam_code.description
            code = f"VSAM_{vsam_code.name}"
        except ValueError:
            error_message = message or f"Unknown VSAM status: {status_code}"
            code = f"VSAM_UNKNOWN_{status_code}"

        return cls(
            code=code,
            message=error_message,
            field_name=field_name,
        )

    @classmethod
    def validation_error(
        cls,
        errors: list[FieldValidationError],
        message: str = "One or more validation errors occurred",
    ) -> ErrorResponse:
        """Create an ErrorResponse wrapping multiple field validation errors.

        Args:
            errors: List of field-level validation errors.
            message: Top-level summary message.

        Returns:
            An ErrorResponse with the details populated.
        """
        return cls(
            code="VALIDATION_ERROR",
            message=message,
            details=errors,
        )
