"""Pydantic model for the TRAN-RECORD layout from CVTRA05Y.cpy (350 bytes).

Field layout (COBOL copybook CVTRA05Y.cpy):
    01  TRAN-RECORD.
        05  TRAN-ID             PIC X(16).       -- Primary key, sequential
        05  TRAN-TYPE-CD        PIC X(02).       -- Transaction type code
        05  TRAN-CAT-CD         PIC 9(04).       -- Category code (numeric)
        05  TRAN-SOURCE         PIC X(10).       -- Source identifier
        05  TRAN-DESC           PIC X(100).      -- Description
        05  TRAN-AMT            PIC S9(09)V99.   -- Signed decimal amount
        05  TRAN-MERCHANT-ID    PIC 9(09).       -- Merchant identifier
        05  TRAN-MERCHANT-NAME  PIC X(50).       -- Merchant name
        05  TRAN-MERCHANT-CITY  PIC X(50).       -- Merchant city
        05  TRAN-MERCHANT-ZIP   PIC X(10).       -- Merchant zip
        05  TRAN-CARD-NUM       PIC X(16).       -- Card number
        05  TRAN-ORIG-TS        PIC X(26).       -- Origination timestamp
        05  TRAN-PROC-TS        PIC X(26).       -- Processing timestamp
        05  FILLER              PIC X(20).       -- Reserved
    Total: 350 bytes
"""

from decimal import Decimal
from typing import ClassVar

from pydantic import BaseModel, Field


class FieldSpec:
    """Describes a fixed-length COBOL field: name, offset, length, and type."""

    def __init__(self, name: str, offset: int, length: int, field_type: str = "text") -> None:
        self.name = name
        self.offset = offset
        self.length = length
        self.field_type = field_type  # "text", "numeric", "signed_decimal"


# Field layout matching CVTRA05Y.cpy byte offsets exactly
TRAN_RECORD_FIELDS: list[FieldSpec] = [
    FieldSpec("tran_id", 0, 16, "text"),
    FieldSpec("tran_type_cd", 16, 2, "text"),
    FieldSpec("tran_cat_cd", 18, 4, "numeric"),
    FieldSpec("tran_source", 22, 10, "text"),
    FieldSpec("tran_desc", 32, 100, "text"),
    FieldSpec("tran_amt", 132, 11, "signed_decimal"),
    FieldSpec("tran_merchant_id", 143, 9, "numeric"),
    FieldSpec("tran_merchant_name", 152, 50, "text"),
    FieldSpec("tran_merchant_city", 202, 50, "text"),
    FieldSpec("tran_merchant_zip", 252, 10, "text"),
    FieldSpec("tran_card_num", 262, 16, "text"),
    FieldSpec("tran_orig_ts", 278, 26, "text"),
    FieldSpec("tran_proc_ts", 304, 26, "text"),
    FieldSpec("filler", 330, 20, "text"),
]

RECORD_LENGTH = 350


class TransactionRecord(BaseModel):
    """Pydantic model for the TRAN-RECORD (CVTRA05Y.cpy, 350 bytes).

    All text fields are stored stripped of trailing whitespace.
    The amount field uses Decimal for exact arithmetic (no floating-point).
    """

    tran_id: str = Field(max_length=16, description="Primary key, sequential (PIC X(16))")
    tran_type_cd: str = Field(max_length=2, description="Transaction type code (PIC X(02))")
    tran_cat_cd: str = Field(max_length=4, description="Category code, numeric (PIC 9(04))")
    tran_source: str = Field(max_length=10, description="Source identifier (PIC X(10))")
    tran_desc: str = Field(max_length=100, description="Description (PIC X(100))")
    tran_amt: Decimal = Field(description="Signed decimal amount (PIC S9(09)V99)")
    tran_merchant_id: str = Field(max_length=9, description="Merchant identifier, numeric (PIC 9(09))")
    tran_merchant_name: str = Field(max_length=50, description="Merchant name (PIC X(50))")
    tran_merchant_city: str = Field(max_length=50, description="Merchant city (PIC X(50))")
    tran_merchant_zip: str = Field(max_length=10, description="Merchant zip (PIC X(10))")
    tran_card_num: str = Field(max_length=16, description="Card number (PIC X(16))")
    tran_orig_ts: str = Field(max_length=26, description="Origination timestamp (PIC X(26))")
    tran_proc_ts: str = Field(max_length=26, description="Processing timestamp (PIC X(26))")
    filler: str = Field(default="", max_length=20, description="Reserved (PIC X(20))")

    RECORD_LENGTH: ClassVar[int] = RECORD_LENGTH
    FIELDS: ClassVar[list[FieldSpec]] = TRAN_RECORD_FIELDS

    model_config = {"frozen": False, "str_strip_whitespace": True}
