"""Tests for VSAM exception classes and status-to-exception mapping."""

import pytest

from src.oe129bc.models.exceptions import (
    VsamDuplicateKeyError,
    VsamDuplicateKeyOnWriteError,
    VsamEndOfFileError,
    VsamError,
    VsamFileLockedError,
    VsamFileNotFoundError,
    VsamRecordNotFoundError,
    VsamSuccess,
    raise_for_status,
)
from src.oe129bc.models.vsam_status import VsamStatusCode


class TestExceptionHierarchy:
    """Verify all VSAM exceptions inherit from VsamError."""

    @pytest.mark.parametrize(
        "exc_class",
        [
            VsamSuccess,
            VsamDuplicateKeyError,
            VsamEndOfFileError,
            VsamDuplicateKeyOnWriteError,
            VsamRecordNotFoundError,
            VsamFileNotFoundError,
            VsamFileLockedError,
        ],
    )
    def test_subclass_of_vsam_error(self, exc_class: type[VsamError]) -> None:
        assert issubclass(exc_class, VsamError)

    def test_vsam_error_is_exception(self) -> None:
        assert issubclass(VsamError, Exception)


class TestExceptionStatusCodes:
    """Verify each exception carries the correct status code."""

    def test_success_status_code(self) -> None:
        exc = VsamSuccess()
        assert exc.status_code == VsamStatusCode.SUCCESS

    def test_duplicate_key_status_code(self) -> None:
        exc = VsamDuplicateKeyError()
        assert exc.status_code == VsamStatusCode.DUPLICATE_KEY

    def test_end_of_file_status_code(self) -> None:
        exc = VsamEndOfFileError()
        assert exc.status_code == VsamStatusCode.END_OF_FILE

    def test_duplicate_key_on_write_status_code(self) -> None:
        exc = VsamDuplicateKeyOnWriteError()
        assert exc.status_code == VsamStatusCode.DUPLICATE_KEY_ON_WRITE

    def test_record_not_found_status_code(self) -> None:
        exc = VsamRecordNotFoundError()
        assert exc.status_code == VsamStatusCode.RECORD_NOT_FOUND

    def test_file_not_found_status_code(self) -> None:
        exc = VsamFileNotFoundError()
        assert exc.status_code == VsamStatusCode.FILE_NOT_FOUND

    def test_file_locked_status_code(self) -> None:
        exc = VsamFileLockedError()
        assert exc.status_code == VsamStatusCode.FILE_LOCKED


class TestExceptionMessages:
    """Verify exception message formatting."""

    def test_default_message(self) -> None:
        exc = VsamRecordNotFoundError()
        assert "not found" in str(exc).lower()

    def test_custom_message(self) -> None:
        exc = VsamRecordNotFoundError(message="Account 12345 missing")
        assert "Account 12345 missing" in str(exc)

    def test_file_name_in_message(self) -> None:
        exc = VsamRecordNotFoundError(file_name="ACCTFILE")
        assert "ACCTFILE" in str(exc)

    def test_file_name_attribute(self) -> None:
        exc = VsamFileNotFoundError(file_name="CUSTFILE")
        assert exc.file_name == "CUSTFILE"

    def test_message_attribute(self) -> None:
        exc = VsamFileLockedError(message="Custom lock msg")
        assert exc.message == "Custom lock msg"

    def test_no_file_name_message(self) -> None:
        exc = VsamEndOfFileError()
        # Without file_name, the message should not contain "(file="
        assert "(file=" not in str(exc)


class TestExceptionCatching:
    """Verify exceptions can be caught by base class."""

    def test_catch_specific_as_base(self) -> None:
        with pytest.raises(VsamError):
            raise VsamRecordNotFoundError()

    def test_catch_specific_as_exception(self) -> None:
        with pytest.raises(Exception):
            raise VsamFileLockedError()

    def test_catch_specific_type(self) -> None:
        with pytest.raises(VsamFileNotFoundError):
            raise VsamFileNotFoundError(file_name="TRANFILE")


class TestRaiseForStatus:
    """Verify the raise_for_status helper function."""

    def test_success_does_not_raise(self) -> None:
        # Status '00' should not raise — it's success
        raise_for_status("00")

    def test_record_not_found_raises(self) -> None:
        with pytest.raises(VsamRecordNotFoundError):
            raise_for_status("23")

    def test_file_not_found_raises(self) -> None:
        with pytest.raises(VsamFileNotFoundError):
            raise_for_status("35")

    def test_end_of_file_raises(self) -> None:
        with pytest.raises(VsamEndOfFileError):
            raise_for_status("10")

    def test_duplicate_key_raises(self) -> None:
        with pytest.raises(VsamDuplicateKeyError):
            raise_for_status("02")

    def test_duplicate_key_on_write_raises(self) -> None:
        with pytest.raises(VsamDuplicateKeyOnWriteError):
            raise_for_status("22")

    def test_file_locked_raises(self) -> None:
        with pytest.raises(VsamFileLockedError):
            raise_for_status("97")

    def test_unknown_code_raises_base_error(self) -> None:
        with pytest.raises(VsamError, match="Unknown VSAM status code: 99"):
            raise_for_status("99")

    def test_file_name_passed_through(self) -> None:
        with pytest.raises(VsamRecordNotFoundError) as exc_info:
            raise_for_status("23", file_name="CARDFILE")
        assert exc_info.value.file_name == "CARDFILE"
