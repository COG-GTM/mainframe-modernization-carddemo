"""Unit tests for Pydantic record models (copybook fidelity)."""

from decimal import Decimal

import pytest
from pydantic import ValidationError

from app.models.card import CardRecord
from app.models.customer import CustomerRecord
from tests.conftest import make_account, make_card, make_customer, make_xref

# -------------------------------------------------------------------------
# AccountRecord (CVACT01Y — 300 bytes)
# -------------------------------------------------------------------------

class TestAccountRecord:
    def test_valid_account(self) -> None:
        acct = make_account()
        assert acct.acct_id == "00000000011"
        assert acct.acct_active_status == "Y"
        assert acct.acct_curr_bal == Decimal("1500.00")

    def test_acct_id_must_be_11_digits(self) -> None:
        with pytest.raises(ValidationError):
            make_account(acct_id="123")

    def test_acct_id_rejects_non_numeric(self) -> None:
        with pytest.raises(ValidationError):
            make_account(acct_id="0000000001A")

    def test_decimal_precision(self) -> None:
        acct = make_account(balance=Decimal("-9999999999.99"))
        assert acct.acct_curr_bal == Decimal("-9999999999.99")


# -------------------------------------------------------------------------
# CardRecord (CVACT02Y — 150 bytes)
# -------------------------------------------------------------------------

class TestCardRecord:
    def test_valid_card(self) -> None:
        card = make_card()
        assert card.card_num == "4000000000000001"
        assert card.card_cvv_cd == "123"

    def test_cvv_must_be_3_digits(self) -> None:
        with pytest.raises(ValidationError):
            CardRecord(
                card_num="4000000000000001",
                card_acct_id="00000000011",
                card_cvv_cd="12",  # too short
                card_embossed_name="TEST",
                card_expiration_date="2027-01-01",
                card_active_status="Y",
            )

    def test_card_acct_id_must_be_11_digits(self) -> None:
        with pytest.raises(ValidationError):
            make_card(acct_id="short")


# -------------------------------------------------------------------------
# CustomerRecord (CVCUS01Y — 500 bytes)
# -------------------------------------------------------------------------

class TestCustomerRecord:
    def test_valid_customer(self) -> None:
        cust = make_customer()
        assert cust.cust_id == "000000001"
        assert cust.cust_first_name == "John"
        assert cust.cust_fico_credit_score == 750

    def test_cust_id_must_be_9_digits(self) -> None:
        with pytest.raises(ValidationError):
            make_customer(cust_id="12345")

    def test_fico_max_999(self) -> None:
        with pytest.raises(ValidationError):
            CustomerRecord(
                cust_id="000000001",
                cust_first_name="X",
                cust_last_name="Y",
                cust_ssn="111111111",
                cust_dob_yyyy_mm_dd="1990-01-01",
                cust_fico_credit_score=1000,  # exceeds PIC 9(03)
            )


# -------------------------------------------------------------------------
# XrefRecord (CVACT03Y — 50 bytes)
# -------------------------------------------------------------------------

class TestXrefRecord:
    def test_valid_xref(self) -> None:
        xref = make_xref()
        assert xref.xref_card_num == "4000000000000001"
        assert xref.xref_cust_id == "000000001"
        assert xref.xref_acct_id == "00000000011"

    def test_xref_acct_id_must_be_11_digits(self) -> None:
        with pytest.raises(ValidationError):
            make_xref(acct_id="bad")

    def test_xref_cust_id_must_be_9_digits(self) -> None:
        with pytest.raises(ValidationError):
            make_xref(cust_id="bad")
