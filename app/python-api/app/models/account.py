"""Pydantic model for ACCT-RECORD (CVACT01Y.cpy — 300 bytes).

COBOL layout:
  01 ACCT-RECORD.
    05 ACCT-ID                PIC 9(11).        -- bytes 1-11
    05 ACCT-ACTIVE-STATUS     PIC X(01).        -- byte 12 (Y/N)
    05 ACCT-CURR-BAL          PIC S9(10)V99.    -- BigDecimal scale 2
    05 ACCT-CREDIT-LIMIT      PIC S9(10)V99.
    05 ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99.
    05 ACCT-OPEN-DATE         PIC X(10).        -- YYYY-MM-DD
    05 ACCT-EXPIRAION-DATE    PIC X(10).
    05 ACCT-REISSUE-DATE      PIC X(10).
    05 ACCT-CURR-CYC-CREDIT   PIC S9(10)V99.
    05 ACCT-CURR-CYC-DEBIT    PIC S9(10)V99.
    05 ACCT-ADDR-ZIP          PIC X(10).
    05 ACCT-GROUP-ID          PIC X(10).
    05 FILLER                 PIC X(178).
"""

from decimal import Decimal

from pydantic import BaseModel, Field


class AccountRecord(BaseModel):
    """Account master record — maps to CVACT01Y.cpy (300-byte ACCT-RECORD)."""

    acct_id: str = Field(
        ...,
        min_length=11,
        max_length=11,
        pattern=r"^\d{11}$",
        description="Account ID — 11-digit numeric string",
    )
    acct_active_status: str = Field(
        ...,
        min_length=1,
        max_length=1,
        description="Active status flag (Y/N)",
    )
    acct_curr_bal: Decimal = Field(
        ...,
        max_digits=12,
        decimal_places=2,
        description="Current balance — PIC S9(10)V99",
    )
    acct_credit_limit: Decimal = Field(
        ...,
        max_digits=12,
        decimal_places=2,
        description="Credit limit — PIC S9(10)V99",
    )
    acct_cash_credit_limit: Decimal = Field(
        ...,
        max_digits=12,
        decimal_places=2,
        description="Cash credit limit — PIC S9(10)V99",
    )
    acct_open_date: str = Field(
        ...,
        max_length=10,
        description="Account open date — YYYY-MM-DD",
    )
    acct_expiration_date: str = Field(
        ...,
        max_length=10,
        description="Account expiration date — YYYY-MM-DD",
    )
    acct_reissue_date: str = Field(
        ...,
        max_length=10,
        description="Reissue date — YYYY-MM-DD",
    )
    acct_curr_cyc_credit: Decimal = Field(
        ...,
        max_digits=12,
        decimal_places=2,
        description="Current cycle credit — PIC S9(10)V99",
    )
    acct_curr_cyc_debit: Decimal = Field(
        ...,
        max_digits=12,
        decimal_places=2,
        description="Current cycle debit — PIC S9(10)V99",
    )
    acct_addr_zip: str = Field(
        ...,
        max_length=10,
        description="Address ZIP code",
    )
    acct_group_id: str = Field(
        ...,
        max_length=10,
        description="Group ID",
    )
