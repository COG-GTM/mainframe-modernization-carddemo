"""Python translation of business logic from app/cbl/COACTUPC.cbl.

COACTUPC is the CICS online program (transaction CAUP) that updates an
account and its associated customer record in CardDemo.  This module
extracts the program's validation and data-access business logic into
plain Python backed by SQLite, mirroring the VSAM file layouts as tables.

Each public function maps to a COBOL paragraph; the originating paragraph
name (and line range) is noted in the function docstring for traceability.

Monetary amounts use decimal.Decimal to preserve the fixed-point semantics
of COBOL PIC S9(10)V99 fields (never floating point for money).
"""

import sqlite3
from decimal import Decimal, InvalidOperation, ROUND_DOWN

# PIC S9(10)V99 bounds: 10 integer digits, 2 fractional digits.
SIGNED_9V2_MAX = Decimal("9999999999.99")
SIGNED_9V2_MIN = Decimal("-9999999999.99")

# Column whitelists matching the VSAM-derived table layouts.
ACCOUNT_COLUMNS = (
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
)

CUSTOMER_COLUMNS = (
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
)


def init_db(db_path):
    """Create SQLite tables matching the VSAM file layouts."""
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    conn.execute("""CREATE TABLE IF NOT EXISTS card_xref (
        xref_card_num   TEXT(16),
        xref_cust_id    INTEGER,
        xref_acct_id    INTEGER PRIMARY KEY
    )""")
    conn.execute("""CREATE TABLE IF NOT EXISTS account (
        acct_id                INTEGER PRIMARY KEY,
        acct_active_status     TEXT(1),
        acct_curr_bal          REAL,
        acct_credit_limit      REAL,
        acct_cash_credit_limit REAL,
        acct_open_date         TEXT(10),
        acct_expiration_date   TEXT(10),
        acct_reissue_date      TEXT(10),
        acct_curr_cyc_credit   REAL,
        acct_curr_cyc_debit    REAL,
        acct_addr_zip          TEXT(10),
        acct_group_id          TEXT(10)
    )""")
    conn.execute("""CREATE TABLE IF NOT EXISTS customer (
        cust_id                 INTEGER PRIMARY KEY,
        cust_first_name         TEXT(25),
        cust_middle_name        TEXT(25),
        cust_last_name          TEXT(25),
        cust_addr_line_1        TEXT(50),
        cust_addr_line_2        TEXT(50),
        cust_addr_line_3        TEXT(50),
        cust_addr_state_cd      TEXT(2),
        cust_addr_country_cd    TEXT(3),
        cust_addr_zip           TEXT(10),
        cust_phone_num_1        TEXT(15),
        cust_phone_num_2        TEXT(15),
        cust_ssn                INTEGER,
        cust_govt_issued_id     TEXT(20),
        cust_dob_yyyy_mm_dd     TEXT(10),
        cust_eft_account_id     TEXT(10),
        cust_pri_card_holder_ind TEXT(1),
        cust_fico_credit_score  INTEGER
    )""")
    conn.commit()
    return conn


# ---------------------------------------------------------------------------
# Field validation
# ---------------------------------------------------------------------------

def validate_account_id(acct_id_str):
    """Validate an account number. Replicates paragraph 1210-EDIT-ACCOUNT
    (app/cbl/COACTUPC.cbl lines 1783-1822).

    Returns (is_valid, error_message).
    """
    # Not supplied (LOW-VALUES / SPACES in COBOL).
    if acct_id_str is None:
        return False, "Account number not provided"
    s = str(acct_id_str).strip()
    if s == "":
        return False, "Account number not provided"

    # The COBOL field CC-ACCT-ID is 11 characters; shorter numeric input is
    # right-justified and zero-padded when moved into the numeric field.
    padded = s.zfill(11)

    # Not numeric, or numeric value is zero.
    if not padded.isdigit() or int(padded) == 0:
        return (
            False,
            "Account Number if supplied must be a 11 digit Non-Zero Number",
        )
    return True, None


def validate_signed_currency(field_name, value_str):
    """Validate a signed currency amount (PIC S9(10)V99). Replicates
    paragraph 1250-EDIT-SIGNED-9V2 (app/cbl/COACTUPC.cbl lines 2180-2223).

    Accepts NUMVAL-C style input such as "1,234.56", "-1,234.56" and
    "+1,234.56". Enforces the PIC S9(10)V99 range. Returns
    (is_valid, error_message).
    """
    # Not supplied.
    if value_str is None:
        return False, "{} must be supplied".format(field_name)
    raw = str(value_str).strip()
    if raw == "":
        return False, "{} must be supplied".format(field_name)

    # NUMVAL-C tolerates currency formatting: thousands separators and a
    # leading sign. Decimal natively parses a leading + or -.
    cleaned = raw.replace(",", "")

    try:
        amount = Decimal(cleaned)
    except (InvalidOperation, ValueError):
        return False, "{} is not valid".format(field_name)

    if amount != amount:  # guard against NaN
        return False, "{} is not valid".format(field_name)

    # PIC V99 keeps only two fractional digits (extra precision truncated).
    amount = amount.quantize(Decimal("0.01"), rounding=ROUND_DOWN)

    if amount > SIGNED_9V2_MAX or amount < SIGNED_9V2_MIN:
        return False, "{} is not valid".format(field_name)

    return True, None


def validate_fico_score(score_str):
    """Validate a FICO score. Replicates paragraph 1275-EDIT-FICO-SCORE
    (app/cbl/COACTUPC.cbl lines 2514-2533): numeric, 3 digits, 300-850.

    Returns (is_valid, error_message).
    """
    if score_str is None:
        return False, "FICO score must be supplied"
    s = str(score_str).strip()
    if not s.isdigit() or len(s) != 3:
        return False, "FICO Score: should be between 300 and 850"
    value = int(s)
    if value < 300 or value > 850:
        return False, "FICO Score: should be between 300 and 850"
    return True, None


def validate_yes_no(field_name, value):
    """Validate a Y/N flag. Replicates paragraph 1220-EDIT-YESNO
    (app/cbl/COACTUPC.cbl lines 1856-1896).

    Returns (is_valid, error_message).
    """
    if value is None:
        return False, "{} must be supplied".format(field_name)
    s = str(value).strip()
    if s == "":
        return False, "{} must be supplied".format(field_name)
    if s.upper() in ("Y", "N"):
        return True, None
    return False, "{} must be Y or N".format(field_name)


# ---------------------------------------------------------------------------
# Data access
# ---------------------------------------------------------------------------

def lookup_account(conn, acct_id):
    """Read account details through the cross-reference, account master and
    customer master files. Replicates paragraphs 9200-GETCARDXREF-BYACCT,
    9300-GETACCTDATA-BYACCT and 9400-GETCUSTDATA-BYCUST
    (app/cbl/COACTUPC.cbl lines 3608-3799).

    Returns (found, xref_row, acct_row, cust_row, error_message).
    """
    # 9200-GETCARDXREF-BYACCT: read CXACAIX (xref by account id).
    xref = conn.execute(
        "SELECT * FROM card_xref WHERE xref_acct_id = ?", (acct_id,)
    ).fetchone()
    if xref is None:
        return (
            False,
            None,
            None,
            None,
            "Account:{} not found in Cross ref file.".format(acct_id),
        )

    # 9300-GETACCTDATA-BYACCT: read ACCTDAT by account id.
    acct = conn.execute(
        "SELECT * FROM account WHERE acct_id = ?", (acct_id,)
    ).fetchone()
    if acct is None:
        return (
            False,
            xref,
            None,
            None,
            "Account:{} not found in Acct Master file.".format(acct_id),
        )

    # 9400-GETCUSTDATA-BYCUST: read CUSTDAT by customer id from the xref.
    cust_id = xref["xref_cust_id"]
    cust = conn.execute(
        "SELECT * FROM customer WHERE cust_id = ?", (cust_id,)
    ).fetchone()
    if cust is None:
        return (
            False,
            xref,
            acct,
            None,
            "CustId:{} not found in customer master.".format(cust_id),
        )

    return True, xref, acct, cust, None


def _values_equal(a, b):
    """Compare two stored values, tolerating int/float/Decimal/str mixes."""
    if a is None or b is None:
        return a == b
    try:
        return Decimal(str(a)) == Decimal(str(b))
    except (InvalidOperation, ValueError):
        return str(a) == str(b)


def update_account(conn, acct_id, new_acct_data, new_cust_data,
                   old_acct_data, old_cust_data):
    """Update the account and customer records with optimistic concurrency.
    Replicates paragraph 9600-WRITE-PROCESSING (app/cbl/COACTUPC.cbl lines
    3888-4107) together with the change detection in 9700-CHECK-CHANGE-IN-REC
    (lines 4109-4195).

    Re-reads the current rows (the CICS READ ... UPDATE lock), compares them
    against the caller's old-data snapshot and, if nothing changed in the
    meantime, applies the update to both tables. Returns
    (success, error_message).
    """
    # READ ... UPDATE for both files. A missing row means we could not lock.
    current_acct = conn.execute(
        "SELECT * FROM account WHERE acct_id = ?", (acct_id,)
    ).fetchone()
    if current_acct is None:
        return False, "Could not lock Account record for update"

    cust_id = old_cust_data.get("cust_id")
    if cust_id is None:
        xref = conn.execute(
            "SELECT * FROM card_xref WHERE xref_acct_id = ?", (acct_id,)
        ).fetchone()
        cust_id = xref["xref_cust_id"] if xref is not None else None

    current_cust = conn.execute(
        "SELECT * FROM customer WHERE cust_id = ?", (cust_id,)
    ).fetchone()
    if current_cust is None:
        return False, "Could not lock Customer record for update"

    # 9700-CHECK-CHANGE-IN-REC: did someone change the record while we were
    # out? Compare current values to the snapshot the caller fetched earlier.
    for key, old_value in old_acct_data.items():
        if key not in current_acct.keys():
            continue
        if not _values_equal(current_acct[key], old_value):
            return False, "Record changed by some one else. Please review"
    for key, old_value in old_cust_data.items():
        if key not in current_cust.keys():
            continue
        if not _values_equal(current_cust[key], old_value):
            return False, "Record changed by some one else. Please review"

    # Apply the update to both tables inside a single transaction.
    try:
        acct_updates = {
            k: v for k, v in new_acct_data.items()
            if k in ACCOUNT_COLUMNS and k != "acct_id"
        }
        if acct_updates:
            assignments = ", ".join("{} = ?".format(c) for c in acct_updates)
            params = list(acct_updates.values()) + [acct_id]
            conn.execute(
                "UPDATE account SET {} WHERE acct_id = ?".format(assignments),
                params,
            )

        cust_updates = {
            k: v for k, v in new_cust_data.items()
            if k in CUSTOMER_COLUMNS and k != "cust_id"
        }
        if cust_updates:
            assignments = ", ".join("{} = ?".format(c) for c in cust_updates)
            params = list(cust_updates.values()) + [cust_id]
            conn.execute(
                "UPDATE customer SET {} WHERE cust_id = ?".format(assignments),
                params,
            )

        conn.commit()
    except sqlite3.Error as exc:
        conn.rollback()
        return False, "Update failed: {}".format(exc)

    return True, None
