"""Shared pytest fixtures for CardDemo OE129BC test suite.

This module provides:
    - Pre-built record fixtures for every CardDemo entity type
    - A seeded random number generator for reproducible test data
    - FastAPI TestClient fixtures (when the app module exists)
    - Temporary file helpers for integration tests
"""

from __future__ import annotations

import random
import tempfile
from pathlib import Path
from typing import Generator

import pytest

# ---------------------------------------------------------------------------
# Record builders & sample data (always available)
# ---------------------------------------------------------------------------
from tests.utils.record_builder import (
    build_account_record,
    build_card_record,
    build_card_xref_record,
    build_customer_record,
    build_daily_transaction_record,
    build_disclosure_group_record,
    build_tran_cat_balance_record,
    build_tran_cat_record,
    build_tran_type_record,
    build_transaction_record,
    build_user_security_record,
)
from tests.data.sample_records import (
    VALID_ACCOUNT_ACTIVE,
    VALID_ACCOUNT_HIGH_BALANCE,
    VALID_ACCOUNT_INACTIVE,
    VALID_CARD_ACTIVE,
    VALID_CARD_EXPIRED,
    VALID_CARD_INACTIVE,
    VALID_CUSTOMER_PRIMARY,
    VALID_CUSTOMER_SECONDARY,
    VALID_DAILY_TRANSACTION,
    VALID_DISCLOSURE_GROUP,
    VALID_TRAN_CAT_BALANCE,
    VALID_TRAN_CAT_GROCERY,
    VALID_TRAN_TYPE_SALE,
    VALID_TRANSACTION_CASH_ADVANCE,
    VALID_TRANSACTION_CREDIT,
    VALID_TRANSACTION_FEE,
    VALID_TRANSACTION_PAYMENT,
    VALID_TRANSACTION_SALE,
    VALID_USER_ADMIN,
    VALID_USER_REGULAR,
    VALID_XREF_1,
    VALID_XREF_2,
)

# ---------------------------------------------------------------------------
# Seeded RNG for reproducible randomised tests
# ---------------------------------------------------------------------------

@pytest.fixture
def rng() -> random.Random:
    """Return a seeded Random instance for deterministic test data."""
    return random.Random(12345)


# ---------------------------------------------------------------------------
# Account fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def valid_account_active() -> str:
    return VALID_ACCOUNT_ACTIVE


@pytest.fixture
def valid_account_inactive() -> str:
    return VALID_ACCOUNT_INACTIVE


@pytest.fixture
def valid_account_high_balance() -> str:
    return VALID_ACCOUNT_HIGH_BALANCE


# ---------------------------------------------------------------------------
# Card fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def valid_card_active() -> str:
    return VALID_CARD_ACTIVE


@pytest.fixture
def valid_card_inactive() -> str:
    return VALID_CARD_INACTIVE


@pytest.fixture
def valid_card_expired() -> str:
    return VALID_CARD_EXPIRED


# ---------------------------------------------------------------------------
# Card cross-reference fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def valid_xref_1() -> str:
    return VALID_XREF_1


@pytest.fixture
def valid_xref_2() -> str:
    return VALID_XREF_2


# ---------------------------------------------------------------------------
# Customer fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def valid_customer_primary() -> str:
    return VALID_CUSTOMER_PRIMARY


@pytest.fixture
def valid_customer_secondary() -> str:
    return VALID_CUSTOMER_SECONDARY


# ---------------------------------------------------------------------------
# Transaction fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def valid_transaction_sale() -> str:
    return VALID_TRANSACTION_SALE


@pytest.fixture
def valid_transaction_cash_advance() -> str:
    return VALID_TRANSACTION_CASH_ADVANCE


@pytest.fixture
def valid_transaction_payment() -> str:
    return VALID_TRANSACTION_PAYMENT


@pytest.fixture
def valid_transaction_credit() -> str:
    return VALID_TRANSACTION_CREDIT


@pytest.fixture
def valid_transaction_fee() -> str:
    return VALID_TRANSACTION_FEE


@pytest.fixture
def valid_daily_transaction() -> str:
    return VALID_DAILY_TRANSACTION


# ---------------------------------------------------------------------------
# Reference data fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def valid_tran_type_sale() -> str:
    return VALID_TRAN_TYPE_SALE


@pytest.fixture
def valid_tran_cat_grocery() -> str:
    return VALID_TRAN_CAT_GROCERY


@pytest.fixture
def valid_tran_cat_balance() -> str:
    return VALID_TRAN_CAT_BALANCE


@pytest.fixture
def valid_disclosure_group() -> str:
    return VALID_DISCLOSURE_GROUP


# ---------------------------------------------------------------------------
# User security fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def valid_user_admin() -> str:
    return VALID_USER_ADMIN


@pytest.fixture
def valid_user_regular() -> str:
    return VALID_USER_REGULAR


# ---------------------------------------------------------------------------
# Builder-function fixtures (convenience aliases)
# ---------------------------------------------------------------------------

@pytest.fixture
def account_builder():
    """Return the account record builder for custom parameterisation."""
    return build_account_record


@pytest.fixture
def card_builder():
    """Return the card record builder for custom parameterisation."""
    return build_card_record


@pytest.fixture
def card_xref_builder():
    """Return the card XREF record builder for custom parameterisation."""
    return build_card_xref_record


@pytest.fixture
def customer_builder():
    """Return the customer record builder for custom parameterisation."""
    return build_customer_record


@pytest.fixture
def transaction_builder():
    """Return the transaction record builder for custom parameterisation."""
    return build_transaction_record


@pytest.fixture
def daily_transaction_builder():
    """Return the daily transaction record builder."""
    return build_daily_transaction_record


@pytest.fixture
def tran_type_builder():
    """Return the transaction type record builder."""
    return build_tran_type_record


@pytest.fixture
def tran_cat_builder():
    """Return the transaction category record builder."""
    return build_tran_cat_record


@pytest.fixture
def tran_cat_balance_builder():
    """Return the transaction category balance record builder."""
    return build_tran_cat_balance_record


@pytest.fixture
def disclosure_group_builder():
    """Return the disclosure group record builder."""
    return build_disclosure_group_record


@pytest.fixture
def user_security_builder():
    """Return the user security record builder."""
    return build_user_security_record


# ---------------------------------------------------------------------------
# Temporary directory / file helpers for integration tests
# ---------------------------------------------------------------------------

@pytest.fixture
def tmp_data_dir() -> Generator[Path, None, None]:
    """Provide a temporary directory for data files, cleaned up after test."""
    with tempfile.TemporaryDirectory(prefix="carddemo_test_") as tmpdir:
        yield Path(tmpdir)


@pytest.fixture
def write_record_file(tmp_data_dir: Path):
    """Return a helper function that writes a list of records to a temp file.

    Usage in tests::

        def test_batch(write_record_file):
            path = write_record_file("input.dat", [record1, record2])
            # ...use path...
    """

    def _write(filename: str, records: list[str]) -> Path:
        fpath = tmp_data_dir / filename
        fpath.write_text("\n".join(records) + "\n")
        return fpath

    return _write


# ---------------------------------------------------------------------------
# FastAPI TestClient (conditional — only when app module is importable)
# ---------------------------------------------------------------------------

@pytest.fixture
def api_client():
    """Return a FastAPI ``TestClient`` for the CardDemo API.

    Skips the test gracefully when the ``carddemo`` application module
    has not yet been implemented.
    """
    try:
        from fastapi.testclient import TestClient

        # Attempt to import the app; adjust the path once the
        # application module is created.
        from carddemo.main import app  # type: ignore[import-not-found]

        return TestClient(app)
    except ImportError:
        pytest.skip("carddemo application module not yet available")
