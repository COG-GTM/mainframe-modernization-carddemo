"""Custom exception classes for VSAM file status errors.

Each exception maps to a specific VSAM file status code, preserving the
error semantics from the original COBOL/CICS programs in the CardDemo
application.
"""

from __future__ import annotations

from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from src.oe129bc.models.vsam_status import VsamStatusCode


class VsamError(Exception):
    """Base exception for all VSAM file operation errors.

    Attributes:
        status_code: The VSAM status code that triggered this error.
        message: Human-readable error description.
        file_name: Optional name of the VSAM file involved.
    """

    status_code: VsamStatusCode | None = None

    def __init__(
        self,
        message: str = "VSAM file operation error",
        file_name: str | None = None,
    ) -> None:
        self.message = message
        self.file_name = file_name
        detail = f"{message} (file={file_name})" if file_name else message
        super().__init__(detail)


class VsamSuccess(VsamError):
    """Raised to signal a successful VSAM operation (status '00').

    Typically not raised as an exception in practice, but included for
    completeness to allow uniform status-to-exception mapping.
    """

    def __init__(
        self,
        message: str = "Operation completed successfully",
        file_name: str | None = None,
    ) -> None:
        from src.oe129bc.models.vsam_status import VsamStatusCode

        self.status_code = VsamStatusCode.SUCCESS
        super().__init__(message, file_name)


class VsamDuplicateKeyError(VsamError):
    """VSAM status '02': Duplicate key detected on read."""

    def __init__(
        self,
        message: str = "Duplicate key detected; record read successfully",
        file_name: str | None = None,
    ) -> None:
        from src.oe129bc.models.vsam_status import VsamStatusCode

        self.status_code = VsamStatusCode.DUPLICATE_KEY
        super().__init__(message, file_name)


class VsamEndOfFileError(VsamError):
    """VSAM status '10': End of file reached during sequential read."""

    def __init__(
        self,
        message: str = "End of file reached during sequential read",
        file_name: str | None = None,
    ) -> None:
        from src.oe129bc.models.vsam_status import VsamStatusCode

        self.status_code = VsamStatusCode.END_OF_FILE
        super().__init__(message, file_name)


class VsamDuplicateKeyOnWriteError(VsamError):
    """VSAM status '22': Duplicate key on write attempt."""

    def __init__(
        self,
        message: str = "Duplicate key on write attempt",
        file_name: str | None = None,
    ) -> None:
        from src.oe129bc.models.vsam_status import VsamStatusCode

        self.status_code = VsamStatusCode.DUPLICATE_KEY_ON_WRITE
        super().__init__(message, file_name)


class VsamRecordNotFoundError(VsamError):
    """VSAM status '23': Record not found for the specified key."""

    def __init__(
        self,
        message: str = "Record not found for the specified key",
        file_name: str | None = None,
    ) -> None:
        from src.oe129bc.models.vsam_status import VsamStatusCode

        self.status_code = VsamStatusCode.RECORD_NOT_FOUND
        super().__init__(message, file_name)


class VsamFileNotFoundError(VsamError):
    """VSAM status '35': File not found or not available."""

    def __init__(
        self,
        message: str = "File not found or not available",
        file_name: str | None = None,
    ) -> None:
        from src.oe129bc.models.vsam_status import VsamStatusCode

        self.status_code = VsamStatusCode.FILE_NOT_FOUND
        super().__init__(message, file_name)


class VsamFileLockedError(VsamError):
    """VSAM status '97': File is locked by another process."""

    def __init__(
        self,
        message: str = "File is locked by another process",
        file_name: str | None = None,
    ) -> None:
        from src.oe129bc.models.vsam_status import VsamStatusCode

        self.status_code = VsamStatusCode.FILE_LOCKED
        super().__init__(message, file_name)


# Mapping from VsamStatusCode enum values to exception classes.
# Allows programmatic lookup: STATUS_CODE_EXCEPTION_MAP[code] → exception class.
def _build_status_exception_map() -> dict[str, type[VsamError]]:
    """Build mapping from status code values to exception classes."""
    from src.oe129bc.models.vsam_status import VsamStatusCode

    return {
        VsamStatusCode.SUCCESS.value: VsamSuccess,
        VsamStatusCode.DUPLICATE_KEY.value: VsamDuplicateKeyError,
        VsamStatusCode.END_OF_FILE.value: VsamEndOfFileError,
        VsamStatusCode.DUPLICATE_KEY_ON_WRITE.value: VsamDuplicateKeyOnWriteError,
        VsamStatusCode.RECORD_NOT_FOUND.value: VsamRecordNotFoundError,
        VsamStatusCode.FILE_NOT_FOUND.value: VsamFileNotFoundError,
        VsamStatusCode.FILE_LOCKED.value: VsamFileLockedError,
    }


def raise_for_status(
    status_code: str,
    file_name: str | None = None,
) -> None:
    """Raise the appropriate exception for a VSAM status code.

    Args:
        status_code: Two-character VSAM file status code string.
        file_name: Optional VSAM file name for error context.

    Raises:
        VsamError subclass corresponding to the status code, or
        VsamError with the raw code if unmapped.
    """
    mapping = _build_status_exception_map()
    exc_class = mapping.get(status_code)
    if exc_class is None:
        raise VsamError(
            message=f"Unknown VSAM status code: {status_code}",
            file_name=file_name,
        )
    if status_code == "00":
        return  # Success — no exception raised
    raise exc_class(file_name=file_name)
