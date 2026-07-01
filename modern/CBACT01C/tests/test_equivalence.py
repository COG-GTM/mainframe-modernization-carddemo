"""Equivalence tests: validate that the modern Python batch produces output
functionally equivalent to the original COBOL CBACT01C program.

Tests cover:
  - Record count parity (50 records from sample data)
  - Field-level value parity for every record
  - Display output format parity (matching COBOL DISPLAY verb output)
  - Sequential ordering by ACCT-ID (matching VSAM KSDS access)
  - Start/end banner messages
  - Audit trail completeness (every record from source appears in output)
"""

from __future__ import annotations

import datetime
import re
from decimal import Decimal
from pathlib import Path

import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import Session

from cbact01c.cli import run_batch
from cbact01c.loader import load_file, parse_record
from cbact01c.models import AccountRecord, Base

SAMPLE_DATA = Path(__file__).resolve().parents[3] / "app" / "data" / "ASCII" / "acctdata.txt"

# Expected values for first and last records, derived from manual parsing
EXPECTED_FIRST = {
    "acct_id": 1,
    "acct_active_status": "Y",
    "acct_curr_bal": Decimal("194.00"),
    "acct_credit_limit": Decimal("2020.00"),
    "acct_cash_credit_limit": Decimal("1020.00"),
    "acct_open_date": datetime.date(2014, 11, 20),
    "acct_expiraion_date": datetime.date(2025, 5, 20),
    "acct_reissue_date": datetime.date(2025, 5, 20),
    "acct_curr_cyc_credit": Decimal("0.00"),
    "acct_curr_cyc_debit": Decimal("0.00"),
}

EXPECTED_LAST = {
    "acct_id": 50,
    "acct_active_status": "Y",
    "acct_curr_bal": Decimal("492.00"),
    "acct_credit_limit": Decimal("6169.00"),
    "acct_cash_credit_limit": Decimal("4587.00"),
    "acct_open_date": datetime.date(2011, 4, 22),
    "acct_expiraion_date": datetime.date(2023, 3, 9),
    "acct_reissue_date": datetime.date(2023, 3, 9),
    "acct_curr_cyc_credit": Decimal("0.00"),
    "acct_curr_cyc_debit": Decimal("0.00"),
}

pytestmark = pytest.mark.skipif(not SAMPLE_DATA.exists(), reason="Sample data not found")


@pytest.fixture(scope="module")
def loaded_engine():
    """Create a temporary DB, load sample data, yield the engine."""
    import os

    dsn = os.environ.get("TEST_DATABASE_URL", "postgresql:///carddemo_test")
    engine = create_engine(dsn)

    from sqlalchemy_utils import create_database, database_exists

    if not database_exists(engine.url):
        create_database(engine.url)

    Base.metadata.drop_all(engine)
    Base.metadata.create_all(engine)

    with Session(engine) as session:
        load_file(SAMPLE_DATA, session)

    yield engine

    Base.metadata.drop_all(engine)
    engine.dispose()


@pytest.fixture()
def all_records(loaded_engine):
    with Session(loaded_engine) as session:
        return (
            session.query(AccountRecord)
            .order_by(AccountRecord.acct_id)
            .all()
        )


class TestRecordCountParity:
    """The modern implementation must process the same number of records."""

    def test_50_records_loaded(self, all_records) -> None:
        assert len(all_records) == 50


class TestFieldValueParity:
    """Every field in the first and last records must match the COBOL-equivalent values."""

    def test_first_record(self, all_records) -> None:
        rec = all_records[0]
        for field, expected in EXPECTED_FIRST.items():
            actual = getattr(rec, field)
            assert actual == expected, f"First record {field}: {actual!r} != {expected!r}"

    def test_last_record(self, all_records) -> None:
        rec = all_records[-1]
        for field, expected in EXPECTED_LAST.items():
            actual = getattr(rec, field)
            assert actual == expected, f"Last record {field}: {actual!r} != {expected!r}"


class TestSequentialOrdering:
    """Records must be ordered by ACCT-ID ascending (VSAM KSDS key order)."""

    def test_monotonically_increasing_ids(self, all_records) -> None:
        ids = [r.acct_id for r in all_records]
        assert ids == sorted(ids)
        assert ids == list(range(1, 51))


class TestDisplayOutputParity:
    """The display output must match the COBOL DISPLAY verb format."""

    @pytest.fixture()
    def output_lines(self, loaded_engine) -> list[str]:
        lines: list[str] = []
        rc = run_batch(str(loaded_engine.url), output=lines)
        assert rc == 0
        return lines

    def test_start_banner(self, output_lines) -> None:
        assert output_lines[0] == "START OF EXECUTION OF PROGRAM CBACT01C"

    def test_end_banner(self, output_lines) -> None:
        assert output_lines[-1] == "END OF EXECUTION OF PROGRAM CBACT01C"

    def test_separator_lines(self, output_lines) -> None:
        separators = [line for line in output_lines if line == "-" * 49]
        assert len(separators) == 50

    def test_acct_id_label_format(self, output_lines) -> None:
        id_lines = [line for line in output_lines if line.startswith("ACCT-ID")]
        assert len(id_lines) == 50
        assert id_lines[0].startswith("ACCT-ID                 :")

    def test_fields_per_record(self, output_lines) -> None:
        """Each record block has 11 field lines + 1 separator = 12 lines."""
        body = output_lines[1:-1]  # strip banners
        assert len(body) == 50 * 12


class TestAuditParity:
    """Every source record must appear in the output — no silent drops."""

    def test_all_source_ids_in_output(self, loaded_engine) -> None:
        lines: list[str] = []
        run_batch(str(loaded_engine.url), output=lines)

        output_ids: set[int] = set()
        for line in lines:
            m = re.match(r"ACCT-ID\s+:(\d+)", line)
            if m:
                output_ids.add(int(m.group(1)))

        with SAMPLE_DATA.open() as fh:
            source_ids = {parse_record(line)["acct_id"] for line in fh if line.strip()}

        assert output_ids == source_ids


class TestRoundTripParity:
    """Parse → load → query must preserve every field for every record."""

    def test_all_fields_round_trip(self, loaded_engine) -> None:
        with SAMPLE_DATA.open() as fh:
            source = [parse_record(line) for line in fh if line.strip()]

        with Session(loaded_engine) as session:
            db_recs = (
                session.query(AccountRecord)
                .order_by(AccountRecord.acct_id)
                .all()
            )

        for src, db in zip(source, db_recs):
            assert src["acct_id"] == db.acct_id
            assert src["acct_active_status"] == db.acct_active_status
            assert src["acct_curr_bal"] == db.acct_curr_bal
            assert src["acct_credit_limit"] == db.acct_credit_limit
            assert src["acct_cash_credit_limit"] == db.acct_cash_credit_limit
            assert src["acct_open_date"] == db.acct_open_date
            assert src["acct_expiraion_date"] == db.acct_expiraion_date
            assert src["acct_reissue_date"] == db.acct_reissue_date
            assert src["acct_curr_cyc_credit"] == db.acct_curr_cyc_credit
            assert src["acct_curr_cyc_debit"] == db.acct_curr_cyc_debit
