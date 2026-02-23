-- ============================================================================
-- CardDemo Transaction Service - Database Schema
-- Migrated from VSAM files used by COBOL/CICS programs COTRN00C-COTRN02C
-- ============================================================================

-- Transaction table (from TRANSACT VSAM KSDS file)
-- COBOL copybook: CVTRA05Y (TRAN-RECORD, 350 bytes)
CREATE TABLE transactions (
    tran_id             VARCHAR(16) PRIMARY KEY,
    tran_type_cd        VARCHAR(2) NOT NULL,
    tran_cat_cd         INTEGER NOT NULL,
    tran_source         VARCHAR(10) NOT NULL,
    tran_desc           VARCHAR(100) NOT NULL,
    tran_amt            DECIMAL(11,2) NOT NULL,
    tran_merchant_id    INTEGER NOT NULL,
    tran_merchant_name  VARCHAR(50) NOT NULL,
    tran_merchant_city  VARCHAR(50) NOT NULL,
    tran_merchant_zip   VARCHAR(10) NOT NULL,
    tran_card_num       VARCHAR(16) NOT NULL,
    tran_orig_ts        VARCHAR(26),
    tran_proc_ts        VARCHAR(26)
);

-- Card cross-reference table (from CCXREF VSAM KSDS file)
-- COBOL copybook: CVACT03Y (CARD-XREF-RECORD, 50 bytes)
-- Primary key: XREF-CARD-NUM (same as CCXREF VSAM primary key)
CREATE TABLE card_xref (
    xref_card_num       VARCHAR(16) PRIMARY KEY,
    xref_cust_id        BIGINT,
    xref_acct_id        BIGINT
);

-- Index on account ID (replaces CXACAIX VSAM alternate index)
CREATE INDEX idx_card_xref_acct ON card_xref(xref_acct_id);

-- Account table (from ACCTDAT VSAM KSDS file)
-- COBOL copybook: CVACT01Y (ACCOUNT-RECORD, 300 bytes)
CREATE TABLE accounts (
    acct_id                 BIGINT PRIMARY KEY,
    acct_active_status      VARCHAR(1),
    acct_curr_bal           DECIMAL(12,2),
    acct_credit_limit       DECIMAL(12,2),
    acct_cash_credit_limit  DECIMAL(12,2),
    acct_open_date          VARCHAR(10),
    acct_expiration_date    VARCHAR(10),
    acct_reissue_date       VARCHAR(10),
    acct_curr_cyc_credit    DECIMAL(12,2),
    acct_curr_cyc_debit     DECIMAL(12,2),
    acct_addr_zip           VARCHAR(10),
    acct_group_id           VARCHAR(10)
);

-- Index for card number lookups on transactions
CREATE INDEX idx_transactions_card_num ON transactions(tran_card_num);
