"""Unit tests for the account update logic in :mod:`cbtrn02c`.

These tests focus on the account-update path of the POSTTRAN batch job
(`2800-UPDATE-ACCOUNT-REC`) and the account validation that guards it
(`1500-B-LOOKUP-ACCT`):

* happy path        -- a valid account is updated and balances change correctly;
* invalid account   -- xref points at a missing account -> reason 101;
* credit-limit edges -- boundary conditions on the overlimit check (reason 102).
"""

from __future__ import annotations

import os
import sqlite3
import sys
from decimal import Decimal

import pytest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import cbtrn02c as m  # noqa: E402


# ---------------------------------------------------------------------------
# Fixtures / helpers
# ---------------------------------------------------------------------------
@pytest.fixture()
def conn():
    connection = sqlite3.connect(":memory:")
    connection.row_factory = sqlite3.Row
    m.create_schema(connection)
    yield connection
    connection.close()


def seed_xref(conn, card_num="1111111111111111", cust_id=1, acct_id=100):
    conn.execute(
        "INSERT INTO card_xref VALUES (?, ?, ?)", (card_num, cust_id, acct_id)
    )


def seed_account(
    conn,
    acct_id=100,
    curr_bal="0.00",
    credit_limit="1000.00",
    curr_cyc_credit="0.00",
    curr_cyc_debit="0.00",
    expiraion_date="2030-12-31",
):
    conn.execute(
        "INSERT INTO accounts (acct_id, acct_active_status, acct_curr_bal, "
        "acct_credit_limit, acct_cash_credit_limit, acct_open_date, "
        "acct_expiraion_date, acct_reissue_date, acct_curr_cyc_credit, "
        "acct_curr_cyc_debit, acct_addr_zip, acct_group_id) "
        "VALUES (?, 'Y', ?, ?, '500.00', '2020-01-01', ?, '2025-01-01', ?, ?, "
        "'12345', 'GRP')",
        (
            acct_id,
            curr_bal,
            credit_limit,
            expiraion_date,
            curr_cyc_credit,
            curr_cyc_debit,
        ),
    )


def seed_daly(
    conn,
    tran_id="T1",
    card_num="1111111111111111",
    amt="100.00",
    type_cd="01",
    cat_cd=1,
    orig_ts="2026-06-01-00.00.00.000000",
):
    conn.execute(
        "INSERT INTO daily_transactions (dalytran_id, dalytran_type_cd, "
        "dalytran_cat_cd, dalytran_source, dalytran_desc, dalytran_amt, "
        "dalytran_merchant_id, dalytran_merchant_name, dalytran_merchant_city, "
        "dalytran_merchant_zip, dalytran_card_num, dalytran_orig_ts, "
        "dalytran_proc_ts) VALUES (?, ?, ?, 'POS', 'desc', ?, 9, 'M', 'C', 'Z', "
        "?, ?, '')",
        (tran_id, type_cd, cat_cd, amt, card_num, orig_ts),
    )


def account_row(conn, acct_id=100):
    return conn.execute(
        "SELECT * FROM accounts WHERE acct_id = ?", (acct_id,)
    ).fetchone()


def reject_rows(conn):
    return conn.execute(
        "SELECT * FROM rejected_transactions ORDER BY id"
    ).fetchall()


# ---------------------------------------------------------------------------
# Happy path -- valid account update
# ---------------------------------------------------------------------------
def test_happy_path_positive_amount_updates_credit_balances(conn):
    seed_xref(conn)
    seed_account(conn, curr_bal="200.00", curr_cyc_credit="50.00")
    seed_daly(conn, amt="100.00")
    conn.commit()

    rc = m.PostTran(conn).run()

    assert rc == 0
    assert reject_rows(conn) == []
    acct = account_row(conn)
    # curr_bal += amt; positive amt -> curr_cyc_credit += amt; debit unchanged.
    assert Decimal(acct["acct_curr_bal"]) == Decimal("300.00")
    assert Decimal(acct["acct_curr_cyc_credit"]) == Decimal("150.00")
    assert Decimal(acct["acct_curr_cyc_debit"]) == Decimal("0.00")
    # Transaction was posted and a category balance was created.
    assert conn.execute("SELECT COUNT(*) FROM transactions").fetchone()[0] == 1
    tcb = conn.execute("SELECT tran_cat_bal FROM tran_cat_bal").fetchone()
    assert Decimal(tcb["tran_cat_bal"]) == Decimal("100.00")


def test_happy_path_negative_amount_updates_debit_balance(conn):
    seed_xref(conn)
    seed_account(conn, curr_bal="200.00", curr_cyc_debit="0.00")
    seed_daly(conn, amt="-30.00")
    conn.commit()

    rc = m.PostTran(conn).run()

    assert rc == 0
    acct = account_row(conn)
    # curr_bal += amt; negative amt -> curr_cyc_debit += amt (becomes negative).
    assert Decimal(acct["acct_curr_bal"]) == Decimal("170.00")
    assert Decimal(acct["acct_curr_cyc_credit"]) == Decimal("0.00")
    assert Decimal(acct["acct_curr_cyc_debit"]) == Decimal("-30.00")


def test_update_account_rec_direct_positive(conn):
    seed_account(conn, curr_bal="10.00", curr_cyc_credit="5.00")
    conn.commit()
    poster = m.PostTran(conn)
    daly = m.DalyTranRecord(dalytran_amt=Decimal("25.00"))

    poster._update_account_rec(daly, acct_id=100)

    acct = account_row(conn)
    assert Decimal(acct["acct_curr_bal"]) == Decimal("35.00")
    assert Decimal(acct["acct_curr_cyc_credit"]) == Decimal("30.00")
    assert Decimal(acct["acct_curr_cyc_debit"]) == Decimal("0.00")


# ---------------------------------------------------------------------------
# Invalid account number
# ---------------------------------------------------------------------------
def test_invalid_account_number_is_rejected_reason_101(conn):
    # xref resolves to acct 999 but no such account exists.
    seed_xref(conn, acct_id=999)
    seed_daly(conn, amt="100.00")
    conn.commit()

    rc = m.PostTran(conn).run()

    assert rc == 4
    rejects = reject_rows(conn)
    assert len(rejects) == 1
    assert rejects[0]["validation_fail_reason"] == m.REASON_ACCT_NOT_FOUND == 101
    assert rejects[0]["validation_fail_reason_desc"] == "ACCOUNT RECORD NOT FOUND"
    # Nothing posted, no account mutated (none exist).
    assert conn.execute("SELECT COUNT(*) FROM transactions").fetchone()[0] == 0


def test_update_account_rec_missing_account_abends(conn):
    poster = m.PostTran(conn)
    daly = m.DalyTranRecord(dalytran_amt=Decimal("1.00"))
    with pytest.raises(m.AbendError):
        poster._update_account_rec(daly, acct_id=12345)


# ---------------------------------------------------------------------------
# Credit-limit boundary conditions (reason 102 overlimit)
# ---------------------------------------------------------------------------
# Overlimit rule: reject when  credit_limit < (cyc_credit - cyc_debit + amt).
# i.e. allowed while temp_bal <= credit_limit.
@pytest.mark.parametrize(
    "amt, expect_reject",
    [
        ("999.99", False),   # just under the limit
        ("1000.00", False),  # exactly at the limit -> allowed (>=)
        ("1000.01", True),   # one cent over -> reason 102
    ],
)
def test_credit_limit_boundary(conn, amt, expect_reject):
    seed_xref(conn)
    seed_account(
        conn,
        credit_limit="1000.00",
        curr_cyc_credit="0.00",
        curr_cyc_debit="0.00",
    )
    seed_daly(conn, amt=amt)
    conn.commit()

    rc = m.PostTran(conn).run()

    rejects = reject_rows(conn)
    if expect_reject:
        assert rc == 4
        assert len(rejects) == 1
        assert rejects[0]["validation_fail_reason"] == m.REASON_OVERLIMIT == 102
        assert rejects[0]["validation_fail_reason_desc"] == "OVERLIMIT TRANSACTION"
        assert conn.execute("SELECT COUNT(*) FROM transactions").fetchone()[0] == 0
    else:
        assert rc == 0
        assert rejects == []
        assert conn.execute("SELECT COUNT(*) FROM transactions").fetchone()[0] == 1


def test_credit_limit_uses_available_cycle_headroom(conn):
    # temp_bal = cyc_credit - cyc_debit + amt = 300 - 100 + 800 = 1000 == limit.
    seed_xref(conn)
    seed_account(
        conn,
        credit_limit="1000.00",
        curr_cyc_credit="300.00",
        curr_cyc_debit="100.00",
    )
    seed_daly(conn, amt="800.00")
    conn.commit()

    rc = m.PostTran(conn).run()

    assert rc == 0  # exactly at the limit is allowed
    assert reject_rows(conn) == []


def test_credit_limit_exceeded_with_cycle_headroom(conn):
    # temp_bal = 300 - 100 + 800.01 = 1000.01 > limit -> reason 102.
    seed_xref(conn)
    seed_account(
        conn,
        credit_limit="1000.00",
        curr_cyc_credit="300.00",
        curr_cyc_debit="100.00",
    )
    seed_daly(conn, amt="800.01")
    conn.commit()

    rc = m.PostTran(conn).run()

    assert rc == 4
    rejects = reject_rows(conn)
    assert len(rejects) == 1
    assert rejects[0]["validation_fail_reason"] == m.REASON_OVERLIMIT
