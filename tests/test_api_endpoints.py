"""Template: FastAPI endpoint tests for the OE129BC Python port.

These tests use ``httpx.AsyncClient`` / ``fastapi.testclient.TestClient``
to exercise the REST API layer.  Until the ``carddemo`` application
package is implemented, every test that requires the client is
auto-skipped via the ``api_client`` fixture defined in ``conftest.py``.

The tests below are *templates* — they document the expected endpoints,
request shapes, and response contracts so that developers can fill in
the details as the API is built.
"""

from __future__ import annotations

import pytest


# ═══════════════════════════════════════════════════════════════════════════
# Health / readiness
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.integration
class TestHealthEndpoint:
    """GET /health — basic liveness probe."""

    def test_health_returns_200(self, api_client):
        resp = api_client.get("/health")
        assert resp.status_code == 200

    def test_health_response_body(self, api_client):
        resp = api_client.get("/health")
        data = resp.json()
        assert "status" in data


# ═══════════════════════════════════════════════════════════════════════════
# Account endpoints
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.integration
class TestAccountEndpoints:
    """CRUD endpoints for the Account entity."""

    def test_get_account_by_id(self, api_client):
        resp = api_client.get("/api/v1/accounts/12345678901")
        assert resp.status_code in (200, 404)

    def test_list_accounts(self, api_client):
        resp = api_client.get("/api/v1/accounts")
        assert resp.status_code == 200
        assert isinstance(resp.json(), list)

    def test_create_account(self, api_client):
        payload = {
            "acct_id": "99999999999",
            "active_status": "Y",
            "credit_limit": 10000.00,
        }
        resp = api_client.post("/api/v1/accounts", json=payload)
        assert resp.status_code in (201, 422)

    def test_update_account(self, api_client):
        payload = {"active_status": "N"}
        resp = api_client.put("/api/v1/accounts/12345678901", json=payload)
        assert resp.status_code in (200, 404, 422)


# ═══════════════════════════════════════════════════════════════════════════
# Card endpoints
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.integration
class TestCardEndpoints:
    """CRUD endpoints for the Card entity."""

    def test_get_card(self, api_client):
        resp = api_client.get("/api/v1/cards/4111111111111111")
        assert resp.status_code in (200, 404)

    def test_list_cards_by_account(self, api_client):
        resp = api_client.get("/api/v1/accounts/12345678901/cards")
        assert resp.status_code in (200, 404)


# ═══════════════════════════════════════════════════════════════════════════
# Customer endpoints
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.integration
class TestCustomerEndpoints:
    """CRUD endpoints for the Customer entity."""

    def test_get_customer(self, api_client):
        resp = api_client.get("/api/v1/customers/100000001")
        assert resp.status_code in (200, 404)

    def test_create_customer(self, api_client):
        payload = {
            "cust_id": "999999999",
            "first_name": "TEST",
            "last_name": "USER",
            "addr_state_cd": "CA",
            "addr_country_cd": "USA",
            "addr_zip": "90210",
        }
        resp = api_client.post("/api/v1/customers", json=payload)
        assert resp.status_code in (201, 422)

    def test_update_customer(self, api_client):
        payload = {"first_name": "UPDATED"}
        resp = api_client.put("/api/v1/customers/100000001", json=payload)
        assert resp.status_code in (200, 404, 422)


# ═══════════════════════════════════════════════════════════════════════════
# Transaction endpoints
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.integration
class TestTransactionEndpoints:
    """CRUD endpoints for the Transaction entity."""

    def test_get_transaction(self, api_client):
        resp = api_client.get("/api/v1/transactions/0000000000000001")
        assert resp.status_code in (200, 404)

    def test_list_transactions_by_card(self, api_client):
        resp = api_client.get(
            "/api/v1/transactions",
            params={"card_num": "4111111111111111"},
        )
        assert resp.status_code == 200

    def test_create_transaction(self, api_client):
        payload = {
            "type_cd": "SA",
            "cat_cd": 5001,
            "source": "POS",
            "desc": "TEST PURCHASE",
            "amt": 42.50,
            "card_num": "4111111111111111",
        }
        resp = api_client.post("/api/v1/transactions", json=payload)
        assert resp.status_code in (201, 422)


# ═══════════════════════════════════════════════════════════════════════════
# Authentication / user endpoints
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.integration
class TestAuthEndpoints:
    """Authentication-related endpoints."""

    def test_login(self, api_client):
        payload = {"user_id": "USER0001", "password": "PASSWORD"}
        resp = api_client.post("/api/v1/auth/login", json=payload)
        assert resp.status_code in (200, 401)

    def test_invalid_login_rejected(self, api_client):
        payload = {"user_id": "BADUSER", "password": "WRONG"}
        resp = api_client.post("/api/v1/auth/login", json=payload)
        assert resp.status_code in (401, 422)


# ═══════════════════════════════════════════════════════════════════════════
# Error-response contract tests
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.integration
class TestErrorResponses:
    """API error responses follow a consistent JSON schema."""

    def test_404_returns_json(self, api_client):
        resp = api_client.get("/api/v1/accounts/00000000000")
        if resp.status_code == 404:
            data = resp.json()
            assert "detail" in data or "message" in data

    def test_422_on_invalid_payload(self, api_client):
        resp = api_client.post("/api/v1/accounts", json={})
        if resp.status_code == 422:
            data = resp.json()
            assert "detail" in data
