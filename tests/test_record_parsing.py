"""Template: Record-parsing tests for the OE129BC Python port.

These tests verify that fixed-length COBOL records can be correctly
created and their individual fields extracted using the test-utility
builders and the field-comparison helpers.
"""

from __future__ import annotations

import pytest

from tests.utils.record_builder import (
    build_account_record,
    build_card_record,
    build_card_xref_record,
    build_customer_record,
    build_daily_transaction_record,
    build_disclosure_group_record,
    build_tran_cat_balance_record,
    build_tran_cat_record,
    build_tran_type_record,
    build_transaction_record,
    build_user_security_record,
)
from tests.utils.record_comparator import (
    ACCOUNT_FIELDS,
    CARD_FIELDS,
    CARD_XREF_FIELDS,
    CUSTOMER_FIELDS,
    DAILY_TRANSACTION_FIELDS,
    DISCLOSURE_GROUP_FIELDS,
    TRAN_CAT_BALANCE_FIELDS,
    TRAN_CAT_FIELDS,
    TRAN_TYPE_FIELDS,
    TRANSACTION_FIELDS,
    USER_SECURITY_FIELDS,
    assert_records_equal,
    compare_records,
    extract_field,
)
from tests.data.sample_records import (
    VALID_ACCOUNT_ACTIVE,
    VALID_CARD_ACTIVE,
    VALID_CUSTOMER_PRIMARY,
    VALID_DAILY_TRANSACTION,
    VALID_DISCLOSURE_GROUP,
    VALID_TRAN_CAT_BALANCE,
    VALID_TRAN_CAT_GROCERY,
    VALID_TRAN_TYPE_SALE,
    VALID_TRANSACTION_SALE,
    VALID_USER_ADMIN,
    VALID_XREF_1,
)


# ═══════════════════════════════════════════════════════════════════════════
# Record length tests
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.unit
class TestRecordLengths:
    """Every built record must match its COBOL-defined RECLN."""

    def test_account_record_is_300_bytes(self):
        assert len(build_account_record()) == 300

    def test_card_record_is_150_bytes(self):
        assert len(build_card_record()) == 150

    def test_card_xref_record_is_50_bytes(self):
        assert len(build_card_xref_record()) == 50

    def test_customer_record_is_500_bytes(self):
        assert len(build_customer_record()) == 500

    def test_transaction_record_is_350_bytes(self):
        assert len(build_transaction_record()) == 350

    def test_daily_transaction_record_is_350_bytes(self):
        assert len(build_daily_transaction_record()) == 350

    def test_tran_type_record_is_60_bytes(self):
        assert len(build_tran_type_record()) == 60

    def test_tran_cat_record_is_60_bytes(self):
        assert len(build_tran_cat_record()) == 60

    def test_tran_cat_balance_record_is_50_bytes(self):
        assert len(build_tran_cat_balance_record()) == 50

    def test_disclosure_group_record_is_50_bytes(self):
        assert len(build_disclosure_group_record()) == 50

    def test_user_security_record_is_80_bytes(self):
        assert len(build_user_security_record()) == 80


# ═══════════════════════════════════════════════════════════════════════════
# Field extraction tests
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.unit
class TestAccountFieldExtraction:
    """Extracting individual fields from a known account record."""

    def test_acct_id_extraction(self):
        field = extract_field(VALID_ACCOUNT_ACTIVE, ACCOUNT_FIELDS[0])
        assert field == "12345678901"

    def test_active_status_extraction(self):
        field = extract_field(VALID_ACCOUNT_ACTIVE, ACCOUNT_FIELDS[1])
        assert field == "Y"

    def test_open_date_extraction(self):
        field = extract_field(VALID_ACCOUNT_ACTIVE, ACCOUNT_FIELDS[5])
        assert field == "2018-03-15"

    def test_group_id_extraction(self):
        field = extract_field(VALID_ACCOUNT_ACTIVE, ACCOUNT_FIELDS[11])
        assert field.strip() == "PREMIUM01"


@pytest.mark.unit
class TestCardFieldExtraction:
    """Extracting individual fields from a known card record."""

    def test_card_num_extraction(self):
        field = extract_field(VALID_CARD_ACTIVE, CARD_FIELDS[0])
        assert field == "4111111111111111"

    def test_cvv_extraction(self):
        field = extract_field(VALID_CARD_ACTIVE, CARD_FIELDS[2])
        assert field == "123"

    def test_active_status_extraction(self):
        field = extract_field(VALID_CARD_ACTIVE, CARD_FIELDS[5])
        assert field == "Y"


@pytest.mark.unit
class TestCardXrefFieldExtraction:
    """Extracting individual fields from a known card XREF record."""

    def test_card_num_extraction(self):
        field = extract_field(VALID_XREF_1, CARD_XREF_FIELDS[0])
        assert field == "4111111111111111"

    def test_cust_id_extraction(self):
        field = extract_field(VALID_XREF_1, CARD_XREF_FIELDS[1])
        assert field == "100000001"

    def test_acct_id_extraction(self):
        field = extract_field(VALID_XREF_1, CARD_XREF_FIELDS[2])
        assert field == "12345678901"


@pytest.mark.unit
class TestCustomerFieldExtraction:
    """Extracting individual fields from a known customer record."""

    def test_cust_id_extraction(self):
        field = extract_field(VALID_CUSTOMER_PRIMARY, CUSTOMER_FIELDS[0])
        assert field == "100000001"

    def test_first_name_extraction(self):
        field = extract_field(VALID_CUSTOMER_PRIMARY, CUSTOMER_FIELDS[1])
        assert field.strip() == "JOHN"

    def test_last_name_extraction(self):
        field = extract_field(VALID_CUSTOMER_PRIMARY, CUSTOMER_FIELDS[3])
        assert field.strip() == "SMITH"

    def test_state_code_extraction(self):
        field = extract_field(VALID_CUSTOMER_PRIMARY, CUSTOMER_FIELDS[7])
        assert field == "GA"

    def test_fico_score_extraction(self):
        field = extract_field(VALID_CUSTOMER_PRIMARY, CUSTOMER_FIELDS[17])
        assert field == "750"


@pytest.mark.unit
class TestTransactionFieldExtraction:
    """Extracting individual fields from a known transaction record."""

    def test_tran_id_extraction(self):
        field = extract_field(VALID_TRANSACTION_SALE, TRANSACTION_FIELDS[0])
        assert field == "0000000000000001"

    def test_type_cd_extraction(self):
        field = extract_field(VALID_TRANSACTION_SALE, TRANSACTION_FIELDS[1])
        assert field == "SA"

    def test_source_extraction(self):
        field = extract_field(VALID_TRANSACTION_SALE, TRANSACTION_FIELDS[3])
        assert field.strip() == "POS"


@pytest.mark.unit
class TestUserSecurityFieldExtraction:
    """Extracting individual fields from a known user-security record."""

    def test_user_id_extraction(self):
        field = extract_field(VALID_USER_ADMIN, USER_SECURITY_FIELDS[0])
        assert field == "ADMIN001"

    def test_user_type_extraction(self):
        field = extract_field(VALID_USER_ADMIN, USER_SECURITY_FIELDS[4])
        assert field == "A"

    def test_password_extraction(self):
        field = extract_field(VALID_USER_ADMIN, USER_SECURITY_FIELDS[3])
        assert field == "PASSWORD"


# ═══════════════════════════════════════════════════════════════════════════
# Record comparison tests
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.unit
class TestRecordComparison:
    """compare_records / assert_records_equal utility behaviour."""

    def test_identical_records_produce_no_diffs(self):
        diffs = compare_records(VALID_ACCOUNT_ACTIVE, VALID_ACCOUNT_ACTIVE, ACCOUNT_FIELDS)
        assert diffs == []

    def test_different_records_produce_diffs(self):
        other = build_account_record(acct_id=99999999999, active_status="N")
        diffs = compare_records(VALID_ACCOUNT_ACTIVE, other, ACCOUNT_FIELDS)
        assert len(diffs) > 0
        field_names = {d.field_name for d in diffs}
        assert "ACCT-ID" in field_names

    def test_assert_records_equal_passes_for_identical(self):
        assert_records_equal(
            VALID_CARD_ACTIVE, VALID_CARD_ACTIVE, CARD_FIELDS,
        )

    def test_assert_records_equal_raises_for_different(self):
        other = build_card_record(card_num="9999999999999999")
        with pytest.raises(AssertionError, match="CARD-NUM"):
            assert_records_equal(VALID_CARD_ACTIVE, other, CARD_FIELDS)


# ═══════════════════════════════════════════════════════════════════════════
# Round-trip build → extract tests
# ═══════════════════════════════════════════════════════════════════════════


@pytest.mark.unit
class TestRoundTrip:
    """Values fed into builders survive extraction unchanged."""

    def test_account_round_trip(self):
        rec = build_account_record(acct_id=11111111111, active_status="N")
        assert extract_field(rec, ACCOUNT_FIELDS[0]) == "11111111111"
        assert extract_field(rec, ACCOUNT_FIELDS[1]) == "N"

    def test_card_round_trip(self):
        rec = build_card_record(card_num="1234567890123456", cvv=999)
        assert extract_field(rec, CARD_FIELDS[0]) == "1234567890123456"
        assert extract_field(rec, CARD_FIELDS[2]) == "999"

    def test_customer_round_trip(self):
        rec = build_customer_record(
            cust_id=555555555,
            first_name="ALICE",
            last_name="WONDER",
        )
        assert extract_field(rec, CUSTOMER_FIELDS[0]) == "555555555"
        assert extract_field(rec, CUSTOMER_FIELDS[1]).strip() == "ALICE"
        assert extract_field(rec, CUSTOMER_FIELDS[3]).strip() == "WONDER"

    def test_user_security_round_trip(self):
        rec = build_user_security_record(
            user_id="TESTER01",
            first_name="TEST",
            last_name="USER",
            password="MYPASS01",
            user_type="U",
        )
        assert extract_field(rec, USER_SECURITY_FIELDS[0]) == "TESTER01"
        assert extract_field(rec, USER_SECURITY_FIELDS[4]) == "U"
