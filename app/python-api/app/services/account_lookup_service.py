"""Account Lookup Service — Python port of COACTVWC.cbl.

Implements the multi-file cross-reference chain:
  1. Input: Account ID (11 digits) or Card Number (16 chars)
  2. CXACAIX (card xref alternate index by account) -> card + customer ID
  3. ACCTDAT (account master) -> account details
  4. CUSTDAT (customer master) -> customer details

Error messages are preserved from the original COBOL source (lines 122-136).
"""

from fastapi import HTTPException, status

from app.models.responses import AccountLookupResponse
from app.repositories.account_repository import AccountRepository

# ---------------------------------------------------------------------------
# Error messages — exact strings from COACTVWC.cbl WORKING-STORAGE SECTION
# ---------------------------------------------------------------------------
ERR_ACCT_NON_ZERO = "Account number must be a non zero 11 digit number"
ERR_NO_INPUT = "No input received"
ERR_NOT_IN_XREF = "Did not find this account in account card xref file"
ERR_NOT_IN_ACCT_MASTER = "Did not find this account in account master file"
ERR_NOT_IN_CUST_MASTER = "Did not find associated customer in master file"
ERR_XREF_READ = "Error reading account card xref File"


class AccountLookupService:
    """Resolves an account ID or card number to a full account + customer view.

    This service mirrors the 9000-READ-ACCT paragraph and its sub-paragraphs
    in COACTVWC.cbl, performing the three-step lookup chain with identical
    error semantics.
    """

    def __init__(self, repository: AccountRepository) -> None:
        self._repo = repository

    # ------------------------------------------------------------------
    # Public API
    # ------------------------------------------------------------------

    def lookup_by_account_id(self, account_id: str) -> AccountLookupResponse:
        """GET /api/v1/accounts/{account_id} — lookup by account ID.

        Mirrors the flow:
          2210-EDIT-ACCOUNT  -> validate input
          9200-GETCARDXREF-BYACCT -> xref lookup
          9300-GETACCTDATA-BYACCT -> account master lookup
          9400-GETCUSTDATA-BYCUST -> customer master lookup
        """
        self._validate_account_id(account_id)
        return self._resolve_by_account_id(account_id)

    def lookup_by_card_number(self, card_num: str) -> AccountLookupResponse:
        """GET /api/v1/accounts/by-card/{card_num} — reverse lookup by card.

        Uses card number to find the xref record first, then follows the
        same account + customer resolution chain.
        """
        if not card_num or not card_num.strip():
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=ERR_NO_INPUT,
            )

        xref = self._repo.get_xref_by_card_number(card_num)
        if xref is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=ERR_NOT_IN_XREF,
            )

        account = self._repo.get_account_by_id(xref.xref_acct_id)
        if account is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=ERR_NOT_IN_ACCT_MASTER,
            )

        customer = self._repo.get_customer_by_id(xref.xref_cust_id)
        if customer is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=ERR_NOT_IN_CUST_MASTER,
            )

        return AccountLookupResponse(
            account=account,
            customer=customer,
            card_number=xref.xref_card_num,
        )

    # ------------------------------------------------------------------
    # Internal helpers
    # ------------------------------------------------------------------

    @staticmethod
    def _validate_account_id(account_id: str) -> None:
        """2210-EDIT-ACCOUNT — input validation.

        COBOL checks:
          - Not blank / LOW-VALUES  -> 'No input received'
          - Not numeric / all zeros -> 'Account number must be a non zero 11 digit number'
        """
        if not account_id or not account_id.strip():
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=ERR_NO_INPUT,
            )
        if not account_id.isdigit() or len(account_id) != 11 or int(account_id) == 0:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=ERR_ACCT_NON_ZERO,
            )

    def _resolve_by_account_id(self, account_id: str) -> AccountLookupResponse:
        """9000-READ-ACCT — three-step cross-reference resolution."""
        # Step 1: CXACAIX — card xref by account (9200-GETCARDXREF-BYACCT)
        xref = self._repo.get_xref_by_account_id(account_id)
        if xref is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=ERR_NOT_IN_XREF,
            )

        # Step 2: ACCTDAT — account master (9300-GETACCTDATA-BYACCT)
        account = self._repo.get_account_by_id(account_id)
        if account is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=ERR_NOT_IN_ACCT_MASTER,
            )

        # Step 3: CUSTDAT — customer master (9400-GETCUSTDATA-BYCUST)
        customer = self._repo.get_customer_by_id(xref.xref_cust_id)
        if customer is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=ERR_NOT_IN_CUST_MASTER,
            )

        return AccountLookupResponse(
            account=account,
            customer=customer,
            card_number=xref.xref_card_num,
        )
