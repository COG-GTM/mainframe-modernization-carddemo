"""VSAM file status code mapping for the OE129BC validation program port.

Maps common VSAM/COBOL file status codes to descriptive enum members.
These codes originate from mainframe VSAM file I/O operations and are
used throughout the CardDemo application (e.g., CBTRN01C, COACTUPC).
"""

from enum import Enum


class VsamStatusCode(str, Enum):
    """VSAM file status codes mapped from COBOL FILE STATUS values.

    Each member corresponds to a two-character COBOL file status code.
    The value is the original COBOL status string; the name describes
    the condition.
    """

    SUCCESS = "00"
    DUPLICATE_KEY = "02"
    END_OF_FILE = "10"
    DUPLICATE_KEY_ON_WRITE = "22"
    RECORD_NOT_FOUND = "23"
    FILE_NOT_FOUND = "35"
    FILE_LOCKED = "97"

    @property
    def description(self) -> str:
        """Human-readable description of the status code."""
        descriptions: dict[str, str] = {
            "00": "Operation completed successfully",
            "02": "Duplicate key detected; record read successfully",
            "10": "End of file reached during sequential read",
            "22": "Duplicate key on write attempt",
            "23": "Record not found for the specified key",
            "35": "File not found or not available",
            "97": "File is locked by another process",
        }
        return descriptions[self.value]

    @property
    def is_success(self) -> bool:
        """Return True if this status indicates a successful operation."""
        return self in (VsamStatusCode.SUCCESS, VsamStatusCode.DUPLICATE_KEY)

    @property
    def is_error(self) -> bool:
        """Return True if this status indicates an error condition."""
        return not self.is_success
