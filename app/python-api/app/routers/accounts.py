"""FastAPI router for Account Lookup endpoints.

Endpoints:
  GET /api/v1/accounts/{account_id}       — full account + customer + card
  GET /api/v1/accounts/by-card/{card_num} — reverse lookup by card number
"""

from fastapi import APIRouter, Depends, Path

from app.dependencies import get_account_lookup_service
from app.models.responses import AccountLookupResponse, ErrorResponse
from app.services.account_lookup_service import AccountLookupService

router = APIRouter(
    prefix="/api/v1/accounts",
    tags=["accounts"],
)


@router.get(
    "/{account_id}",
    response_model=AccountLookupResponse,
    responses={
        400: {"model": ErrorResponse, "description": "Invalid account ID"},
        404: {"model": ErrorResponse, "description": "Account / customer / xref not found"},
    },
    summary="Look up account by Account ID",
    description=(
        "Resolves an 11-digit account ID through the cross-reference chain "
        "(CXACAIX → ACCTDAT → CUSTDAT) and returns combined account, "
        "customer, and card information."
    ),
)
def get_account_by_id(
    account_id: str = Path(
        ...,
        description="11-digit account ID",
        examples=["00000000011"],
    ),
    service: AccountLookupService = Depends(get_account_lookup_service),
) -> AccountLookupResponse:
    return service.lookup_by_account_id(account_id)


@router.get(
    "/by-card/{card_num}",
    response_model=AccountLookupResponse,
    responses={
        400: {"model": ErrorResponse, "description": "Invalid card number"},
        404: {"model": ErrorResponse, "description": "Card / account / customer not found"},
    },
    summary="Reverse-lookup account by Card Number",
    description=(
        "Resolves a card number through the cross-reference file to find the "
        "associated account and customer records."
    ),
)
def get_account_by_card(
    card_num: str = Path(
        ...,
        description="Card number (up to 16 characters)",
        examples=["4000000000000001"],
    ),
    service: AccountLookupService = Depends(get_account_lookup_service),
) -> AccountLookupResponse:
    return service.lookup_by_card_number(card_num)
