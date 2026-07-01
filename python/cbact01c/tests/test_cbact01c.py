"""Tests for the CBACT01C Python migration.

Seeds known accounts into an in-memory SQLite database, runs the batch, and
asserts the printed output matches the labelled format of paragraph
1100-DISPLAY-ACCT-RECORD -- including 2-decimal-place rendering of money.
"""

from __future__ import annotations

from decimal import Decimal

import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from cbact01c.account import Account, Base
from cbact01c.cbact01c import (
    SEPARATOR,
    format_account_record,
    iter_accounts,
    run_batch,
)
from cbact01c.copybook import decode_zoned_decimal, parse_account_record


@pytest.fixture()
def session_factory():
    engine = create_engine("sqlite:///:memory:")
    Base.metadata.create_all(engine)
    return sessionmaker(bind=engine)


def _sample_accounts() -> list[Account]:
    return [
        Account(
            acct_id=2,
            acct_active_status="N",
            acct_curr_bal=Decimal("-19.40"),
            acct_credit_limit=Decimal("5000.00"),
            acct_cash_credit_limit=Decimal("1000.00"),
            acct_open_date="2013-06-19",
            acct_expiraion_date="2024-08-11",
            acct_reissue_date="2024-08-11",
            acct_curr_cyc_credit=Decimal("0.00"),
            acct_curr_cyc_debit=Decimal("12.34"),
            acct_addr_zip="12345",
            acct_group_id="GROUP2",
        ),
        Account(
            acct_id=1,
            acct_active_status="Y",
            acct_curr_bal=Decimal("194.00"),
            acct_credit_limit=Decimal("2020.00"),
            acct_cash_credit_limit=Decimal("1020.00"),
            acct_open_date="2014-11-20",
            acct_expiraion_date="2025-05-20",
            acct_reissue_date="2025-05-20",
            acct_curr_cyc_credit=Decimal("0.00"),
            acct_curr_cyc_debit=Decimal("0.00"),
            acct_addr_zip="A000000000",
            acct_group_id="",
        ),
    ]


def _seed(session_factory, accounts: list[Account]) -> None:
    with session_factory() as session:
        session.add_all(accounts)
        session.commit()


def test_run_batch_output_matches_expected_format(session_factory):
    _seed(session_factory, _sample_accounts())
    lines: list[str] = []
    with session_factory() as session:
        rc = run_batch(iter_accounts(session), out=lines.append, err=lines.append)

    assert rc == 0

    expected = [
        "START OF EXECUTION OF PROGRAM CBACT01C",
        # acct_id 1 comes first: ordered by ACCT-ID ascending.
        "ACCT-ID                 :00000000001",
        "ACCT-ACTIVE-STATUS      :Y",
        "ACCT-CURR-BAL           :194.00",
        "ACCT-CREDIT-LIMIT       :2020.00",
        "ACCT-CASH-CREDIT-LIMIT  :1020.00",
        "ACCT-OPEN-DATE          :2014-11-20",
        "ACCT-EXPIRAION-DATE     :2025-05-20",
        "ACCT-REISSUE-DATE       :2025-05-20",
        "ACCT-CURR-CYC-CREDIT    :0.00",
        "ACCT-CURR-CYC-DEBIT     :0.00",
        "ACCT-GROUP-ID           :",
        SEPARATOR,
        "ACCT-ID                 :00000000002",
        "ACCT-ACTIVE-STATUS      :N",
        "ACCT-CURR-BAL           :-19.40",
        "ACCT-CREDIT-LIMIT       :5000.00",
        "ACCT-CASH-CREDIT-LIMIT  :1000.00",
        "ACCT-OPEN-DATE          :2013-06-19",
        "ACCT-EXPIRAION-DATE     :2024-08-11",
        "ACCT-REISSUE-DATE       :2024-08-11",
        "ACCT-CURR-CYC-CREDIT    :0.00",
        "ACCT-CURR-CYC-DEBIT     :12.34",
        "ACCT-GROUP-ID           :GROUP2",
        SEPARATOR,
        "END OF EXECUTION OF PROGRAM CBACT01C",
    ]
    assert lines == expected


def test_ordered_by_acct_id_ascending(session_factory):
    _seed(session_factory, _sample_accounts())
    with session_factory() as session:
        ids = [a.acct_id for a in iter_accounts(session)]
    assert ids == [1, 2]


def test_monetary_fields_render_two_decimals():
    account = Account(
        acct_id=99,
        acct_active_status="Y",
        acct_curr_bal=Decimal("5"),
        acct_credit_limit=Decimal("0"),
        acct_cash_credit_limit=Decimal("1234567890.99"),
        acct_open_date="2020-01-01",
        acct_expiraion_date="2030-01-01",
        acct_reissue_date="2030-01-01",
        acct_curr_cyc_credit=Decimal("-0.01"),
        acct_curr_cyc_debit=Decimal("100.5"),
        acct_addr_zip="00000",
        acct_group_id="G",
    )
    lines = format_account_record(account)
    money_lines = [ln for ln in lines if ln.startswith("ACCT-CURR-BAL")]
    assert money_lines == ["ACCT-CURR-BAL           :5.00"]

    # every rendered money field has exactly two decimal places
    for label in (
        "ACCT-CURR-BAL",
        "ACCT-CREDIT-LIMIT",
        "ACCT-CASH-CREDIT-LIMIT",
        "ACCT-CURR-CYC-CREDIT",
        "ACCT-CURR-CYC-DEBIT",
    ):
        (line,) = [ln for ln in lines if ln.startswith(label)]
        value = line.split(":", 1)[1]
        assert value.split(".")[1] == "00" or len(value.split(".")[1]) == 2

    assert "ACCT-CASH-CREDIT-LIMIT  :1234567890.99" in lines
    assert "ACCT-CURR-CYC-CREDIT    :-0.01" in lines
    assert "ACCT-CURR-CYC-DEBIT     :100.50" in lines


def test_separator_line_is_49_dashes():
    account = _sample_accounts()[0]
    lines = format_account_record(account)
    assert lines[-1] == "-" * 49


def test_run_batch_error_returns_nonzero():
    from sqlalchemy.exc import OperationalError

    def broken() -> list[Account]:
        raise OperationalError("SELECT", {}, Exception("boom"))
        yield  # pragma: no cover - makes this a generator

    err_lines: list[str] = []
    out_lines: list[str] = []
    rc = run_batch(broken(), out=out_lines.append, err=err_lines.append)

    assert rc != 0
    assert "START OF EXECUTION OF PROGRAM CBACT01C" in out_lines
    assert "END OF EXECUTION OF PROGRAM CBACT01C" not in out_lines
    assert any("ERROR READING ACCOUNT FILE" in ln for ln in err_lines)
    assert any("ABENDING PROGRAM" in ln for ln in err_lines)


def test_decode_zoned_decimal_positive_and_negative():
    # '{' overpunch -> last digit 0, positive; V99 implied
    assert decode_zoned_decimal("00000001940{") == Decimal("194.00")
    # '}' -> last digit 0 negative ; 'J'..'R' -> 1..9 negative
    assert decode_zoned_decimal("0000000194}") == Decimal("-19.40")
    assert decode_zoned_decimal("00000000000J") == Decimal("-0.01")
    # plain trailing digit
    assert decode_zoned_decimal("00000000012") == Decimal("0.12")


def test_parse_account_record_roundtrip():
    # Build a 300-byte record matching CVACT01Y and parse it.
    record = (
        "00000000001"  # acct id
        "Y"  # status
        "00000001940{"  # curr bal 194.00
        "00000020200{"  # credit limit 2020.00
        "00000010200{"  # cash credit limit 1020.00
        "2014-11-20"  # open date
        "2025-05-20"  # expiration date
        "2025-05-20"  # reissue date
        "00000000000{"  # cyc credit 0.00
        "00000000000{"  # cyc debit 0.00
        "A000000000"  # zip
        "          "  # group id (blank)
    )
    record = record + " " * (300 - len(record))
    account = parse_account_record(record)
    assert account.acct_id == 1
    assert account.acct_active_status == "Y"
    assert account.acct_curr_bal == Decimal("194.00")
    assert account.acct_credit_limit == Decimal("2020.00")
    assert account.acct_cash_credit_limit == Decimal("1020.00")
    assert account.acct_addr_zip == "A000000000"
    assert account.acct_group_id == ""
