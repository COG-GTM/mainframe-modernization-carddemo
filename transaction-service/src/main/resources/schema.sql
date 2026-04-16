-- PostgreSQL schema for CardDemo Transaction Service
-- Matches the 350-byte TRAN-RECORD layout from CVTRA05Y.cpy

CREATE TABLE IF NOT EXISTS transactions (
    tran_id             VARCHAR(16)     NOT NULL PRIMARY KEY,
    tran_type_cd        VARCHAR(2)      NOT NULL,
    tran_cat_cd         INTEGER         NOT NULL,
    tran_source         VARCHAR(10)     NOT NULL,
    tran_desc           VARCHAR(100)    NOT NULL,
    tran_amt            NUMERIC(11,2)   NOT NULL,
    tran_merchant_id    NUMERIC(9,0)    NOT NULL,
    tran_merchant_name  VARCHAR(50)     NOT NULL,
    tran_merchant_city  VARCHAR(50)     NOT NULL,
    tran_merchant_zip   VARCHAR(10)     NOT NULL,
    tran_card_num       VARCHAR(16)     NOT NULL,
    tran_orig_ts        VARCHAR(26),
    tran_proc_ts        VARCHAR(26),
    created_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transactions_card_num ON transactions(tran_card_num);

-- Account table for bill payment balance updates
-- Matches the 300-byte ACCOUNT-RECORD layout from CVACT01Y.cpy
CREATE TABLE IF NOT EXISTS accounts (
    acct_id                 NUMERIC(11,0)   NOT NULL PRIMARY KEY,
    acct_active_status      VARCHAR(1),
    acct_curr_bal           NUMERIC(12,2)   NOT NULL DEFAULT 0,
    acct_credit_limit       NUMERIC(12,2)   DEFAULT 0,
    acct_cash_credit_limit  NUMERIC(12,2)   DEFAULT 0,
    acct_open_date          VARCHAR(10),
    acct_expiration_date    VARCHAR(10),
    acct_reissue_date       VARCHAR(10),
    acct_curr_cyc_credit    NUMERIC(12,2)   DEFAULT 0,
    acct_curr_cyc_debit     NUMERIC(12,2)   DEFAULT 0,
    acct_addr_zip           VARCHAR(10),
    acct_group_id           VARCHAR(10)
);

-- Card cross-reference table for account/card lookups
-- Matches the 50-byte XREF-RECORD layout from CVACT03Y.cpy
CREATE TABLE IF NOT EXISTS card_xref (
    xref_card_num   VARCHAR(16)     NOT NULL PRIMARY KEY,
    xref_cust_id    NUMERIC(9,0),
    xref_acct_id    NUMERIC(11,0)   NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_card_xref_acct_id ON card_xref(xref_acct_id);
