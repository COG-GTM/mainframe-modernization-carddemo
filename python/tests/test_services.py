import pytest
from decimal import Decimal
from bill_payment.services import BillPaymentService
from bill_payment.data_store import DataStore
from bill_payment.models import AccountRecord, CardXrefRecord


@pytest.fixture
def test_data_store():
    store = DataStore()
    store.accounts["12345678901"] = AccountRecord(
        acct_id="12345678901",
        acct_active_status="Y",
        acct_curr_bal=Decimal("1500.50"),
        acct_credit_limit=Decimal("5000.00"),
        acct_cash_credit_limit=Decimal("1000.00")
    )
    store.card_xrefs["12345678901"] = CardXrefRecord(
        xref_card_num="4111111111111111",
        xref_cust_id="000000001",
        xref_acct_id="12345678901"
    )
    return store


@pytest.fixture
def service(test_data_store, monkeypatch):
    from bill_payment import data_store as ds
    monkeypatch.setattr(ds, "accounts", test_data_store.accounts)
    monkeypatch.setattr(ds, "card_xrefs", test_data_store.card_xrefs)
    monkeypatch.setattr(ds, "transactions", test_data_store.transactions)
    return BillPaymentService()


def test_validate_account_empty_id(service):
    result = service.validate_account("")
    assert not result["success"]
    assert "can NOT be empty" in result["error"]


def test_validate_account_not_found(service):
    result = service.validate_account("99999999999")
    assert not result["success"]
    assert "NOT found" in result["error"]


def test_validate_account_zero_balance(service, test_data_store, monkeypatch):
    from bill_payment import data_store as ds
    test_data_store.accounts["12345678901"].acct_curr_bal = Decimal("0.00")
    monkeypatch.setattr(ds, "accounts", test_data_store.accounts)
    
    result = service.validate_account("12345678901")
    assert not result["success"]
    assert "nothing to pay" in result["error"]


def test_validate_account_success(service):
    result = service.validate_account("12345678901")
    assert result["success"]
    assert result["current_balance"] == 1500.50


def test_process_payment_no_confirmation(service):
    result = service.process_payment("12345678901", "N")
    assert not result["success"]
    assert "Confirm" in result["error"]


def test_process_payment_invalid_account(service):
    result = service.process_payment("99999999999", "Y")
    assert not result["success"]
    assert "NOT found" in result["error"]


def test_process_payment_success(service, test_data_store, monkeypatch):
    from bill_payment import data_store as ds
    monkeypatch.setattr(ds, "accounts", test_data_store.accounts)
    monkeypatch.setattr(ds, "card_xrefs", test_data_store.card_xrefs)
    monkeypatch.setattr(ds, "transactions", test_data_store.transactions)
    
    result = service.process_payment("12345678901", "Y")
    assert result["success"]
    assert "successful" in result["message"]
    assert "transaction_id" in result
    assert result["amount_paid"] == 1500.50
    
    account = test_data_store.get_account("12345678901")
    assert account.acct_curr_bal == Decimal("0.00")


def test_process_payment_generates_transaction_id(service, test_data_store, monkeypatch):
    from bill_payment import data_store as ds
    monkeypatch.setattr(ds, "accounts", test_data_store.accounts)
    monkeypatch.setattr(ds, "card_xrefs", test_data_store.card_xrefs)
    monkeypatch.setattr(ds, "transactions", test_data_store.transactions)
    
    result = service.process_payment("12345678901", "Y")
    assert result["success"]
    assert len(result["transaction_id"]) == 16
    assert result["transaction_id"] == "0000000000000001"
