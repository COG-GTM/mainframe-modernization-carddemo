"""MQ050CC — Input MQ message record layout for OE129BC card validation.

This model represents the input message structure sent to the OE129BC
validation program via MQ.  The record layout is based on typical mainframe
MQ message conventions for credit-card transaction validation requests,
using patterns from existing CardDemo copybooks (CVTRA05Y, CVACT01Y,
CVACT02Y, CVCUS01Y).

COBOL Record Layout (placeholder — 300 bytes):
    01  MQ050CC-RECORD.
        05  MQ050-MSG-ID                    PIC X(24).
        05  MQ050-MSG-TYPE                  PIC X(04).
        05  MQ050-TIMESTAMP                 PIC X(26).
        05  MQ050-CARD-NUM                  PIC X(16).
        05  MQ050-ACCT-ID                   PIC 9(11).
        05  MQ050-CUST-ID                   PIC 9(09).
        05  MQ050-TRAN-AMT                  PIC S9(09)V99.
        05  MQ050-TRAN-TYPE-CD              PIC X(02).
        05  MQ050-TRAN-CAT-CD              PIC 9(04).
        05  MQ050-TRAN-SOURCE               PIC X(10).
        05  MQ050-TRAN-DESC                 PIC X(100).
        05  MQ050-MERCHANT-ID               PIC 9(09).
        05  MQ050-MERCHANT-NAME             PIC X(50).
        05  MQ050-ORIG-TS                   PIC X(26).
        05  FILLER                          PIC X(03).

TODO: Update field definitions once the actual MQ050CC COBOL copybook is
      located or provided.  The layout above is a best-effort placeholder.
"""

from __future__ import annotations

from decimal import Decimal
from typing import ClassVar

from pydantic import BaseModel, Field, field_validator

from src.oe129bc.models.base import FieldSpec, FixedLengthRecordMixin


class MQ050CCRecord(FixedLengthRecordMixin, BaseModel):
    """Pydantic model for the MQ050CC input message record (300 bytes)."""

    FIELD_SPECS: ClassVar[list[FieldSpec]] = [
        FieldSpec("msg_id", "X", 24),
        FieldSpec("msg_type", "X", 4),
        FieldSpec("timestamp", "X", 26),
        FieldSpec("card_num", "X", 16),
        FieldSpec("acct_id", "9", 11),
        FieldSpec("cust_id", "9", 9),
        FieldSpec("tran_amt", "9", 9, decimals=2, signed=True),
        FieldSpec("tran_type_cd", "X", 2),
        FieldSpec("tran_cat_cd", "9", 4),
        FieldSpec("tran_source", "X", 10),
        FieldSpec("tran_desc", "X", 100),
        FieldSpec("merchant_id", "9", 9),
        FieldSpec("merchant_name", "X", 50),
        FieldSpec("orig_ts", "X", 26),
        FieldSpec("FILLER", "X", 3),
    ]

    # --- Message header fields ---
    msg_id: str = Field(
        ...,
        max_length=24,
        description="Unique message identifier (PIC X(24)).",
    )
    msg_type: str = Field(
        ...,
        max_length=4,
        description="Message type code (PIC X(04)).",
    )
    timestamp: str = Field(
        ...,
        max_length=26,
        description="Message timestamp (PIC X(26)).",
    )

    # --- Card / account fields ---
    card_num: str = Field(
        ...,
        max_length=16,
        description="Credit card number (PIC X(16)).",
    )
    acct_id: int = Field(
        ...,
        ge=0,
        description="Account identifier (PIC 9(11)).",
    )
    cust_id: int = Field(
        ...,
        ge=0,
        description="Customer identifier (PIC 9(09)).",
    )

    # --- Transaction fields ---
    tran_amt: Decimal = Field(
        ...,
        description="Transaction amount with 2-decimal precision (PIC S9(09)V99).",
    )
    tran_type_cd: str = Field(
        ...,
        max_length=2,
        description="Transaction type code (PIC X(02)).",
    )
    tran_cat_cd: int = Field(
        ...,
        ge=0,
        description="Transaction category code (PIC 9(04)).",
    )
    tran_source: str = Field(
        ...,
        max_length=10,
        description="Transaction source identifier (PIC X(10)).",
    )
    tran_desc: str = Field(
        ...,
        max_length=100,
        description="Transaction description (PIC X(100)).",
    )

    # --- Merchant fields ---
    merchant_id: int = Field(
        ...,
        ge=0,
        description="Merchant identifier (PIC 9(09)).",
    )
    merchant_name: str = Field(
        ...,
        max_length=50,
        description="Merchant name (PIC X(50)).",
    )

    # --- Trailing fields ---
    orig_ts: str = Field(
        ...,
        max_length=26,
        description="Original transaction timestamp (PIC X(26)).",
    )

    # ---- Validators ----

    @field_validator("acct_id")
    @classmethod
    def acct_id_max_digits(cls, v: int) -> int:
        if v > 99_999_999_999:
            raise ValueError("acct_id must fit within 11 digits (PIC 9(11))")
        return v

    @field_validator("cust_id")
    @classmethod
    def cust_id_max_digits(cls, v: int) -> int:
        if v > 999_999_999:
            raise ValueError("cust_id must fit within 9 digits (PIC 9(09))")
        return v

    @field_validator("tran_amt")
    @classmethod
    def tran_amt_precision(cls, v: Decimal) -> Decimal:
        int_part = int(abs(v))
        if int_part > 999_999_999:
            raise ValueError(
                "tran_amt integer portion must fit within 9 digits (PIC S9(09)V99)"
            )
        return v

    @field_validator("tran_cat_cd")
    @classmethod
    def tran_cat_cd_max_digits(cls, v: int) -> int:
        if v > 9999:
            raise ValueError("tran_cat_cd must fit within 4 digits (PIC 9(04))")
        return v

    @field_validator("merchant_id")
    @classmethod
    def merchant_id_max_digits(cls, v: int) -> int:
        if v > 999_999_999:
            raise ValueError("merchant_id must fit within 9 digits (PIC 9(09))")
        return v
