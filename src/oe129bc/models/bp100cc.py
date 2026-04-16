"""BP100CC record layout model for the OE129BC validation program port.

NOTE: The BP100CC copybook was not found in the CardDemo COBOL source
(app/cpy/). This module provides a placeholder Pydantic model based on
common CardDemo record patterns observed in copybooks such as CVACT01Y
(account records) and CVTRA06Y (transaction records).

When the actual BP100CC copybook is located, this model should be
updated to reflect its exact field layout.
"""

from __future__ import annotations

from datetime import date
from decimal import Decimal

from pydantic import BaseModel, Field


class BP100CCRecord(BaseModel):
    """Placeholder model for the BP100CC COBOL copybook record layout.

    This model captures the common fields found across CardDemo record
    layouts (account, card, transaction) and serves as a starting point.
    Update this model once the actual BP100CC copybook is available.

    Field names follow Python conventions; the ``cobol_name`` is noted
    in each field description for traceability.
    """

    # Account identification fields
    account_id: str = Field(
        ...,
        max_length=11,
        description="Account identifier (COBOL: ACCT-ID, PIC 9(11))",
        examples=["00000000001"],
    )
    card_number: str = Field(
        ...,
        max_length=16,
        description="Credit card number (COBOL: CARD-NUM, PIC X(16))",
        examples=["4111111111111111"],
    )
    customer_id: str = Field(
        ...,
        max_length=9,
        description="Customer identifier (COBOL: CUST-ID, PIC 9(09))",
        examples=["000000001"],
    )

    # Account details
    account_status: str = Field(
        default="A",
        max_length=1,
        description="Account status flag (COBOL: ACCT-STATUS, PIC X(01)). "
        "'A'=Active, 'C'=Closed, 'S'=Suspended",
        examples=["A", "C", "S"],
    )
    credit_limit: Decimal = Field(
        default=Decimal("0.00"),
        ge=Decimal("0"),
        description="Credit limit amount (COBOL: ACCT-CREDIT-LIMIT, PIC S9(7)V99)",
        examples=["5000.00"],
    )
    current_balance: Decimal = Field(
        default=Decimal("0.00"),
        description="Current account balance (COBOL: ACCT-CURR-BAL, PIC S9(7)V99)",
        examples=["1234.56"],
    )

    # Date fields
    expiration_date: date | None = Field(
        default=None,
        description="Card expiration date (COBOL: CARD-EXPIRY-DATE)",
    )
    open_date: date | None = Field(
        default=None,
        description="Account open date (COBOL: ACCT-OPEN-DATE)",
    )

    # Processing fields
    return_code: str = Field(
        default="00",
        max_length=2,
        description="VSAM return code from last I/O operation "
        "(COBOL: BP100CC-RETURN-CODE, PIC XX)",
        examples=["00", "23", "35"],
    )
    error_message: str = Field(
        default="",
        max_length=80,
        description="Error message text from last operation "
        "(COBOL: BP100CC-ERROR-MSG, PIC X(80))",
    )

    model_config = {
        "json_schema_extra": {
            "description": (
                "Placeholder BP100CC record layout. "
                "Update when the actual copybook is located."
            ),
        },
    }
