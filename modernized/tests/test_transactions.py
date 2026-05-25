"""Tests covering each validation rule from CBTRN02C.cbl."""

from decimal import Decimal

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.models.account import Account
from app.models.card_xref import CardXref
from app.models.rejected_transaction import RejectedTransaction
from app.models.tran_cat_balance import TranCatBalance
from app.models.transaction import Transaction


def _valid_payload(**overrides: object) -> dict:
    base = {
        "tran_id": "0000000000000001",
        "type_cd": "01",
        "cat_cd": 1,
        "source": "POS TERM",
        "description": "Test purchase",
        "amount": "100.00",
        "merchant_id": 800000000,
        "merchant_name": "Test Merchant",
        "merchant_city": "Test City",
        "merchant_zip": "12345",
        "card_num": "1234567890123456",
        "orig_ts": "2025-06-10 19:27:53.000000",
    }
    base.update(overrides)
    return base


# -----------------------------------------------------------------------
# 1. test_process_valid_transaction
# -----------------------------------------------------------------------
@pytest.mark.usefixtures("seed_test_data")
def test_process_valid_transaction(client: TestClient, db_session: Session) -> None:
    resp = client.post("/api/v1/transactions/process", json=_valid_payload())
    assert resp.status_code == 200
    data = resp.json()
    assert data["status"] == "posted"
    assert data["tran_id"] == "0000000000000001"

    # Verify transaction was created
    tran = db_session.query(Transaction).filter_by(tran_id="0000000000000001").first()
    assert tran is not None
    assert tran.proc_ts is not None
    # DB2 timestamp format: YYYY-MM-DD-HH.MM.SS.NN0000
    assert len(tran.proc_ts) == 26
    assert tran.proc_ts[4] == "-"
    assert tran.proc_ts[10] == "-"
    assert tran.proc_ts[13] == "."

    # Verify account balances updated
    acct = db_session.query(Account).filter_by(acct_id=12345678901).first()
    assert Decimal(str(acct.curr_bal)) == Decimal("100.00")
    assert Decimal(str(acct.curr_cyc_credit)) == Decimal("100.00")

    # Verify tran_cat_balance created
    tcb = (
        db_session.query(TranCatBalance)
        .filter_by(acct_id=12345678901, type_cd="01", cat_cd=1)
        .first()
    )
    assert tcb is not None
    assert Decimal(str(tcb.balance)) == Decimal("100.00")


# -----------------------------------------------------------------------
# 2. test_reject_code_100_invalid_card
# -----------------------------------------------------------------------
@pytest.mark.usefixtures("seed_test_data")
def test_reject_code_100_invalid_card(client: TestClient) -> None:
    payload = _valid_payload(card_num="0000000000000000")
    resp = client.post("/api/v1/transactions/process", json=payload)
    assert resp.status_code == 422
    detail = resp.json()["detail"]
    assert detail["rejection_code"] == 100
    assert detail["rejection_reason"] == "INVALID CARD NUMBER FOUND"


# -----------------------------------------------------------------------
# 3. test_reject_code_101_account_not_found
# -----------------------------------------------------------------------
def test_reject_code_101_account_not_found(
    client: TestClient, db_session: Session
) -> None:
    # Create xref pointing to non-existent account
    xref = CardXref(card_num="1111111111111111", cust_id=111111111, acct_id=11111111111)
    db_session.add(xref)
    db_session.commit()

    payload = _valid_payload(card_num="1111111111111111")
    resp = client.post("/api/v1/transactions/process", json=payload)
    assert resp.status_code == 422
    detail = resp.json()["detail"]
    assert detail["rejection_code"] == 101
    assert detail["rejection_reason"] == "ACCOUNT RECORD NOT FOUND"


# -----------------------------------------------------------------------
# 4. test_reject_code_102_overlimit
# -----------------------------------------------------------------------
@pytest.mark.usefixtures("seed_test_data")
def test_reject_code_102_overlimit(client: TestClient) -> None:
    payload = _valid_payload(amount="6000.00")
    resp = client.post("/api/v1/transactions/process", json=payload)
    assert resp.status_code == 422
    detail = resp.json()["detail"]
    assert detail["rejection_code"] == 102
    assert detail["rejection_reason"] == "OVERLIMIT TRANSACTION"


# -----------------------------------------------------------------------
# 5. test_reject_code_103_expired_account
# -----------------------------------------------------------------------
@pytest.mark.usefixtures("seed_test_data")
def test_reject_code_103_expired_account(client: TestClient) -> None:
    payload = _valid_payload(
        card_num="9999999999999999",
        orig_ts="2025-06-10 19:27:53.000000",
    )
    resp = client.post("/api/v1/transactions/process", json=payload)
    assert resp.status_code == 422
    detail = resp.json()["detail"]
    assert detail["rejection_code"] == 103
    assert detail["rejection_reason"] == "TRANSACTION RECEIVED AFTER ACCT EXPIRATION"


# -----------------------------------------------------------------------
# 6. test_process_batch_mixed
# -----------------------------------------------------------------------
@pytest.mark.usefixtures("seed_test_data")
def test_process_batch_mixed(client: TestClient, db_session: Session) -> None:
    transactions = [
        _valid_payload(tran_id=f"VALID{i:011d}", amount="100.00")
        for i in range(3)
    ] + [
        _valid_payload(tran_id="INVALID_CARD0001", card_num="0000000000000000"),
        _valid_payload(tran_id="OVERLIMIT0000001", amount="6000.00"),
    ]
    upload_resp = client.post(
        "/api/v1/transactions/upload",
        json={"transactions": transactions},
    )
    assert upload_resp.status_code == 200
    assert upload_resp.json()["inserted"] == 5

    batch_resp = client.post("/api/v1/transactions/process-batch", json={})
    assert batch_resp.status_code == 200
    data = batch_resp.json()
    assert data["transactions_processed"] == 5
    assert data["transactions_rejected"] == 2

    rejects = db_session.query(RejectedTransaction).all()
    assert len(rejects) == 2


# -----------------------------------------------------------------------
# 7. test_tcatbal_create_vs_update
# -----------------------------------------------------------------------
@pytest.mark.usefixtures("seed_test_data")
def test_tcatbal_create_vs_update(client: TestClient, db_session: Session) -> None:
    # First transaction creates tcatbal
    resp1 = client.post(
        "/api/v1/transactions/process",
        json=_valid_payload(tran_id="TCAT_CREATE_0001", amount="200.00"),
    )
    assert resp1.status_code == 200

    tcb = (
        db_session.query(TranCatBalance)
        .filter_by(acct_id=12345678901, type_cd="01", cat_cd=1)
        .first()
    )
    assert tcb is not None
    assert Decimal(str(tcb.balance)) == Decimal("200.00")

    # Second transaction with same category updates existing balance
    resp2 = client.post(
        "/api/v1/transactions/process",
        json=_valid_payload(tran_id="TCAT_UPDATE_0001", amount="300.00"),
    )
    assert resp2.status_code == 200

    db_session.expire_all()
    tcb = (
        db_session.query(TranCatBalance)
        .filter_by(acct_id=12345678901, type_cd="01", cat_cd=1)
        .first()
    )
    assert Decimal(str(tcb.balance)) == Decimal("500.00")


# -----------------------------------------------------------------------
# 8. test_account_balance_update_positive
# -----------------------------------------------------------------------
@pytest.mark.usefixtures("seed_test_data")
def test_account_balance_update_positive(
    client: TestClient, db_session: Session
) -> None:
    client.post(
        "/api/v1/transactions/process",
        json=_valid_payload(tran_id="POS_AMT_0000001", amount="250.00"),
    )
    db_session.expire_all()
    acct = db_session.query(Account).filter_by(acct_id=12345678901).first()
    assert Decimal(str(acct.curr_bal)) == Decimal("250.00")
    assert Decimal(str(acct.curr_cyc_credit)) == Decimal("250.00")
    assert Decimal(str(acct.curr_cyc_debit)) == Decimal("0.00")


# -----------------------------------------------------------------------
# 9. test_account_balance_update_negative
# -----------------------------------------------------------------------
@pytest.mark.usefixtures("seed_test_data")
def test_account_balance_update_negative(
    client: TestClient, db_session: Session
) -> None:
    client.post(
        "/api/v1/transactions/process",
        json=_valid_payload(tran_id="NEG_AMT_0000001", amount="-50.00"),
    )
    db_session.expire_all()
    acct = db_session.query(Account).filter_by(acct_id=12345678901).first()
    assert Decimal(str(acct.curr_bal)) == Decimal("-50.00")
    assert Decimal(str(acct.curr_cyc_credit)) == Decimal("0.00")
    assert Decimal(str(acct.curr_cyc_debit)) == Decimal("-50.00")


# -----------------------------------------------------------------------
# 10. test_upload_and_process
# -----------------------------------------------------------------------
@pytest.mark.usefixtures("seed_test_data")
def test_upload_and_process(client: TestClient) -> None:
    transactions = [
        _valid_payload(tran_id=f"FLOW{i:012d}", amount="50.00")
        for i in range(3)
    ]
    upload_resp = client.post(
        "/api/v1/transactions/upload",
        json={"transactions": transactions},
    )
    assert upload_resp.status_code == 200
    assert upload_resp.json()["inserted"] == 3

    batch_resp = client.post("/api/v1/transactions/process-batch", json={})
    assert batch_resp.status_code == 200
    data = batch_resp.json()
    assert data["transactions_processed"] == 3
    assert data["transactions_rejected"] == 0

    # Check list endpoint
    list_resp = client.get("/api/v1/transactions")
    assert list_resp.status_code == 200
    assert list_resp.json()["total"] == 3

    # Check single get
    get_resp = client.get(f"/api/v1/transactions/FLOW000000000000")
    assert get_resp.status_code == 200
    assert get_resp.json()["tran_id"] == "FLOW000000000000"

    # No rejects
    rej_resp = client.get("/api/v1/transactions/rejected")
    assert rej_resp.status_code == 200
    assert len(rej_resp.json()) == 0
