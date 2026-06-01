"""Unit tests for coactupc.py.

These tests exercise the business logic extracted from
app/cbl/COACTUPC.cbl (CICS transaction CAUP - account/customer update).
Each test class references the originating COBOL paragraph.
"""

import os
import sys
import unittest
from decimal import Decimal

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import coactupc


# ---------------------------------------------------------------------------
# Seed data helpers
# ---------------------------------------------------------------------------
SEED_CARD_NUM = "4111111111111111"
SEED_CUST_ID = 123456789
SEED_ACCT_ID = 12345678901


def seed_standard_data(conn):
    """Insert the standard xref/account/customer trio used by integration
    tests (mirrors the seed data described for COACTUPC)."""
    conn.execute(
        "INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id) "
        "VALUES (?, ?, ?)",
        (SEED_CARD_NUM, SEED_CUST_ID, SEED_ACCT_ID),
    )
    conn.execute(
        "INSERT INTO account (acct_id, acct_active_status, acct_curr_bal, "
        "acct_credit_limit, acct_cash_credit_limit, acct_open_date, "
        "acct_expiration_date, acct_reissue_date, acct_curr_cyc_credit, "
        "acct_curr_cyc_debit, acct_addr_zip, acct_group_id) "
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        (
            SEED_ACCT_ID, "Y", 1000.00, 5000.00, 2000.00,
            "2020-01-01", "2030-01-01", "2025-01-01", 0.00, 0.00,
            "12345", "DEFAULT",
        ),
    )
    conn.execute(
        "INSERT INTO customer (cust_id, cust_first_name, cust_middle_name, "
        "cust_last_name, cust_addr_line_1, cust_addr_line_2, cust_addr_line_3, "
        "cust_addr_state_cd, cust_addr_country_cd, cust_addr_zip, "
        "cust_phone_num_1, cust_phone_num_2, cust_ssn, cust_govt_issued_id, "
        "cust_dob_yyyy_mm_dd, cust_eft_account_id, cust_pri_card_holder_ind, "
        "cust_fico_credit_score) "
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        (
            SEED_CUST_ID, "John", "Q", "Doe", "1 Main St", "", "",
            "NY", "USA", "10001", "555-1111", "555-2222", 999001234,
            "ID123", "1980-05-15", "EFT1", "Y", 720,
        ),
    )
    conn.commit()


class TestValidateAccountId(unittest.TestCase):
    """Tests paragraph 1210-EDIT-ACCOUNT (lines 1783-1822)."""

    def test_valid_account_number(self):
        valid, err = coactupc.validate_account_id("12345678901")
        self.assertTrue(valid)
        self.assertIsNone(err)

    def test_blank_account_number(self):
        for value in ("", None):
            valid, err = coactupc.validate_account_id(value)
            self.assertFalse(valid)
            self.assertIn("not provided", err)

    def test_spaces_account_number(self):
        valid, err = coactupc.validate_account_id("           ")
        self.assertFalse(valid)
        self.assertIn("not provided", err)

    def test_non_numeric_account_number(self):
        valid, err = coactupc.validate_account_id("1234567890A")
        self.assertFalse(valid)
        self.assertIn("11 digit", err)

    def test_all_zeros_account_number(self):
        valid, err = coactupc.validate_account_id("00000000000")
        self.assertFalse(valid)
        self.assertIn("Non-Zero", err)

    def test_short_account_number(self):
        # The COBOL field is 11 chars; shorter numeric input is zero-padded
        # and remains a valid non-zero account number.
        valid, err = coactupc.validate_account_id("12345")
        self.assertTrue(valid)
        self.assertIsNone(err)


class TestValidateCreditLimit(unittest.TestCase):
    """Tests paragraph 1250-EDIT-SIGNED-9V2 (lines 2180-2223),
    applied to the Credit Limit field (PIC S9(10)V99)."""

    FIELD = "Credit Limit"

    # Happy path
    def test_valid_positive_credit_limit(self):
        valid, err = coactupc.validate_signed_currency(self.FIELD, "5000.00")
        self.assertTrue(valid)
        self.assertIsNone(err)

    def test_valid_currency_format(self):
        valid, err = coactupc.validate_signed_currency(self.FIELD, "1,234.56")
        self.assertTrue(valid)
        self.assertIsNone(err)

    def test_valid_negative_value(self):
        valid, err = coactupc.validate_signed_currency(self.FIELD, "-100.00")
        self.assertTrue(valid)
        self.assertIsNone(err)

    def test_valid_zero(self):
        valid, err = coactupc.validate_signed_currency(self.FIELD, "0.00")
        self.assertTrue(valid)
        self.assertIsNone(err)

    # Boundary conditions
    def test_max_credit_limit(self):
        valid, err = coactupc.validate_signed_currency(
            self.FIELD, "9999999999.99")
        self.assertTrue(valid)
        self.assertIsNone(err)

    def test_min_credit_limit(self):
        valid, err = coactupc.validate_signed_currency(
            self.FIELD, "-9999999999.99")
        self.assertTrue(valid)
        self.assertIsNone(err)

    def test_overflow_credit_limit(self):
        valid, err = coactupc.validate_signed_currency(
            self.FIELD, "10000000000.00")
        self.assertFalse(valid)
        self.assertIsNotNone(err)

    def test_underflow_credit_limit(self):
        valid, err = coactupc.validate_signed_currency(
            self.FIELD, "-10000000000.00")
        self.assertFalse(valid)
        self.assertIsNotNone(err)

    def test_small_positive_credit_limit(self):
        valid, err = coactupc.validate_signed_currency(self.FIELD, "0.01")
        self.assertTrue(valid)
        self.assertIsNone(err)

    def test_small_negative_credit_limit(self):
        valid, err = coactupc.validate_signed_currency(self.FIELD, "-0.01")
        self.assertTrue(valid)
        self.assertIsNone(err)

    def test_three_decimal_places(self):
        # V99 keeps only two fractional digits; extra precision is truncated
        # but the value is still accepted as valid.
        valid, err = coactupc.validate_signed_currency(self.FIELD, "100.123")
        self.assertTrue(valid)
        self.assertIsNone(err)

    # Invalid
    def test_blank_credit_limit(self):
        valid, err = coactupc.validate_signed_currency(self.FIELD, "")
        self.assertFalse(valid)
        self.assertIn("must be supplied", err)

    def test_spaces_credit_limit(self):
        valid, err = coactupc.validate_signed_currency(
            self.FIELD, "               ")
        self.assertFalse(valid)
        self.assertIn("must be supplied", err)

    def test_alpha_credit_limit(self):
        valid, err = coactupc.validate_signed_currency(self.FIELD, "ABC")
        self.assertFalse(valid)
        self.assertIn("is not valid", err)

    def test_special_chars_credit_limit(self):
        valid, err = coactupc.validate_signed_currency(self.FIELD, "12@34")
        self.assertFalse(valid)
        self.assertIn("is not valid", err)

    def test_multiple_decimals(self):
        valid, err = coactupc.validate_signed_currency(self.FIELD, "12.34.56")
        self.assertFalse(valid)
        self.assertIn("is not valid", err)


class TestValidateFicoScore(unittest.TestCase):
    """Tests paragraph 1275-EDIT-FICO-SCORE (lines 2514-2533)."""

    def test_valid_fico_300(self):
        valid, err = coactupc.validate_fico_score("300")
        self.assertTrue(valid)
        self.assertIsNone(err)

    def test_valid_fico_850(self):
        valid, err = coactupc.validate_fico_score("850")
        self.assertTrue(valid)
        self.assertIsNone(err)

    def test_valid_fico_mid(self):
        valid, err = coactupc.validate_fico_score("720")
        self.assertTrue(valid)
        self.assertIsNone(err)

    def test_invalid_fico_below_300(self):
        valid, err = coactupc.validate_fico_score("299")
        self.assertFalse(valid)
        self.assertIsNotNone(err)

    def test_invalid_fico_above_850(self):
        valid, err = coactupc.validate_fico_score("851")
        self.assertFalse(valid)
        self.assertIsNotNone(err)

    def test_invalid_fico_zero(self):
        valid, err = coactupc.validate_fico_score("000")
        self.assertFalse(valid)
        self.assertIsNotNone(err)

    def test_invalid_fico_non_numeric(self):
        valid, err = coactupc.validate_fico_score("ABC")
        self.assertFalse(valid)
        self.assertIsNotNone(err)


class TestValidateActiveStatus(unittest.TestCase):
    """Tests paragraph 1220-EDIT-YESNO (lines 1856-1896)."""

    FIELD = "Active Status"

    def test_valid_yes(self):
        valid, err = coactupc.validate_yes_no(self.FIELD, "Y")
        self.assertTrue(valid)
        self.assertIsNone(err)

    def test_valid_no(self):
        valid, err = coactupc.validate_yes_no(self.FIELD, "N")
        self.assertTrue(valid)
        self.assertIsNone(err)

    def test_invalid_other_char(self):
        valid, err = coactupc.validate_yes_no(self.FIELD, "X")
        self.assertFalse(valid)
        self.assertIn("must be Y or N", err)

    def test_blank(self):
        valid, err = coactupc.validate_yes_no(self.FIELD, "")
        self.assertFalse(valid)
        self.assertIn("must be supplied", err)


class TestLookupAccount(unittest.TestCase):
    """Tests paragraphs 9200-GETCARDXREF-BYACCT, 9300-GETACCTDATA-BYACCT and
    9400-GETCUSTDATA-BYCUST (lines 3608-3799)."""

    def setUp(self):
        self.conn = coactupc.init_db(":memory:")
        seed_standard_data(self.conn)

    def tearDown(self):
        self.conn.close()

    def test_happy_path_lookup(self):
        found, xref, acct, cust, err = coactupc.lookup_account(
            self.conn, SEED_ACCT_ID)
        self.assertTrue(found)
        self.assertIsNone(err)
        self.assertEqual(xref["xref_card_num"], SEED_CARD_NUM)
        self.assertEqual(acct["acct_id"], SEED_ACCT_ID)
        self.assertEqual(cust["cust_first_name"], "John")
        self.assertEqual(cust["cust_last_name"], "Doe")

    def test_account_not_in_xref(self):
        # Replicates DFHRESP(NOTFND) in 9200-GETCARDXREF-BYACCT (3668-3685).
        found, xref, acct, cust, err = coactupc.lookup_account(
            self.conn, 99999999999)
        self.assertFalse(found)
        self.assertIsNone(xref)
        self.assertIn("Cross ref", err)

    def test_account_in_xref_but_not_in_master(self):
        # Replicates 9300-GETACCTDATA-BYACCT NOTFND (3716-3734).
        self.conn.execute(
            "INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id) "
            "VALUES (?, ?, ?)",
            ("4222222222222222", SEED_CUST_ID, 22222222222),
        )
        self.conn.commit()
        found, xref, acct, cust, err = coactupc.lookup_account(
            self.conn, 22222222222)
        self.assertFalse(found)
        self.assertIsNotNone(xref)
        self.assertIsNone(acct)
        self.assertIn("Acct Master", err)

    def test_account_found_but_customer_missing(self):
        # Replicates 9400-GETCUSTDATA-BYCUST NOTFND (3766-3784).
        self.conn.execute(
            "INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id) "
            "VALUES (?, ?, ?)",
            ("4333333333333333", 555555555, 33333333333),
        )
        self.conn.execute(
            "INSERT INTO account (acct_id, acct_active_status, acct_curr_bal, "
            "acct_credit_limit) VALUES (?, ?, ?, ?)",
            (33333333333, "Y", 0.00, 1000.00),
        )
        self.conn.commit()
        found, xref, acct, cust, err = coactupc.lookup_account(
            self.conn, 33333333333)
        self.assertFalse(found)
        self.assertIsNotNone(acct)
        self.assertIsNone(cust)
        self.assertIn("customer master", err)


class TestUpdateAccount(unittest.TestCase):
    """Tests paragraph 9600-WRITE-PROCESSING (lines 3888-4107) and the
    change detection in 9700-CHECK-CHANGE-IN-REC (lines 4109-4195)."""

    def setUp(self):
        self.conn = coactupc.init_db(":memory:")
        seed_standard_data(self.conn)

    def tearDown(self):
        self.conn.close()

    def _snapshot(self):
        found, xref, acct, cust, err = coactupc.lookup_account(
            self.conn, SEED_ACCT_ID)
        self.assertTrue(found)
        return dict(acct), dict(cust)

    def test_happy_path_update(self):
        old_acct, old_cust = self._snapshot()
        new_acct = dict(old_acct)
        new_acct["acct_credit_limit"] = 7500.00
        success, err = coactupc.update_account(
            self.conn, SEED_ACCT_ID, new_acct, dict(old_cust),
            old_acct, old_cust)
        self.assertTrue(success)
        self.assertIsNone(err)
        row = self.conn.execute(
            "SELECT acct_credit_limit FROM account WHERE acct_id = ?",
            (SEED_ACCT_ID,)).fetchone()
        self.assertEqual(Decimal(str(row["acct_credit_limit"])),
                         Decimal("7500.00"))

    def test_concurrent_modification_detected(self):
        old_acct, old_cust = self._snapshot()
        # Simulate another user changing the row after we read it.
        self.conn.execute(
            "UPDATE account SET acct_credit_limit = ? WHERE acct_id = ?",
            (9999.00, SEED_ACCT_ID))
        self.conn.commit()
        new_acct = dict(old_acct)
        new_acct["acct_credit_limit"] = 7500.00
        success, err = coactupc.update_account(
            self.conn, SEED_ACCT_ID, new_acct, dict(old_cust),
            old_acct, old_cust)
        self.assertFalse(success)
        self.assertIn("Record changed by some one else", err)

    def test_update_all_account_fields(self):
        old_acct, old_cust = self._snapshot()
        new_acct = dict(old_acct)
        new_acct.update({
            "acct_active_status": "N",
            "acct_credit_limit": 8000.00,
            "acct_cash_credit_limit": 3000.00,
            "acct_open_date": "2021-02-02",
            "acct_expiration_date": "2031-02-02",
            "acct_reissue_date": "2026-02-02",
            "acct_group_id": "GROUP2",
        })
        success, err = coactupc.update_account(
            self.conn, SEED_ACCT_ID, new_acct, dict(old_cust),
            old_acct, old_cust)
        self.assertTrue(success)
        self.assertIsNone(err)
        row = self.conn.execute(
            "SELECT * FROM account WHERE acct_id = ?",
            (SEED_ACCT_ID,)).fetchone()
        self.assertEqual(row["acct_active_status"], "N")
        self.assertEqual(Decimal(str(row["acct_cash_credit_limit"])),
                         Decimal("3000.00"))
        self.assertEqual(row["acct_open_date"], "2021-02-02")
        self.assertEqual(row["acct_expiration_date"], "2031-02-02")
        self.assertEqual(row["acct_reissue_date"], "2026-02-02")
        self.assertEqual(row["acct_group_id"], "GROUP2")

    def test_update_customer_fields(self):
        old_acct, old_cust = self._snapshot()
        new_cust = dict(old_cust)
        new_cust.update({
            "cust_first_name": "Jane",
            "cust_last_name": "Smith",
            "cust_addr_line_1": "99 New Ave",
            "cust_phone_num_1": "555-9999",
            "cust_ssn": 888002345,
            "cust_fico_credit_score": 800,
        })
        success, err = coactupc.update_account(
            self.conn, SEED_ACCT_ID, dict(old_acct), new_cust,
            old_acct, old_cust)
        self.assertTrue(success)
        self.assertIsNone(err)
        row = self.conn.execute(
            "SELECT * FROM customer WHERE cust_id = ?",
            (SEED_CUST_ID,)).fetchone()
        self.assertEqual(row["cust_first_name"], "Jane")
        self.assertEqual(row["cust_last_name"], "Smith")
        self.assertEqual(row["cust_addr_line_1"], "99 New Ave")
        self.assertEqual(row["cust_phone_num_1"], "555-9999")
        self.assertEqual(row["cust_ssn"], 888002345)
        self.assertEqual(row["cust_fico_credit_score"], 800)


if __name__ == "__main__":
    unittest.main()
