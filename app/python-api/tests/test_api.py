"""Integration tests for FastAPI endpoints (HTTP-level)."""

from fastapi.testclient import TestClient

from app.dependencies import set_repository
from app.main import app
from app.repositories.account_repository import InMemoryAccountRepository
from app.services.account_lookup_service import (
    ERR_ACCT_NON_ZERO,
    ERR_NOT_IN_XREF,
)
from tests.conftest import make_account, make_customer, make_xref


class TestGetAccountById:
    """GET /api/v1/accounts/{account_id}"""

    def test_200_happy_path(self, client: TestClient) -> None:
        resp = client.get("/api/v1/accounts/00000000011")
        assert resp.status_code == 200
        body = resp.json()
        assert body["account"]["acct_id"] == "00000000011"
        assert body["customer"]["cust_id"] == "000000001"
        assert body["card_number"] == "4000000000000001"

    def test_400_non_numeric_account(self, client: TestClient) -> None:
        resp = client.get("/api/v1/accounts/ABCDEFGHIJK")
        assert resp.status_code == 400
        assert resp.json()["detail"] == ERR_ACCT_NON_ZERO

    def test_404_account_not_in_xref(self, client: TestClient) -> None:
        resp = client.get("/api/v1/accounts/99999999999")
        assert resp.status_code == 404
        assert resp.json()["detail"] == ERR_NOT_IN_XREF

    def test_response_contains_all_account_fields(self, client: TestClient) -> None:
        resp = client.get("/api/v1/accounts/00000000011")
        acct = resp.json()["account"]
        expected_fields = {
            "acct_id",
            "acct_active_status",
            "acct_curr_bal",
            "acct_credit_limit",
            "acct_cash_credit_limit",
            "acct_open_date",
            "acct_expiration_date",
            "acct_reissue_date",
            "acct_curr_cyc_credit",
            "acct_curr_cyc_debit",
            "acct_addr_zip",
            "acct_group_id",
        }
        assert expected_fields == set(acct.keys())

    def test_response_contains_all_customer_fields(self, client: TestClient) -> None:
        resp = client.get("/api/v1/accounts/00000000011")
        cust = resp.json()["customer"]
        expected_fields = {
            "cust_id",
            "cust_first_name",
            "cust_middle_name",
            "cust_last_name",
            "cust_addr_line_1",
            "cust_addr_line_2",
            "cust_addr_line_3",
            "cust_addr_state_cd",
            "cust_addr_country_cd",
            "cust_addr_zip",
            "cust_phone_num_1",
            "cust_phone_num_2",
            "cust_ssn",
            "cust_govt_issued_id",
            "cust_dob_yyyy_mm_dd",
            "cust_eft_account_id",
            "cust_pri_card_holder_ind",
            "cust_fico_credit_score",
        }
        assert expected_fields == set(cust.keys())


class TestGetAccountByCard:
    """GET /api/v1/accounts/by-card/{card_num}"""

    def test_200_happy_path(self, client: TestClient) -> None:
        resp = client.get("/api/v1/accounts/by-card/4000000000000001")
        assert resp.status_code == 200
        body = resp.json()
        assert body["account"]["acct_id"] == "00000000011"
        assert body["customer"]["cust_last_name"] == "Doe"

    def test_404_unknown_card(self, client: TestClient) -> None:
        resp = client.get("/api/v1/accounts/by-card/0000000000000000")
        assert resp.status_code == 404

    def test_multiple_cards_resolve_independently(self) -> None:
        """Two cards linked to different accounts resolve correctly."""
        repo = InMemoryAccountRepository()
        repo.add_xref(make_xref(
            card_num="1111111111111111", acct_id="00000000011", cust_id="000000001",
        ))
        repo.add_xref(make_xref(
            card_num="2222222222222222", acct_id="00000000022", cust_id="000000002",
        ))
        repo.add_account(make_account(acct_id="00000000011"))
        repo.add_account(make_account(acct_id="00000000022"))
        repo.add_customer(make_customer(cust_id="000000001", first_name="Alice"))
        repo.add_customer(make_customer(cust_id="000000002", first_name="Bob"))
        set_repository(repo)

        with TestClient(app) as c:
            r1 = c.get("/api/v1/accounts/by-card/1111111111111111")
            assert r1.status_code == 200
            assert r1.json()["customer"]["cust_first_name"] == "Alice"

            r2 = c.get("/api/v1/accounts/by-card/2222222222222222")
            assert r2.status_code == 200
            assert r2.json()["customer"]["cust_first_name"] == "Bob"


class TestHealthEndpoint:
    def test_health_returns_ok(self, client: TestClient) -> None:
        resp = client.get("/health")
        assert resp.status_code == 200
        assert resp.json() == {"status": "ok"}
