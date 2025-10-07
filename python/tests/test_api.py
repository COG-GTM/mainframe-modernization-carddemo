import pytest
from fastapi.testclient import TestClient
from bill_payment.api import app
from bill_payment.data_store import data_store


@pytest.fixture
def client():
    data_store.add_sample_data()
    return TestClient(app)


def test_health_check(client):
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "healthy", "service": "bill-payment"}


def test_validate_account_success(client):
    response = client.post(
        "/api/v1/bill-payment/validate",
        json={"acct_id": "12345678901"}
    )
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    assert data["current_balance"] == 1500.50


def test_validate_account_not_found(client):
    response = client.post(
        "/api/v1/bill-payment/validate",
        json={"acct_id": "99999999999"}
    )
    assert response.status_code == 400
    assert "NOT found" in response.json()["detail"]


def test_validate_account_empty_id(client):
    response = client.post(
        "/api/v1/bill-payment/validate",
        json={"acct_id": ""}
    )
    assert response.status_code == 400
    assert "can NOT be empty" in response.json()["detail"]


def test_validate_account_zero_balance(client):
    response = client.post(
        "/api/v1/bill-payment/validate",
        json={"acct_id": "00000000000"}
    )
    assert response.status_code == 400
    assert "nothing to pay" in response.json()["detail"]


def test_process_payment_success(client):
    response = client.post(
        "/api/v1/bill-payment/process",
        json={"acct_id": "12345678901", "confirm": "Y"}
    )
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    assert "successful" in data["message"]
    assert "transaction_id" in data
    assert data["amount_paid"] == 1500.50


def test_process_payment_no_confirmation(client):
    response = client.post(
        "/api/v1/bill-payment/process",
        json={"acct_id": "12345678901", "confirm": "N"}
    )
    assert response.status_code == 400
    assert "Confirm" in response.json()["detail"]


def test_process_payment_invalid_account(client):
    response = client.post(
        "/api/v1/bill-payment/process",
        json={"acct_id": "99999999999", "confirm": "Y"}
    )
    assert response.status_code == 400
    assert "NOT found" in response.json()["detail"]
