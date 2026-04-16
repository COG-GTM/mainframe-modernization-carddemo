"""Unit tests for AccountLookupService — business logic and error messages."""

import pytest
from fastapi import HTTPException

from app.repositories.account_repository import InMemoryAccountRepository
from app.services.account_lookup_service import (
    ERR_ACCT_NON_ZERO,
    ERR_NO_INPUT,
    ERR_NOT_IN_ACCT_MASTER,
    ERR_NOT_IN_CUST_MASTER,
    ERR_NOT_IN_XREF,
    AccountLookupService,
)
from tests.conftest import make_account, make_xref


class TestLookupByAccountId:
    """Tests for lookup_by_account_id — mirrors 9000-READ-ACCT flow."""

    def test_happy_path(self, seeded_repo: InMemoryAccountRepository) -> None:
        svc = AccountLookupService(seeded_repo)
        result = svc.lookup_by_account_id("00000000011")
        assert result.account.acct_id == "00000000011"
        assert result.customer.cust_id == "000000001"
        assert result.card_number == "4000000000000001"

    def test_empty_account_id_raises_no_input(
        self, repo: InMemoryAccountRepository
    ) -> None:
        svc = AccountLookupService(repo)
        with pytest.raises(HTTPException) as exc_info:
            svc.lookup_by_account_id("")
        assert exc_info.value.status_code == 400
        assert exc_info.value.detail == ERR_NO_INPUT

    def test_whitespace_account_id_raises_no_input(
        self, repo: InMemoryAccountRepository
    ) -> None:
        svc = AccountLookupService(repo)
        with pytest.raises(HTTPException) as exc_info:
            svc.lookup_by_account_id("   ")
        assert exc_info.value.status_code == 400
        assert exc_info.value.detail == ERR_NO_INPUT

    def test_non_numeric_raises_non_zero(
        self, repo: InMemoryAccountRepository
    ) -> None:
        svc = AccountLookupService(repo)
        with pytest.raises(HTTPException) as exc_info:
            svc.lookup_by_account_id("0000000001A")
        assert exc_info.value.status_code == 400
        assert exc_info.value.detail == ERR_ACCT_NON_ZERO

    def test_all_zeros_raises_non_zero(
        self, repo: InMemoryAccountRepository
    ) -> None:
        svc = AccountLookupService(repo)
        with pytest.raises(HTTPException) as exc_info:
            svc.lookup_by_account_id("00000000000")
        assert exc_info.value.status_code == 400
        assert exc_info.value.detail == ERR_ACCT_NON_ZERO

    def test_wrong_length_raises_non_zero(
        self, repo: InMemoryAccountRepository
    ) -> None:
        svc = AccountLookupService(repo)
        with pytest.raises(HTTPException) as exc_info:
            svc.lookup_by_account_id("123")
        assert exc_info.value.status_code == 400
        assert exc_info.value.detail == ERR_ACCT_NON_ZERO

    def test_not_in_xref_raises_404(
        self, repo: InMemoryAccountRepository
    ) -> None:
        """Account ID is valid but no xref record exists."""
        svc = AccountLookupService(repo)
        with pytest.raises(HTTPException) as exc_info:
            svc.lookup_by_account_id("00000000099")
        assert exc_info.value.status_code == 404
        assert exc_info.value.detail == ERR_NOT_IN_XREF

    def test_xref_exists_but_no_account_master(
        self, repo: InMemoryAccountRepository
    ) -> None:
        """Xref found, but account master file has no matching record."""
        repo.add_xref(make_xref(acct_id="00000000022"))
        svc = AccountLookupService(repo)
        with pytest.raises(HTTPException) as exc_info:
            svc.lookup_by_account_id("00000000022")
        assert exc_info.value.status_code == 404
        assert exc_info.value.detail == ERR_NOT_IN_ACCT_MASTER

    def test_xref_and_account_exist_but_no_customer(
        self, repo: InMemoryAccountRepository
    ) -> None:
        """Xref + account found, but customer master has no matching record."""
        repo.add_xref(make_xref(acct_id="00000000033", cust_id="000000099"))
        repo.add_account(make_account(acct_id="00000000033"))
        svc = AccountLookupService(repo)
        with pytest.raises(HTTPException) as exc_info:
            svc.lookup_by_account_id("00000000033")
        assert exc_info.value.status_code == 404
        assert exc_info.value.detail == ERR_NOT_IN_CUST_MASTER


class TestLookupByCardNumber:
    """Tests for lookup_by_card_number — reverse lookup flow."""

    def test_happy_path(self, seeded_repo: InMemoryAccountRepository) -> None:
        svc = AccountLookupService(seeded_repo)
        result = svc.lookup_by_card_number("4000000000000001")
        assert result.account.acct_id == "00000000011"
        assert result.customer.cust_first_name == "John"

    def test_empty_card_raises_no_input(
        self, repo: InMemoryAccountRepository
    ) -> None:
        svc = AccountLookupService(repo)
        with pytest.raises(HTTPException) as exc_info:
            svc.lookup_by_card_number("")
        assert exc_info.value.status_code == 400
        assert exc_info.value.detail == ERR_NO_INPUT

    def test_whitespace_card_raises_no_input(
        self, repo: InMemoryAccountRepository
    ) -> None:
        svc = AccountLookupService(repo)
        with pytest.raises(HTTPException) as exc_info:
            svc.lookup_by_card_number("   ")
        assert exc_info.value.status_code == 400
        assert exc_info.value.detail == ERR_NO_INPUT

    def test_card_not_in_xref(
        self, repo: InMemoryAccountRepository
    ) -> None:
        svc = AccountLookupService(repo)
        with pytest.raises(HTTPException) as exc_info:
            svc.lookup_by_card_number("9999999999999999")
        assert exc_info.value.status_code == 404
        assert exc_info.value.detail == ERR_NOT_IN_XREF

    def test_card_xref_exists_but_no_account(
        self, repo: InMemoryAccountRepository
    ) -> None:
        repo.add_xref(make_xref(card_num="5555000000000001", acct_id="00000000044"))
        svc = AccountLookupService(repo)
        with pytest.raises(HTTPException) as exc_info:
            svc.lookup_by_card_number("5555000000000001")
        assert exc_info.value.status_code == 404
        assert exc_info.value.detail == ERR_NOT_IN_ACCT_MASTER

    def test_card_xref_and_account_but_no_customer(
        self, repo: InMemoryAccountRepository
    ) -> None:
        repo.add_xref(
            make_xref(card_num="5555000000000002", acct_id="00000000055", cust_id="000000088")
        )
        repo.add_account(make_account(acct_id="00000000055"))
        svc = AccountLookupService(repo)
        with pytest.raises(HTTPException) as exc_info:
            svc.lookup_by_card_number("5555000000000002")
        assert exc_info.value.status_code == 404
        assert exc_info.value.detail == ERR_NOT_IN_CUST_MASTER
