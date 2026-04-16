"""Pydantic model for CUST-RECORD (CVCUS01Y.cpy — 500 bytes).

COBOL layout:
  01 CUST-RECORD.
    05 CUST-ID              PIC 9(09).
    05 CUST-FIRST-NAME      PIC X(25).
    05 CUST-MIDDLE-NAME     PIC X(25).
    05 CUST-LAST-NAME       PIC X(25).
    05 CUST-ADDR-LINE-1     PIC X(50).
    05 CUST-ADDR-LINE-2     PIC X(50).
    05 CUST-ADDR-LINE-3     PIC X(50).
    05 CUST-ADDR-STATE-CD   PIC X(02).
    05 CUST-ADDR-COUNTRY-CD PIC X(03).
    05 CUST-ADDR-ZIP        PIC X(10).
    05 CUST-PHONE-NUM-1     PIC X(15).
    05 CUST-PHONE-NUM-2     PIC X(15).
    05 CUST-SSN             PIC 9(09).
    05 CUST-GOVT-ISSUED-ID  PIC X(20).
    05 CUST-DOB-YYYY-MM-DD  PIC X(10).
    05 CUST-EFT-ACCOUNT-ID  PIC X(10).
    05 CUST-PRI-CARD-HOLDER-IND PIC X(01).
    05 CUST-FICO-CREDIT-SCORE PIC 9(03).
    05 FILLER               PIC X(168).
"""

from pydantic import BaseModel, Field


class CustomerRecord(BaseModel):
    """Customer master record — maps to CVCUS01Y.cpy (500-byte CUST-RECORD)."""

    cust_id: str = Field(
        ...,
        min_length=9,
        max_length=9,
        pattern=r"^\d{9}$",
        description="Customer ID — PIC 9(09)",
    )
    cust_first_name: str = Field(
        ...,
        max_length=25,
        description="First name",
    )
    cust_middle_name: str = Field(
        default="",
        max_length=25,
        description="Middle name",
    )
    cust_last_name: str = Field(
        ...,
        max_length=25,
        description="Last name",
    )
    cust_addr_line_1: str = Field(
        default="",
        max_length=50,
        description="Address line 1",
    )
    cust_addr_line_2: str = Field(
        default="",
        max_length=50,
        description="Address line 2",
    )
    cust_addr_line_3: str = Field(
        default="",
        max_length=50,
        description="Address line 3",
    )
    cust_addr_state_cd: str = Field(
        default="",
        max_length=2,
        description="State code",
    )
    cust_addr_country_cd: str = Field(
        default="",
        max_length=3,
        description="Country code",
    )
    cust_addr_zip: str = Field(
        default="",
        max_length=10,
        description="ZIP code",
    )
    cust_phone_num_1: str = Field(
        default="",
        max_length=15,
        description="Primary phone number",
    )
    cust_phone_num_2: str = Field(
        default="",
        max_length=15,
        description="Secondary phone number",
    )
    cust_ssn: str = Field(
        ...,
        min_length=9,
        max_length=9,
        pattern=r"^\d{9}$",
        description="Social Security Number — PIC 9(09)",
    )
    cust_govt_issued_id: str = Field(
        default="",
        max_length=20,
        description="Government-issued ID",
    )
    cust_dob_yyyy_mm_dd: str = Field(
        ...,
        max_length=10,
        description="Date of birth — YYYY-MM-DD",
    )
    cust_eft_account_id: str = Field(
        default="",
        max_length=10,
        description="EFT account ID",
    )
    cust_pri_card_holder_ind: str = Field(
        default="",
        max_length=1,
        description="Primary cardholder indicator",
    )
    cust_fico_credit_score: int = Field(
        ...,
        ge=0,
        le=999,
        description="FICO credit score — PIC 9(03)",
    )
