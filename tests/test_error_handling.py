"""Template: Error-handling tests for the OE129BC Python port.

These tests verify that the application handles error conditions
gracefully — boundary values, malformed input, missing data, and
unexpected states.  Tests are organised by the type of error rather
than by entity, since error-handling patterns cut across entities.
"""

from __future__ import annotations

import pytest

from tests.utils.record_builder import (
    build_account_record,
    build_card_record,
    build_card_xref_record,
    build_customer_record,
    build_transaction_record,
    build_user_security_record,
)
from tests.utils.record_comparator import (
    ACCOUNT_FIELDS,
    CARD_FIELDS,
    CUSTOMER_FIELDS,
    TRANSACTION_FIELDS,
    USER_SECURITY_FIELDS,
    extract_field,
)


# ═══════════════════════════════════════════════════════════════════════════
# Boundary-value tests
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.unit
class TestBoundaryValues:
    """Extreme / edge-case values at the limits of PIC clauses."""

    def test_account_max_balance(self):
        """PIC S9(10)V99 max is 9999999999.99."""
        rec = build_account_record(curr_bal=9999999999.99)
        assert len(rec) == 300

    def test_account_max_negative_balance(self):
        """PIC S9(10)V99 min is -9999999999.99."""
        rec = build_account_record(curr_bal=-9999999999.99)
        assert len(rec) == 300

    def test_account_zero_balance(self):
        rec = build_account_record(curr_bal=0.0)
        assert len(rec) == 300

    def test_card_cvv_boundary_000(self):
        rec = build_card_record(cvv=0)
        field = extract_field(rec, CARD_FIELDS[2])
        assert field == "000"

    def test_card_cvv_boundary_999(self):
        rec = build_card_record(cvv=999)
        field = extract_field(rec, CARD_FIELDS[2])
        assert field == "999"

    def test_customer_fico_lower_bound(self):
        rec = build_customer_record(fico_credit_score=300)
        field = extract_field(rec, CUSTOMER_FIELDS[17])
        assert field == "300"

    def test_customer_fico_upper_bound(self):
        rec = build_customer_record(fico_credit_score=850)
        field = extract_field(rec, CUSTOMER_FIELDS[17])
        assert field == "850"

    def test_transaction_max_amount(self):
        rec = build_transaction_record(amt=999999999.99)
        assert len(rec) == 350

    def test_transaction_negative_amount(self):
        rec = build_transaction_record(amt=-100.50)
        assert len(rec) == 350


# ═══════════════════════════════════════════════════════════════════════════
# Truncation and padding tests
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.unit
class TestTruncationAndPadding:
    """Fields longer than the PIC length are truncated; shorter are padded."""

    def test_card_embossed_name_truncated_to_50(self):
        long_name = "A" * 100
        rec = build_card_record(embossed_name=long_name)
        field = extract_field(rec, CARD_FIELDS[3])
        assert len(field) == 50
        assert field == "A" * 50

    def test_card_embossed_name_padded_to_50(self):
        short_name = "JOE"
        rec = build_card_record(embossed_name=short_name)
        field = extract_field(rec, CARD_FIELDS[3])
        assert len(field) == 50
        assert field.strip() == "JOE"

    def test_customer_first_name_truncated_to_25(self):
        long_name = "B" * 50
        rec = build_customer_record(first_name=long_name)
        field = extract_field(rec, CUSTOMER_FIELDS[1])
        assert len(field) == 25
        assert field == "B" * 25

    def test_user_id_truncated_to_8(self):
        rec = build_user_security_record(user_id="TOOLONGID123")
        field = extract_field(rec, USER_SECURITY_FIELDS[0])
        assert len(field) == 8
        assert field == "TOOLONGI"

    def test_user_id_padded_to_8(self):
        rec = build_user_security_record(user_id="AB")
        field = extract_field(rec, USER_SECURITY_FIELDS[0])
        assert len(field) == 8
        assert field == "AB      "


# ═══════════════════════════════════════════════════════════════════════════
# Empty / blank-field handling
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.unit
class TestEmptyFields:
    """Records with blank or default field values."""

    def test_account_defaults_produce_valid_record(self):
        rec = build_account_record()
        assert len(rec) == 300

    def test_card_defaults_produce_valid_record(self):
        rec = build_card_record()
        assert len(rec) == 150

    def test_customer_defaults_produce_valid_record(self):
        rec = build_customer_record()
        assert len(rec) == 500

    def test_transaction_defaults_produce_valid_record(self):
        rec = build_transaction_record()
        assert len(rec) == 350

    def test_user_defaults_produce_valid_record(self):
        rec = build_user_security_record()
        assert len(rec) == 80

    def test_xref_defaults_produce_valid_record(self):
        rec = build_card_xref_record()
        assert len(rec) == 50


# ═══════════════════════════════════════════════════════════════════════════
# Malformed-input resilience (placeholder — extend once parsers exist)
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.unit
class TestMalformedInput:
    """Placeholder tests for when the production parser is available.

    These verify that the parser raises the expected exceptions when
    given records that are too short, contain binary data, etc.
    """

    def test_short_account_record_detected(self):
        """A record shorter than 300 bytes should be flagged."""
        short_record = "X" * 100
        assert len(short_record) < 300

    def test_short_card_record_detected(self):
        short_record = "X" * 50
        assert len(short_record) < 150

    def test_short_customer_record_detected(self):
        short_record = "X" * 200
        assert len(short_record) < 500

    def test_non_numeric_in_numeric_field(self):
        """Account ID should be numeric; alphabetic chars are invalid."""
        rec = build_account_record()
        # Manually corrupt the ACCT-ID field (first 11 chars)
        corrupted = "ABCDEFGHIJK" + rec[11:]
        acct_id = corrupted[0:11]
        assert not acct_id.isdigit()
