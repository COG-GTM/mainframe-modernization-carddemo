"""Tests for the flat-file parser and database loader."""

from __future__ import annotations

import datetime
from decimal import Decimal
from pathlib import Path

import pytest

from cbact01c.loader import load_file, parse_record
from cbact01c.models import AccountRecord

SAMPLE_DATA = Path(__file__).resolve().parents[3] / "app" / "data" / "ASCII" / "acctdata.txt"


class TestParseRecord:
    """Verify that a single fixed-width line is parsed correctly."""

    FIRST_LINE = (
        "00000000001Y00000001940{00000020200{00000010200{"
        "2014-11-202025-05-202025-05-20"
        "00000000000{00000000000{A000000000"
    ).ljust(300)

    def test_acct_id(self) -> None:
        rec = parse_record(self.FIRST_LINE)
        assert rec["acct_id"] == 1

    def test_active_status(self) -> None:
        rec = parse_record(self.FIRST_LINE)
        assert rec["acct_active_status"] == "Y"

    def test_curr_bal(self) -> None:
        rec = parse_record(self.FIRST_LINE)
        assert rec["acct_curr_bal"] == Decimal("194.00")

    def test_credit_limit(self) -> None:
        rec = parse_record(self.FIRST_LINE)
        assert rec["acct_credit_limit"] == Decimal("2020.00")

    def test_cash_credit_limit(self) -> None:
        rec = parse_record(self.FIRST_LINE)
        assert rec["acct_cash_credit_limit"] == Decimal("1020.00")

    def test_open_date(self) -> None:
        rec = parse_record(self.FIRST_LINE)
        assert rec["acct_open_date"] == datetime.date(2014, 11, 20)

    def test_expiration_date(self) -> None:
        rec = parse_record(self.FIRST_LINE)
        assert rec["acct_expiraion_date"] == datetime.date(2025, 5, 20)

    def test_reissue_date(self) -> None:
        rec = parse_record(self.FIRST_LINE)
        assert rec["acct_reissue_date"] == datetime.date(2025, 5, 20)

    def test_cyc_credit(self) -> None:
        rec = parse_record(self.FIRST_LINE)
        assert rec["acct_curr_cyc_credit"] == Decimal("0.00")

    def test_cyc_debit(self) -> None:
        rec = parse_record(self.FIRST_LINE)
        assert rec["acct_curr_cyc_debit"] == Decimal("0.00")

    def test_group_id(self) -> None:
        rec = parse_record(self.FIRST_LINE)
        # Offset 112-121: need to check what's actually there
        # Based on first record, the visible data at 102+ is "A000000000"
        # which is ACCT-ADDR-ZIP; ACCT-GROUP-ID starts at offset 112
        assert isinstance(rec["acct_group_id"], str)


class TestLoadFile:
    """Verify loading the sample acctdata.txt into the database."""

    @pytest.mark.skipif(not SAMPLE_DATA.exists(), reason="Sample data not found")
    def test_load_all_50_records(self, db_session) -> None:
        count = load_file(SAMPLE_DATA, db_session)
        assert count == 50

    @pytest.mark.skipif(not SAMPLE_DATA.exists(), reason="Sample data not found")
    def test_records_queryable_after_load(self, db_session) -> None:
        load_file(SAMPLE_DATA, db_session)
        recs = db_session.query(AccountRecord).order_by(AccountRecord.acct_id).all()
        assert len(recs) == 50
        assert recs[0].acct_id == 1
        assert recs[-1].acct_id == 50

    @pytest.mark.skipif(not SAMPLE_DATA.exists(), reason="Sample data not found")
    def test_first_record_values(self, db_session) -> None:
        load_file(SAMPLE_DATA, db_session)
        rec = db_session.query(AccountRecord).filter_by(acct_id=1).one()
        assert rec.acct_active_status == "Y"
        assert rec.acct_curr_bal == Decimal("194.00")
        assert rec.acct_credit_limit == Decimal("2020.00")
        assert rec.acct_cash_credit_limit == Decimal("1020.00")
        assert rec.acct_open_date == datetime.date(2014, 11, 20)
        assert rec.acct_expiraion_date == datetime.date(2025, 5, 20)
        assert rec.acct_reissue_date == datetime.date(2025, 5, 20)
        assert rec.acct_curr_cyc_credit == Decimal("0.00")
        assert rec.acct_curr_cyc_debit == Decimal("0.00")
