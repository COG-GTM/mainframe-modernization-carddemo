#!/usr/bin/env python3
"""Python translation of the COBOL batch program CBTRN02C.CBL.

Original program:
    app/cbl/CBTRN02C.cbl  (CardDemo)
    Function: Post the records from the daily transaction file.

This module is a faithful, line-for-line-in-spirit translation of the COBOL
batch program. The original program reads a sequential daily-transaction VSAM
file and posts each record against several indexed (KSDS) VSAM files:

    DALYTRAN-FILE  (sequential input)  -> daily_transactions JSON input
    XREF-FILE      (indexed, card num) -> card_xref table
    ACCOUNT-FILE   (indexed, acct id)  -> account table
    TCATBAL-FILE   (indexed, cat key)  -> tran_cat_bal table
    TRANSACT-FILE  (indexed, tran id)  -> transaction_record table
    DALYREJS-FILE  (sequential output) -> daily_reject table

Here the VSAM files are replaced with SQLite tables and the sequential daily
transaction input is replaced with a JSON file (same field layout as the
CVTRA06Y.cpy copybook).

COBOL paragraph -> Python function mapping:
    PROCEDURE DIVISION (lines 193-234) -> main()
    1500-VALIDATE-TRAN / 1500-A-LOOKUP-XREF / 1500-B-LOOKUP-ACCT
                                          -> validate_transaction()
    2000-POST-TRANSACTION / 2700-* / 2800-* / 2900-*
                                          -> post_transaction()
    2500-WRITE-REJECT-REC                 -> write_reject()
    Z-GET-DB2-FORMAT-TIMESTAMP            -> z_get_db2_format_timestamp()

Monetary values use decimal.Decimal to match COBOL fixed-point arithmetic
(PIC S9(09)V99 and PIC S9(10)V99). Only Python standard library modules are
used (Python 3.9+).
"""

import argparse
import json
import sqlite3
import sys
from datetime import datetime
from decimal import Decimal, ROUND_DOWN


# Monetary scale: COBOL PIC ...V99 -> two decimal places.
MONEY_SCALE = Decimal("0.01")


def _money(value) -> Decimal:
    """Coerce a value into a 2-decimal-place Decimal (PIC S9(n)V99 semantics).

    COBOL fixed-point fields truncate (do not round) extra fractional digits,
    so ROUND_DOWN is used to mirror the host arithmetic.
    """
    if isinstance(value, Decimal):
        dec = value
    else:
        # Convert via str() so float inputs do not introduce binary noise.
        dec = Decimal(str(value))
    return dec.quantize(MONEY_SCALE, rounding=ROUND_DOWN)


# ---------------------------------------------------------------------------
# Database schema
# ---------------------------------------------------------------------------
def init_db(db_path):
    """Open the SQLite database and create the tables if they don't exist.

    Replaces the COBOL file OPEN paragraphs (0000-DALYTRAN-OPEN through
    0500-TCATBALF-OPEN) which open the VSAM datasets. Each VSAM KSDS becomes a
    table whose primary key matches the COBOL RECORD KEY.
    """
    conn = sqlite3.connect(db_path)
    conn.execute("PRAGMA foreign_keys = ON")
    cur = conn.cursor()

    # card_xref  <- XREF-FILE / CVACT03Y (RECORD KEY: XREF-CARD-NUM)
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS card_xref (
            xref_card_num   TEXT(16) PRIMARY KEY,
            xref_cust_id    INTEGER,       -- 9(09)
            xref_acct_id    INTEGER        -- 9(11)
        )
        """
    )

    # account  <- ACCOUNT-FILE / CVACT01Y (RECORD KEY: ACCT-ID)
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS account (
            acct_id                INTEGER PRIMARY KEY,  -- 9(11)
            acct_active_status     TEXT(1),
            acct_curr_bal          REAL,       -- S9(10)V99
            acct_credit_limit      REAL,       -- S9(10)V99
            acct_cash_credit_limit REAL,       -- S9(10)V99
            acct_open_date         TEXT(10),
            acct_expiration_date   TEXT(10),
            acct_reissue_date      TEXT(10),
            acct_curr_cyc_credit   REAL,       -- S9(10)V99
            acct_curr_cyc_debit    REAL,       -- S9(10)V99
            acct_addr_zip          TEXT(10),
            acct_group_id          TEXT(10)
        )
        """
    )

    # transaction_record  <- TRANSACT-FILE / CVTRA05Y (RECORD KEY: TRAN-ID)
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS transaction_record (
            tran_id            TEXT(16) PRIMARY KEY,
            tran_type_cd       TEXT(2),
            tran_cat_cd        INTEGER,      -- 9(04)
            tran_source        TEXT(10),
            tran_desc          TEXT(100),
            tran_amt           REAL,         -- S9(09)V99
            tran_merchant_id   INTEGER,      -- 9(09)
            tran_merchant_name TEXT(50),
            tran_merchant_city TEXT(50),
            tran_merchant_zip  TEXT(10),
            tran_card_num      TEXT(16),
            tran_orig_ts       TEXT(26),
            tran_proc_ts       TEXT(26)
        )
        """
    )

    # tran_cat_bal  <- TCATBAL-FILE / CVTRA01Y (RECORD KEY: TRAN-CAT-KEY)
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS tran_cat_bal (
            trancat_acct_id  INTEGER,       -- 9(11)
            trancat_type_cd  TEXT(2),
            trancat_cd       INTEGER,       -- 9(04)
            tran_cat_bal     REAL,          -- S9(09)V99
            PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
        )
        """
    )

    # daily_reject  <- DALYREJS-FILE (sequential reject output)
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS daily_reject (
            id                  INTEGER PRIMARY KEY AUTOINCREMENT,
            reject_tran_id      TEXT(16),
            reject_card_num     TEXT(16),
            reject_amt          REAL,
            reject_orig_ts      TEXT(26),
            fail_reason_code    INTEGER,     -- 4-digit code (100, 101, 102, 103)
            fail_reason_desc    TEXT(76),
            raw_tran_data       TEXT         -- JSON of full daily tran record
        )
        """
    )

    conn.commit()
    return conn


# ---------------------------------------------------------------------------
# Z-GET-DB2-FORMAT-TIMESTAMP (line 692)
# ---------------------------------------------------------------------------
def z_get_db2_format_timestamp(now=None):
    """Replicates paragraph Z-GET-DB2-FORMAT-TIMESTAMP (line 692).

    Builds a DB2-format timestamp string of the form:
        YYYY-MM-DD-HH.MM.SS.HH0000
    where HH after the final dot is hundredths of a second (COBOL CURRENT-DATE
    supplies hundredths in COB-MIL), followed by the literal '0000' moved into
    DB2-REST. The result is exactly 26 characters wide (PIC X(26)).
    """
    if now is None:
        now = datetime.now()
    hundredths = now.microsecond // 10000  # COB-MIL: hundredths of a second
    return "{:04d}-{:02d}-{:02d}-{:02d}.{:02d}.{:02d}.{:02d}0000".format(
        now.year, now.month, now.day,
        now.hour, now.minute, now.second, hundredths,
    )


# ---------------------------------------------------------------------------
# 1500-VALIDATE-TRAN (lines 370-422)
# ---------------------------------------------------------------------------
def validate_transaction(conn, tran):
    """Replicates paragraphs 1500-VALIDATE-TRAN, 1500-A-LOOKUP-XREF and
    1500-B-LOOKUP-ACCT (lines 370-422).

    Returns a tuple:
        (is_valid, fail_code, fail_desc, xref_row, acct_row)

    Validation order mirrors the COBOL exactly:
        100 INVALID CARD NUMBER FOUND               (xref lookup fails)
        101 ACCOUNT RECORD NOT FOUND                (account lookup fails)
        102 OVERLIMIT TRANSACTION                    (temp bal > credit limit)
        103 TRANSACTION RECEIVED AFTER ACCT EXPIRATION (expiration < orig ts)

    NOTE (matching COBOL): checks 102 and 103 are NOT mutually exclusive. Both
    are evaluated, and the last failing one overwrites the fail reason. So a
    transaction that is both overlimit and post-expiration is rejected with
    code 103.
    """
    fail_code = 0
    fail_desc = ""

    # ---- 1500-A-LOOKUP-XREF (lines 380-392) ----
    cur = conn.cursor()
    cur.execute(
        "SELECT xref_card_num, xref_cust_id, xref_acct_id "
        "FROM card_xref WHERE xref_card_num = ?",
        (tran["dalytran_card_num"],),
    )
    xref_row = cur.fetchone()
    if xref_row is None:
        # INVALID KEY
        fail_code = 100
        fail_desc = "INVALID CARD NUMBER FOUND"
        return (False, fail_code, fail_desc, None, None)

    # ---- 1500-B-LOOKUP-ACCT (lines 393-422) ----
    xref_acct_id = xref_row[2]
    cur.execute(
        "SELECT acct_id, acct_active_status, acct_curr_bal, acct_credit_limit, "
        "acct_cash_credit_limit, acct_open_date, acct_expiration_date, "
        "acct_reissue_date, acct_curr_cyc_credit, acct_curr_cyc_debit, "
        "acct_addr_zip, acct_group_id "
        "FROM account WHERE acct_id = ?",
        (xref_acct_id,),
    )
    acct_row = cur.fetchone()
    if acct_row is None:
        # INVALID KEY
        fail_code = 101
        fail_desc = "ACCOUNT RECORD NOT FOUND"
        return (False, fail_code, fail_desc, xref_row, None)

    # COMPUTE WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT
    #                     - ACCT-CURR-CYC-DEBIT
    #                     + DALYTRAN-AMT
    acct_curr_cyc_credit = _money(acct_row[8])
    acct_curr_cyc_debit = _money(acct_row[9])
    acct_credit_limit = _money(acct_row[3])
    tran_amt = _money(tran["dalytran_amt"])
    temp_bal = acct_curr_cyc_credit - acct_curr_cyc_debit + tran_amt

    # IF ACCT-CREDIT-LIMIT >= WS-TEMP-BAL CONTINUE ELSE reject 102
    if not (acct_credit_limit >= temp_bal):
        fail_code = 102
        fail_desc = "OVERLIMIT TRANSACTION"

    # IF ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS (1:10) CONTINUE ELSE reject 103
    # (checked independently; overwrites a prior 102)
    acct_expiration_date = acct_row[6]
    orig_ts_date = (tran["dalytran_orig_ts"] or "")[:10]
    if not (acct_expiration_date >= orig_ts_date):
        fail_code = 103
        fail_desc = "TRANSACTION RECEIVED AFTER ACCT EXPIRATION"

    is_valid = fail_code == 0
    return (is_valid, fail_code, fail_desc, xref_row, acct_row)


# ---------------------------------------------------------------------------
# 2000-POST-TRANSACTION (lines 424-444)
# ---------------------------------------------------------------------------
def post_transaction(conn, tran, xref_row, acct_row):
    """Replicates paragraphs 2000-POST-TRANSACTION, 2700-UPDATE-TCATBAL,
    2800-UPDATE-ACCOUNT-REC and 2900-WRITE-TRANSACTION-FILE (lines 424-579).

    All three sub-steps run inside a single SQLite transaction. The COBOL
    program ABENDs if any file operation fails, rolling back nothing committed
    yet within the record; mirroring that, the caller commits once per daily
    transaction and any exception here aborts before commit.
    """
    cur = conn.cursor()
    xref_acct_id = xref_row[2]
    tran_amt = _money(tran["dalytran_amt"])

    # ---- 2700-UPDATE-TCATBAL (lines 467-542) ----
    # Build key (TRANCAT-ACCT-ID, TRANCAT-TYPE-CD, TRANCAT-CD) and read.
    cur.execute(
        "SELECT tran_cat_bal FROM tran_cat_bal "
        "WHERE trancat_acct_id = ? AND trancat_type_cd = ? AND trancat_cd = ?",
        (xref_acct_id, tran["dalytran_type_cd"], tran["dalytran_cat_cd"]),
    )
    tcatbal_row = cur.fetchone()
    if tcatbal_row is None:
        # 2700-A-CREATE-TCATBAL-REC (INVALID KEY -> create new record).
        new_bal = _money(Decimal("0.00") + tran_amt)
        cur.execute(
            "INSERT INTO tran_cat_bal "
            "(trancat_acct_id, trancat_type_cd, trancat_cd, tran_cat_bal) "
            "VALUES (?, ?, ?, ?)",
            (
                xref_acct_id,
                tran["dalytran_type_cd"],
                tran["dalytran_cat_cd"],
                float(new_bal),
            ),
        )
    else:
        # 2700-B-UPDATE-TCATBAL-REC (ADD DALYTRAN-AMT TO TRAN-CAT-BAL).
        updated_bal = _money(_money(tcatbal_row[0]) + tran_amt)
        cur.execute(
            "UPDATE tran_cat_bal SET tran_cat_bal = ? "
            "WHERE trancat_acct_id = ? AND trancat_type_cd = ? "
            "AND trancat_cd = ?",
            (
                float(updated_bal),
                xref_acct_id,
                tran["dalytran_type_cd"],
                tran["dalytran_cat_cd"],
            ),
        )

    # ---- 2800-UPDATE-ACCOUNT-REC (lines 545-560) ----
    acct_curr_bal = _money(acct_row[2])
    acct_curr_cyc_credit = _money(acct_row[8])
    acct_curr_cyc_debit = _money(acct_row[9])

    acct_curr_bal = _money(acct_curr_bal + tran_amt)
    if tran_amt >= 0:
        acct_curr_cyc_credit = _money(acct_curr_cyc_credit + tran_amt)
    else:
        acct_curr_cyc_debit = _money(acct_curr_cyc_debit + tran_amt)

    cur.execute(
        "UPDATE account SET acct_curr_bal = ?, acct_curr_cyc_credit = ?, "
        "acct_curr_cyc_debit = ? WHERE acct_id = ?",
        (
            float(acct_curr_bal),
            float(acct_curr_cyc_credit),
            float(acct_curr_cyc_debit),
            acct_row[0],
        ),
    )

    # ---- 2900-WRITE-TRANSACTION-FILE (lines 562-579) ----
    # MOVE DALYTRAN-* TO TRAN-*; PROC-TS = DB2 format current timestamp.
    tran_proc_ts = z_get_db2_format_timestamp()
    cur.execute(
        "INSERT INTO transaction_record "
        "(tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc, "
        "tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city, "
        "tran_merchant_zip, tran_card_num, tran_orig_ts, tran_proc_ts) "
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        (
            tran["dalytran_id"],
            tran["dalytran_type_cd"],
            tran["dalytran_cat_cd"],
            tran["dalytran_source"],
            tran["dalytran_desc"],
            float(tran_amt),
            tran["dalytran_merchant_id"],
            tran["dalytran_merchant_name"],
            tran["dalytran_merchant_city"],
            tran["dalytran_merchant_zip"],
            tran["dalytran_card_num"],
            tran["dalytran_orig_ts"],
            tran_proc_ts,
        ),
    )


# ---------------------------------------------------------------------------
# 2500-WRITE-REJECT-REC (lines 446-465)
# ---------------------------------------------------------------------------
def write_reject(conn, tran, fail_code, fail_desc):
    """Replicates paragraph 2500-WRITE-REJECT-REC (lines 446-465).

    Writes a rejected daily transaction together with its validation failure
    reason. The COBOL writes the full DALYTRAN-RECORD (REJECT-TRAN-DATA) plus a
    WS-VALIDATION-TRAILER (fail reason code + description); here the full daily
    record is preserved as JSON in raw_tran_data for auditability.
    """
    cur = conn.cursor()
    cur.execute(
        "INSERT INTO daily_reject "
        "(reject_tran_id, reject_card_num, reject_amt, reject_orig_ts, "
        "fail_reason_code, fail_reason_desc, raw_tran_data) "
        "VALUES (?, ?, ?, ?, ?, ?, ?)",
        (
            tran["dalytran_id"],
            tran["dalytran_card_num"],
            float(_money(tran["dalytran_amt"])),
            tran["dalytran_orig_ts"],
            fail_code,
            fail_desc,
            json.dumps(tran),
        ),
    )


# ---------------------------------------------------------------------------
# PROCEDURE DIVISION (lines 193-234)
# ---------------------------------------------------------------------------
def main(db_path, daily_tran_file):
    """Replicates the main PROCEDURE DIVISION (lines 193-234).

    Opens the database, loads the daily transactions, loops over each record
    validating then posting (or rejecting) it, prints the processed/rejected
    counts, and returns RETURN-CODE 4 when any transaction was rejected
    (MOVE 4 TO RETURN-CODE).
    """
    print("START OF EXECUTION OF PROGRAM CBTRN02C")

    # PERFORM 0000-DALYTRAN-OPEN .. 0500-TCATBALF-OPEN
    conn = init_db(db_path)

    # Load the daily transactions (replaces the sequential DALYTRAN-FILE).
    with open(daily_tran_file, "r", encoding="utf-8") as fh:
        daily_transactions = json.load(fh)

    transaction_count = 0
    reject_count = 0

    # PERFORM UNTIL END-OF-FILE = 'Y'
    for tran in daily_transactions:
        # ADD 1 TO WS-TRANSACTION-COUNT
        transaction_count += 1

        # PERFORM 1500-VALIDATE-TRAN
        is_valid, fail_code, fail_desc, xref_row, acct_row = validate_transaction(
            conn, tran
        )

        if is_valid:
            # PERFORM 2000-POST-TRANSACTION (single SQLite txn per record)
            try:
                post_transaction(conn, tran, xref_row, acct_row)
                conn.commit()
            except Exception:
                # Mirrors the COBOL ABEND on any file failure during posting.
                conn.rollback()
                conn.close()
                raise
        else:
            # ADD 1 TO WS-REJECT-COUNT; PERFORM 2500-WRITE-REJECT-REC
            reject_count += 1
            write_reject(conn, tran, fail_code, fail_desc)
            conn.commit()

    # PERFORM 9000-DALYTRAN-CLOSE .. 9500-TCATBALF-CLOSE
    conn.close()

    print("TRANSACTIONS PROCESSED: {}".format(transaction_count))
    print("TRANSACTIONS REJECTED: {}".format(reject_count))
    print("END OF EXECUTION OF PROGRAM CBTRN02C")

    # IF WS-REJECT-COUNT > 0  MOVE 4 TO RETURN-CODE
    if reject_count > 0:
        sys.exit(4)


# ---------------------------------------------------------------------------
# Test helpers
# ---------------------------------------------------------------------------
def create_sample_data(conn):
    """Insert a few test rows into card_xref, account and tran_cat_bal so the
    program can be exercised standalone (no COBOL equivalent).
    """
    cur = conn.cursor()

    # Cross-reference rows linking card numbers to accounts.
    cur.executemany(
        "INSERT OR REPLACE INTO card_xref "
        "(xref_card_num, xref_cust_id, xref_acct_id) VALUES (?, ?, ?)",
        [
            ("4111111111111111", 100000001, 10000000001),
            ("4222222222222222", 100000002, 10000000002),
        ],
    )

    # Account rows.
    cur.executemany(
        "INSERT OR REPLACE INTO account "
        "(acct_id, acct_active_status, acct_curr_bal, acct_credit_limit, "
        "acct_cash_credit_limit, acct_open_date, acct_expiration_date, "
        "acct_reissue_date, acct_curr_cyc_credit, acct_curr_cyc_debit, "
        "acct_addr_zip, acct_group_id) "
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        [
            (
                10000000001, "Y", 100.00, 5000.00, 1000.00,
                "2020-01-01", "2030-12-31", "2025-01-01",
                500.00, 100.00, "12345", "GROUP01",
            ),
            (
                10000000002, "Y", 0.00, 200.00, 50.00,
                "2020-01-01", "2021-01-01", "2025-01-01",
                150.00, 0.00, "67890", "GROUP02",
            ),
        ],
    )

    # An existing transaction category balance (will be updated, not created).
    cur.executemany(
        "INSERT OR REPLACE INTO tran_cat_bal "
        "(trancat_acct_id, trancat_type_cd, trancat_cd, tran_cat_bal) "
        "VALUES (?, ?, ?, ?)",
        [
            (10000000001, "01", 5, 25.00),
        ],
    )

    conn.commit()


def create_sample_input(filepath):
    """Write a sample JSON daily-transactions file (no COBOL equivalent).

    The records mirror the CVTRA06Y.cpy layout and exercise the happy path as
    well as the 100/101/102/103 reject paths.
    """
    sample = [
        {
            # Valid: posts against an existing tran_cat_bal (update path).
            "dalytran_id": "0000000000000001",
            "dalytran_type_cd": "01",
            "dalytran_cat_cd": 5,
            "dalytran_source": "POS",
            "dalytran_desc": "GROCERY STORE PURCHASE",
            "dalytran_amt": "50.00",
            "dalytran_merchant_id": 123456789,
            "dalytran_merchant_name": "GROCERY STORE",
            "dalytran_merchant_city": "SEATTLE",
            "dalytran_merchant_zip": "98101",
            "dalytran_card_num": "4111111111111111",
            "dalytran_orig_ts": "2025-06-01-10.00.00.000000",
        },
        {
            # Valid: creates a brand-new tran_cat_bal (create path).
            "dalytran_id": "0000000000000002",
            "dalytran_type_cd": "02",
            "dalytran_cat_cd": 9,
            "dalytran_source": "POS",
            "dalytran_desc": "RESTAURANT PURCHASE",
            "dalytran_amt": "30.00",
            "dalytran_merchant_id": 223456789,
            "dalytran_merchant_name": "RESTAURANT",
            "dalytran_merchant_city": "PORTLAND",
            "dalytran_merchant_zip": "97201",
            "dalytran_card_num": "4111111111111111",
            "dalytran_orig_ts": "2025-06-01-12.00.00.000000",
        },
        {
            # Reject 100: card number not in card_xref.
            "dalytran_id": "0000000000000003",
            "dalytran_type_cd": "01",
            "dalytran_cat_cd": 5,
            "dalytran_source": "POS",
            "dalytran_desc": "UNKNOWN CARD PURCHASE",
            "dalytran_amt": "10.00",
            "dalytran_merchant_id": 323456789,
            "dalytran_merchant_name": "MERCHANT",
            "dalytran_merchant_city": "DENVER",
            "dalytran_merchant_zip": "80201",
            "dalytran_card_num": "9999999999999999",
            "dalytran_orig_ts": "2025-06-01-13.00.00.000000",
        },
        {
            # Reject 102: amount pushes the cycle balance over the credit limit.
            "dalytran_id": "0000000000000004",
            "dalytran_type_cd": "01",
            "dalytran_cat_cd": 5,
            "dalytran_source": "POS",
            "dalytran_desc": "BIG TICKET PURCHASE",
            "dalytran_amt": "500.00",
            "dalytran_merchant_id": 423456789,
            "dalytran_merchant_name": "ELECTRONICS",
            "dalytran_merchant_city": "AUSTIN",
            "dalytran_merchant_zip": "73301",
            "dalytran_card_num": "4222222222222222",
            "dalytran_orig_ts": "2020-06-01-14.00.00.000000",
        },
    ]
    with open(filepath, "w", encoding="utf-8") as fh:
        json.dump(sample, fh, indent=2)


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Python translation of COBOL batch program CBTRN02C "
        "(post daily transactions)."
    )
    parser.add_argument(
        "--db", default="carddemo.db", help="Path to the SQLite database file."
    )
    parser.add_argument(
        "--input",
        default="daily_transactions.json",
        help="Path to the daily transactions JSON input file.",
    )
    parser.add_argument(
        "--init-sample",
        action="store_true",
        help="Populate sample reference data and write a sample input file "
        "before running.",
    )
    args = parser.parse_args()

    if args.init_sample:
        conn = init_db(args.db)
        create_sample_data(conn)
        conn.close()
        create_sample_input(args.input)
        print(
            "Initialized sample data in '{}' and sample input '{}'.".format(
                args.db, args.input
            )
        )

    main(args.db, args.input)
