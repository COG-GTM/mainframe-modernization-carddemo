"""MQ060CC — Output MQ message record layout for OE129BC card validation.

This model represents the output/response message structure returned by the
OE129BC validation program via MQ.  The record layout is based on typical
mainframe MQ message conventions for credit-card transaction validation
responses, using patterns from existing CardDemo copybooks.

COBOL Record Layout (placeholder — 300 bytes):
    01  MQ060CC-RECORD.
        05  MQ060-MSG-ID                    PIC X(24).
        05  MQ060-MSG-TYPE                  PIC X(04).
        05  MQ060-TIMESTAMP                 PIC X(26).
        05  MQ060-ORIG-MSG-ID               PIC X(24).
        05  MQ060-CARD-NUM                  PIC X(16).
        05  MQ060-ACCT-ID                   PIC 9(11).
        05  MQ060-CUST-ID                   PIC 9(09).
        05  MQ060-TRAN-AMT                  PIC S9(09)V99.
        05  MQ060-RESP-CD                   PIC X(04).
        05  MQ060-RESP-REASON-CD            PIC X(04).
        05  MQ060-RESP-MSG                  PIC X(80).
        05  MQ060-AUTH-CD                   PIC X(06).
        05  MQ060-AVL-BAL                   PIC S9(10)V99.
        05  MQ060-CREDIT-LIMIT              PIC S9(10)V99.
        05  MQ060-ACCT-STATUS               PIC X(01).
        05  MQ060-CARD-STATUS               PIC X(01).
        05  MQ060-PROC-TS                   PIC X(26).
        05  FILLER                          PIC X(21).

TODO: Update field definitions once the actual MQ060CC COBOL copybook is
      located or provided.  The layout above is a best-effort placeholder.
"""

from __future__ import annotations

from decimal import Decimal
from typing import ClassVar

from pydantic import BaseModel, Field, field_validator

from src.oe129bc.models.base import FieldSpec, FixedLengthRecordMixin


class MQ060CCRecord(FixedLengthRecordMixin, BaseModel):
    """Pydantic model for the MQ060CC output/response message record (300 bytes)."""

    FIELD_SPECS: ClassVar[list[FieldSpec]] = [
        FieldSpec("msg_id", "X", 24),
        FieldSpec("msg_type", "X", 4),
        FieldSpec("timestamp", "X", 26),
        FieldSpec("orig_msg_id", "X", 24),
        FieldSpec("card_num", "X", 16),
        FieldSpec("acct_id", "9", 11),
        FieldSpec("cust_id", "9", 9),
        FieldSpec("tran_amt", "9", 9, decimals=2, signed=True),
        FieldSpec("resp_cd", "X", 4),
        FieldSpec("resp_reason_cd", "X", 4),
        FieldSpec("resp_msg", "X", 80),
        FieldSpec("auth_cd", "X", 6),
        FieldSpec("avl_bal", "9", 10, decimals=2, signed=True),
        FieldSpec("credit_limit", "9", 10, decimals=2, signed=True),
        FieldSpec("acct_status", "X", 1),
        FieldSpec("card_status", "X", 1),
        FieldSpec("proc_ts", "X", 26),
        FieldSpec("FILLER", "X", 21),
    ]

    # --- Message header fields ---
    msg_id: str = Field(
        ...,
        max_length=24,
        description="Unique response message identifier (PIC X(24)).",
    )
    msg_type: str = Field(
        ...,
        max_length=4,
        description="Message type code (PIC X(04)).",
    )
    timestamp: str = Field(
        ...,
        max_length=26,
        description="Response message timestamp (PIC X(26)).",
    )
    orig_msg_id: str = Field(
        ...,
        max_length=24,
        description="Original request message ID for correlation (PIC X(24)).",
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
    tran_amt: Decimal = Field(
        ...,
        description="Transaction amount with 2-decimal precision (PIC S9(09)V99).",
    )

    # --- Validation response fields ---
    resp_cd: str = Field(
        ...,
        max_length=4,
        description="Response code indicating approval/decline (PIC X(04)).",
    )
    resp_reason_cd: str = Field(
        ...,
        max_length=4,
        description="Reason code for the response (PIC X(04)).",
    )
    resp_msg: str = Field(
        ...,
        max_length=80,
        description="Human-readable response message (PIC X(80)).",
    )
    auth_cd: str = Field(
        ...,
        max_length=6,
        description="Authorization code for approved transactions (PIC X(06)).",
    )

    # --- Account status fields ---
    avl_bal: Decimal = Field(
        ...,
        description="Available balance after transaction (PIC S9(10)V99).",
    )
    credit_limit: Decimal = Field(
        ...,
        description="Account credit limit (PIC S9(10)V99).",
    )
    acct_status: str = Field(
        ...,
        max_length=1,
        description="Account active status flag (PIC X(01)).",
    )
    card_status: str = Field(
        ...,
        max_length=1,
        description="Card active status flag (PIC X(01)).",
    )

    # --- Trailing fields ---
    proc_ts: str = Field(
        ...,
        max_length=26,
        description="Processing timestamp (PIC X(26)).",
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

    @field_validator("avl_bal")
    @classmethod
    def avl_bal_precision(cls, v: Decimal) -> Decimal:
        int_part = int(abs(v))
        if int_part > 9_999_999_999:
            raise ValueError(
                "avl_bal integer portion must fit within 10 digits (PIC S9(10)V99)"
            )
        return v

    @field_validator("credit_limit")
    @classmethod
    def credit_limit_precision(cls, v: Decimal) -> Decimal:
        int_part = int(abs(v))
        if int_part > 9_999_999_999:
            raise ValueError(
                "credit_limit integer portion must fit within 10 digits "
                "(PIC S9(10)V99)"
            )
        return v
