"""Pydantic models for OE129BC MQ message record layouts.

MQ050CC: Input message structure for card transaction validation requests.
MQ060CC: Output/response message structure for validation results.

Note: These models are based on typical MQ message structures for a credit card
validation system. The original COBOL copybooks (MQ050CC, MQ060CC) are not
present in the repository. Field layouts are derived from patterns observed in
existing copybooks (CVTRA05Y, CVACT01Y, CVCUS01Y, CVACT02Y) and standard
mainframe MQ message conventions for card-processing applications.
"""

from src.oe129bc.models.mq050cc import MQ050CCRecord
from src.oe129bc.models.mq060cc import MQ060CCRecord

__all__ = [
    "MQ050CCRecord",
    "MQ060CCRecord",
]
