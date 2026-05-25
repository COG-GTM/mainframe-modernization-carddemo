from collections.abc import Generator
from decimal import Decimal

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker
from sqlalchemy.pool import StaticPool

from app.database import Base, get_db
from app.main import app
from app.models.account import Account
from app.models.card_xref import CardXref

SQLALCHEMY_DATABASE_URL = "sqlite://"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False},
    poolclass=StaticPool,
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


@pytest.fixture(autouse=True)
def setup_db() -> Generator[None, None, None]:
    Base.metadata.create_all(bind=engine)
    yield
    Base.metadata.drop_all(bind=engine)


@pytest.fixture
def db_session() -> Generator[Session, None, None]:
    session = TestingSessionLocal()
    try:
        yield session
    finally:
        session.rollback()
        session.close()


@pytest.fixture
def client(db_session: Session) -> Generator[TestClient, None, None]:
    def _override_get_db() -> Generator[Session, None, None]:
        yield db_session

    app.dependency_overrides[get_db] = _override_get_db
    with TestClient(app) as c:
        yield c
    app.dependency_overrides.clear()


@pytest.fixture
def seed_test_data(db_session: Session) -> None:
    """Seed minimal data for tests."""
    # Active account with credit limit 5000
    account = Account(
        acct_id=12345678901,
        active_status="Y",
        curr_bal=Decimal("0.00"),
        credit_limit=Decimal("5000.00"),
        cash_credit_limit=Decimal("5000.00"),
        open_date="2020-01-01",
        expiration_date="2030-12-31",
        reissue_date="2025-01-01",
        curr_cyc_credit=Decimal("0.00"),
        curr_cyc_debit=Decimal("0.00"),
        addr_zip="12345",
        group_id="GRP001",
    )
    db_session.add(account)

    # Expired account for testing rejection code 103
    expired_account = Account(
        acct_id=99999999999,
        active_status="Y",
        curr_bal=Decimal("0.00"),
        credit_limit=Decimal("50000.00"),
        cash_credit_limit=Decimal("50000.00"),
        open_date="2015-01-01",
        expiration_date="2020-01-01",
        reissue_date="2020-01-01",
        curr_cyc_credit=Decimal("0.00"),
        curr_cyc_debit=Decimal("0.00"),
        addr_zip="99999",
        group_id="GRP002",
    )
    db_session.add(expired_account)

    # Card XREF for the active account
    xref = CardXref(
        card_num="1234567890123456",
        cust_id=123456789,
        acct_id=12345678901,
    )
    db_session.add(xref)

    # Card XREF for the expired account
    xref_expired = CardXref(
        card_num="9999999999999999",
        cust_id=999999999,
        acct_id=99999999999,
    )
    db_session.add(xref_expired)

    db_session.commit()
