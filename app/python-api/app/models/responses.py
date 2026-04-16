"""API response models for the Account Lookup Service."""

from pydantic import BaseModel, Field

from app.models.account import AccountRecord
from app.models.customer import CustomerRecord


class AccountLookupResponse(BaseModel):
    """Combined account + customer + card response.

    This mirrors the COBOL program's output which populates both
    account and customer data on the COACTVW screen after the
    multi-file cross-reference chain succeeds.
    """

    account: AccountRecord = Field(..., description="Account master data")
    customer: CustomerRecord = Field(..., description="Customer master data")
    card_number: str = Field(..., description="Card number from cross-reference")


class ErrorResponse(BaseModel):
    """Standard error response preserving COBOL error message semantics."""

    detail: str = Field(..., description="Error message")
