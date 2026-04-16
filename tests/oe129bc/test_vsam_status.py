"""Tests for VSAM status code enum."""

import pytest

from src.oe129bc.models.vsam_status import VsamStatusCode


class TestVsamStatusCodeValues:
    """Verify each status code maps to the correct COBOL status string."""

    def test_success_code(self) -> None:
        assert VsamStatusCode.SUCCESS.value == "00"

    def test_duplicate_key_code(self) -> None:
        assert VsamStatusCode.DUPLICATE_KEY.value == "02"

    def test_end_of_file_code(self) -> None:
        assert VsamStatusCode.END_OF_FILE.value == "10"

    def test_duplicate_key_on_write_code(self) -> None:
        assert VsamStatusCode.DUPLICATE_KEY_ON_WRITE.value == "22"

    def test_record_not_found_code(self) -> None:
        assert VsamStatusCode.RECORD_NOT_FOUND.value == "23"

    def test_file_not_found_code(self) -> None:
        assert VsamStatusCode.FILE_NOT_FOUND.value == "35"

    def test_file_locked_code(self) -> None:
        assert VsamStatusCode.FILE_LOCKED.value == "97"


class TestVsamStatusCodeLookup:
    """Verify status codes can be looked up by their string value."""

    @pytest.mark.parametrize(
        ("code_str", "expected"),
        [
            ("00", VsamStatusCode.SUCCESS),
            ("02", VsamStatusCode.DUPLICATE_KEY),
            ("10", VsamStatusCode.END_OF_FILE),
            ("22", VsamStatusCode.DUPLICATE_KEY_ON_WRITE),
            ("23", VsamStatusCode.RECORD_NOT_FOUND),
            ("35", VsamStatusCode.FILE_NOT_FOUND),
            ("97", VsamStatusCode.FILE_LOCKED),
        ],
    )
    def test_lookup_by_value(self, code_str: str, expected: VsamStatusCode) -> None:
        assert VsamStatusCode(code_str) is expected

    def test_invalid_code_raises_value_error(self) -> None:
        with pytest.raises(ValueError):
            VsamStatusCode("99")


class TestVsamStatusCodeDescription:
    """Verify each status code has a non-empty description."""

    @pytest.mark.parametrize("code", list(VsamStatusCode))
    def test_description_is_non_empty(self, code: VsamStatusCode) -> None:
        assert len(code.description) > 0

    def test_success_description(self) -> None:
        assert "success" in VsamStatusCode.SUCCESS.description.lower()

    def test_record_not_found_description(self) -> None:
        assert "not found" in VsamStatusCode.RECORD_NOT_FOUND.description.lower()


class TestVsamStatusCodeFlags:
    """Verify is_success and is_error properties."""

    def test_success_is_success(self) -> None:
        assert VsamStatusCode.SUCCESS.is_success is True
        assert VsamStatusCode.SUCCESS.is_error is False

    def test_duplicate_key_is_success(self) -> None:
        # Status '02' is a warning, not an error — record was read successfully
        assert VsamStatusCode.DUPLICATE_KEY.is_success is True
        assert VsamStatusCode.DUPLICATE_KEY.is_error is False

    @pytest.mark.parametrize(
        "code",
        [
            VsamStatusCode.END_OF_FILE,
            VsamStatusCode.DUPLICATE_KEY_ON_WRITE,
            VsamStatusCode.RECORD_NOT_FOUND,
            VsamStatusCode.FILE_NOT_FOUND,
            VsamStatusCode.FILE_LOCKED,
        ],
    )
    def test_error_codes_are_errors(self, code: VsamStatusCode) -> None:
        assert code.is_error is True
        assert code.is_success is False


class TestVsamStatusCodeIsStringEnum:
    """Verify VsamStatusCode is a str enum usable in string contexts."""

    def test_str_comparison(self) -> None:
        assert VsamStatusCode.SUCCESS == "00"

    def test_membership_count(self) -> None:
        assert len(VsamStatusCode) == 7
