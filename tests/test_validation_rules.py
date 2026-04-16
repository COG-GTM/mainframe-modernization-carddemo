"""Template: Validation rule tests for the OE129BC Python port.

These tests verify that the CardDemo business-rule validation logic
correctly accepts valid inputs and rejects invalid ones.  Each test
class corresponds to one entity / record type; individual test methods
cover specific validation rules extracted from the COBOL program logic.

When the production validation module is implemented, update the
``validate_*`` placeholder imports to point at the real functions.
"""

from __future__ import annotations

import pytest

from tests.data.sample_records import (
    VALID_ACCOUNT_ACTIVE,
    VALID_ACCOUNT_INACTIVE,
    VALID_CARD_ACTIVE,
    VALID_CUSTOMER_PRIMARY,
    VALID_TRANSACTION_SALE,
    VALID_USER_ADMIN,
    VALID_USER_REGULAR,
)
from tests.utils.data_generator import (
    generate_invalid_account_negative_credit_limit,
    generate_invalid_card_bad_status,
    generate_invalid_card_expired,
    generate_invalid_customer_bad_fico,
    generate_invalid_customer_missing_name,
    generate_invalid_transaction_bad_type,
    generate_invalid_transaction_zero_amount,
    generate_invalid_user_bad_type,
)

# ---------------------------------------------------------------------------
# Placeholder validation functions — replace with real imports once the
# production ``carddemo.validation`` module exists.
# ---------------------------------------------------------------------------


def _validate_account(record: str) -> list[str]:
    """Placeholder: return a list of error messages (empty = valid)."""
    errors: list[str] = []
    active_status = record[11:12].strip()
    if active_status not in ("Y", "N"):
        errors.append("ACCT-ACTIVE-STATUS must be 'Y' or 'N'")
    # Credit-limit sign check (offset 25, 13 chars: sign + 10 int + 2 dec)
    credit_limit_str = record[25:38].strip()
    if credit_limit_str.startswith("-"):
        errors.append("ACCT-CREDIT-LIMIT must not be negative")
    return errors


def _validate_card(record: str) -> list[str]:
    errors: list[str] = []
    active_status = record[90:91].strip()
    if active_status not in ("Y", "N"):
        errors.append("CARD-ACTIVE-STATUS must be 'Y' or 'N'")
    return errors


def _validate_customer(record: str) -> list[str]:
    errors: list[str] = []
    first_name = record[9:34].strip()
    last_name = record[59:84].strip()
    if not first_name:
        errors.append("CUST-FIRST-NAME is required")
    if not last_name:
        errors.append("CUST-LAST-NAME is required")
    fico_str = record[329:332].strip()
    if fico_str.isdigit():
        fico = int(fico_str)
        if fico < 300 or fico > 850:
            errors.append("CUST-FICO-CREDIT-SCORE must be between 300 and 850")
    return errors


def _validate_transaction(record: str) -> list[str]:
    errors: list[str] = []
    type_cd = record[16:18].strip()
    valid_types = {"SA", "CA", "PR", "CR", "BA", "FE"}
    if type_cd not in valid_types:
        errors.append(f"TRAN-TYPE-CD '{type_cd}' is not a recognised type")
    return errors


def _validate_user(record: str) -> list[str]:
    errors: list[str] = []
    user_type = record[56:57].strip()
    if user_type not in ("A", "U"):
        errors.append("SEC-USR-TYPE must be 'A' or 'U'")
    return errors


# ═══════════════════════════════════════════════════════════════════════════
# Account validation
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.unit
class TestAccountValidation:
    """Validation rules for the Account record (CVACT01Y, 300 bytes)."""

    def test_valid_active_account_passes(self):
        errors = _validate_account(VALID_ACCOUNT_ACTIVE)
        assert errors == []

    def test_valid_inactive_account_passes(self):
        errors = _validate_account(VALID_ACCOUNT_INACTIVE)
        assert errors == []

    def test_negative_credit_limit_rejected(self):
        record = generate_invalid_account_negative_credit_limit()
        errors = _validate_account(record)
        assert any("CREDIT-LIMIT" in e for e in errors)

    def test_active_status_must_be_y_or_n(self, account_builder):
        record = account_builder(active_status="X")
        errors = _validate_account(record)
        assert any("ACTIVE-STATUS" in e for e in errors)


# ═══════════════════════════════════════════════════════════════════════════
# Card validation
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.unit
class TestCardValidation:
    """Validation rules for the Card record (CVACT02Y, 150 bytes)."""

    def test_valid_active_card_passes(self):
        errors = _validate_card(VALID_CARD_ACTIVE)
        assert errors == []

    def test_bad_active_status_rejected(self):
        record = generate_invalid_card_bad_status()
        errors = _validate_card(record)
        assert any("ACTIVE-STATUS" in e for e in errors)

    def test_card_number_must_be_16_chars(self, card_builder):
        record = card_builder(card_num="12345")
        card_num_field = record[0:16].strip()
        assert len(card_num_field) <= 16


# ═══════════════════════════════════════════════════════════════════════════
# Customer validation
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.unit
class TestCustomerValidation:
    """Validation rules for the Customer record (CVCUS01Y, 500 bytes)."""

    def test_valid_customer_passes(self):
        errors = _validate_customer(VALID_CUSTOMER_PRIMARY)
        assert errors == []

    def test_missing_names_rejected(self):
        record = generate_invalid_customer_missing_name()
        errors = _validate_customer(record)
        assert any("FIRST-NAME" in e for e in errors)
        assert any("LAST-NAME" in e for e in errors)

    def test_fico_out_of_range_rejected(self):
        record = generate_invalid_customer_bad_fico()
        errors = _validate_customer(record)
        assert any("FICO" in e for e in errors)


# ═══════════════════════════════════════════════════════════════════════════
# Transaction validation
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.unit
class TestTransactionValidation:
    """Validation rules for the Transaction record (CVTRA05Y, 350 bytes)."""

    def test_valid_sale_passes(self):
        errors = _validate_transaction(VALID_TRANSACTION_SALE)
        assert errors == []

    def test_invalid_type_code_rejected(self):
        record = generate_invalid_transaction_bad_type()
        errors = _validate_transaction(record)
        assert any("TYPE-CD" in e for e in errors)


# ═══════════════════════════════════════════════════════════════════════════
# User Security validation
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.unit
class TestUserSecurityValidation:
    """Validation rules for the User Security record (CSUSR01Y, 80 bytes)."""

    def test_valid_admin_passes(self):
        errors = _validate_user(VALID_USER_ADMIN)
        assert errors == []

    def test_valid_regular_user_passes(self):
        errors = _validate_user(VALID_USER_REGULAR)
        assert errors == []

    def test_invalid_user_type_rejected(self):
        record = generate_invalid_user_bad_type()
        errors = _validate_user(record)
        assert any("USR-TYPE" in e for e in errors)
