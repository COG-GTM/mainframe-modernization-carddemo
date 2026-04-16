"""Repository abstraction for VSAM file access.

In the original COBOL program, data is read from three VSAM files:
  - CXACAIX (card xref alternate index by account)
  - ACCTDAT (account master)
  - CUSTDAT (customer master)

This module provides a repository protocol and an in-memory implementation
for use in development and testing.  A production implementation would
back this with a relational database.
"""

from typing import Protocol

from app.models.account import AccountRecord
from app.models.customer import CustomerRecord
from app.models.xref import XrefRecord


class AccountRepository(Protocol):
    """Protocol defining the data-access contract for the account lookup chain."""

    def get_xref_by_account_id(self, account_id: str) -> XrefRecord | None:
        """CXACAIX lookup — card xref by account ID."""
        ...

    def get_xref_by_card_number(self, card_num: str) -> XrefRecord | None:
        """CXACAIX lookup — card xref by card number."""
        ...

    def get_account_by_id(self, account_id: str) -> AccountRecord | None:
        """ACCTDAT lookup — account master by account ID."""
        ...

    def get_customer_by_id(self, customer_id: str) -> CustomerRecord | None:
        """CUSTDAT lookup — customer master by customer ID."""
        ...


class InMemoryAccountRepository:
    """In-memory repository backed by dictionaries.

    Suitable for unit tests and local development.
    """

    def __init__(self) -> None:
        self._xref_by_account: dict[str, XrefRecord] = {}
        self._xref_by_card: dict[str, XrefRecord] = {}
        self._accounts: dict[str, AccountRecord] = {}
        self._customers: dict[str, CustomerRecord] = {}

    # ------------------------------------------------------------------
    # Seed helpers
    # ------------------------------------------------------------------

    def add_xref(self, xref: XrefRecord) -> None:
        self._xref_by_account[xref.xref_acct_id] = xref
        self._xref_by_card[xref.xref_card_num] = xref

    def add_account(self, account: AccountRecord) -> None:
        self._accounts[account.acct_id] = account

    def add_customer(self, customer: CustomerRecord) -> None:
        self._customers[customer.cust_id] = customer

    # ------------------------------------------------------------------
    # Protocol implementation
    # ------------------------------------------------------------------

    def get_xref_by_account_id(self, account_id: str) -> XrefRecord | None:
        return self._xref_by_account.get(account_id)

    def get_xref_by_card_number(self, card_num: str) -> XrefRecord | None:
        return self._xref_by_card.get(card_num)

    def get_account_by_id(self, account_id: str) -> AccountRecord | None:
        return self._accounts.get(account_id)

    def get_customer_by_id(self, customer_id: str) -> CustomerRecord | None:
        return self._customers.get(customer_id)
