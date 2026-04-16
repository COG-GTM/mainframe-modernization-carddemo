"""Pydantic model for CARD-RECORD (CVACT02Y.cpy — 150 bytes).

COBOL layout:
  01 CARD-RECORD.
    05 CARD-NUM              PIC X(16).
    05 CARD-ACCT-ID          PIC 9(11).
    05 CARD-CVV-CD           PIC 9(03).
    05 CARD-EMBOSSED-NAME    PIC X(50).
    05 CARD-EXPIRAION-DATE   PIC X(10).
    05 CARD-ACTIVE-STATUS    PIC X(01).
    05 FILLER                PIC X(59).
"""

from pydantic import BaseModel, Field


class CardRecord(BaseModel):
    """Card record — maps to CVACT02Y.cpy (150-byte CARD-RECORD)."""

    card_num: str = Field(
        ...,
        max_length=16,
        description="Card number — PIC X(16)",
    )
    card_acct_id: str = Field(
        ...,
        min_length=11,
        max_length=11,
        pattern=r"^\d{11}$",
        description="Account ID linked to this card — PIC 9(11)",
    )
    card_cvv_cd: str = Field(
        ...,
        min_length=3,
        max_length=3,
        pattern=r"^\d{3}$",
        description="CVV code — PIC 9(03)",
    )
    card_embossed_name: str = Field(
        ...,
        max_length=50,
        description="Embossed cardholder name — PIC X(50)",
    )
    card_expiration_date: str = Field(
        ...,
        max_length=10,
        description="Card expiration date — YYYY-MM-DD",
    )
    card_active_status: str = Field(
        ...,
        min_length=1,
        max_length=1,
        description="Active status flag (Y/N)",
    )
