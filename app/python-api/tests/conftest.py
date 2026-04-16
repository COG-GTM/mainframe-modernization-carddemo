"""Shared fixtures for account lookup service tests."""

from collections.abc import Generator
from decimal import Decimal

import pytest
from fastapi.testclient import TestClient

from app.dependencies import set_repository
from app.main import app
from app.models.account import AccountRecord
from app.models.card import CardRecord
from app.models.customer import CustomerRecord
from app.models.xref import XrefRecord
from app.repositories.account_repository import InMemoryAccountRepository

# ---------------------------------------------------------------------------
# Reusable test data factories
# ---------------------------------------------------------------------------

def make_xref(
    card_num: str = "4000000000000001",
    cust_id: str = "000000001",
    acct_id: str = "00000000011",
) -> XrefRecord:
    return XrefRecord(
        xref_card_num=card_num,
        xref_cust_id=cust_id,
        xref_acct_id=acct_id,
    )


def make_account(
    acct_id: str = "00000000011",
    active: str = "Y",
    balance: Decimal = Decimal("1500.00"),
) -> AccountRecord:
    return AccountRecord(
        acct_id=acct_id,
        acct_active_status=active,
        acct_curr_bal=balance,
        acct_credit_limit=Decimal("5000.00"),
        acct_cash_credit_limit=Decimal("1500.00"),
        acct_open_date="2020-01-15",
        acct_expiration_date="2027-01-15",
        acct_reissue_date="2025-01-15",
        acct_curr_cyc_credit=Decimal("200.00"),
        acct_curr_cyc_debit=Decimal("350.50"),
        acct_addr_zip="30301",
        acct_group_id="GRP001",
    )


def make_customer(
    cust_id: str = "000000001",
    first_name: str = "John",
    last_name: str = "Doe",
) -> CustomerRecord:
    return CustomerRecord(
        cust_id=cust_id,
        cust_first_name=first_name,
        cust_middle_name="M",
        cust_last_name=last_name,
        cust_addr_line_1="123 Main St",
        cust_addr_line_2="Apt 4B",
        cust_addr_line_3="Atlanta",
        cust_addr_state_cd="GA",
        cust_addr_country_cd="US",
        cust_addr_zip="30301",
        cust_phone_num_1="555-123-4567",
        cust_phone_num_2="555-987-6543",
        cust_ssn="123456789",
        cust_govt_issued_id="DL123456",
        cust_dob_yyyy_mm_dd="1985-06-15",
        cust_eft_account_id="EFT001",
        cust_pri_card_holder_ind="Y",
        cust_fico_credit_score=750,
    )


def make_card(
    card_num: str = "4000000000000001",
    acct_id: str = "00000000011",
) -> CardRecord:
    return CardRecord(
        card_num=card_num,
        card_acct_id=acct_id,
        card_cvv_cd="123",
        card_embossed_name="JOHN M DOE",
        card_expiration_date="2027-01-15",
        card_active_status="Y",
    )


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture()
def repo() -> InMemoryAccountRepository:
    """Empty in-memory repository."""
    return InMemoryAccountRepository()


@pytest.fixture()
def seeded_repo() -> InMemoryAccountRepository:
    """Repository pre-loaded with one full cross-reference chain."""
    r = InMemoryAccountRepository()
    r.add_xref(make_xref())
    r.add_account(make_account())
    r.add_customer(make_customer())
    return r


@pytest.fixture()
def client(seeded_repo: InMemoryAccountRepository) -> Generator[TestClient, None, None]:
    """FastAPI TestClient wired to the seeded repository."""
    set_repository(seeded_repo)
    with TestClient(app) as c:
        yield c
