"""Pydantic model for CARD-XREF-RECORD (CVACT03Y.cpy — 50 bytes).

COBOL layout:
  01 CARD-XREF-RECORD.
    05 XREF-CARD-NUM  PIC X(16).
    05 XREF-CUST-ID   PIC 9(09).
    05 XREF-ACCT-ID   PIC 9(11).
    05 FILLER          PIC X(14).
"""

from pydantic import BaseModel, Field


class XrefRecord(BaseModel):
    """Card cross-reference record — maps to CVACT03Y.cpy (50-byte CARD-XREF-RECORD)."""

    xref_card_num: str = Field(
        ...,
        max_length=16,
        description="Card number — PIC X(16)",
    )
    xref_cust_id: str = Field(
        ...,
        min_length=9,
        max_length=9,
        pattern=r"^\d{9}$",
        description="Customer ID — PIC 9(09)",
    )
    xref_acct_id: str = Field(
        ...,
        min_length=11,
        max_length=11,
        pattern=r"^\d{11}$",
        description="Account ID — PIC 9(11)",
    )
