-- V1__init_card_platform.sql
-- Initial schema migrated from the Nationwide legacy VSAM card platform.
-- Each table corresponds to one VSAM master defined in the CardDemo
-- copybooks under ../../../scene-1-understand-the-tail/cobol-source/cpy/.

-- ACCOUNT-RECORD (CVACT01Y.cpy) — keyed by ACCT-ID 9(11).
CREATE TABLE IF NOT EXISTS account (
    acct_id                BIGINT          NOT NULL PRIMARY KEY,
    acct_active_status     CHAR(1)         NOT NULL,
    acct_curr_bal          NUMERIC(12, 2)  NOT NULL,
    acct_credit_limit      NUMERIC(12, 2)  NOT NULL,
    acct_cash_credit_limit NUMERIC(12, 2)  NOT NULL,
    acct_open_date         DATE,
    acct_expiration_date   DATE,
    acct_reissue_date      DATE,
    acct_curr_cyc_credit   NUMERIC(12, 2),
    acct_curr_cyc_debit    NUMERIC(12, 2),
    acct_addr_zip          VARCHAR(10),
    acct_group_id          VARCHAR(10)
);

-- CARD-RECORD (CVACT02Y.cpy) — keyed by CARD-NUM X(16).
CREATE TABLE IF NOT EXISTS card (
    card_num              VARCHAR(16) NOT NULL PRIMARY KEY,
    card_acct_id          BIGINT      NOT NULL REFERENCES account(acct_id),
    card_cvv_cd           INTEGER,
    card_embossed_name    VARCHAR(50),
    card_expiration_date  DATE,
    card_active_status    CHAR(1)     NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_card_acct ON card(card_acct_id);

-- CARD-XREF-RECORD (CVACT03Y.cpy) — keyed by XREF-CARD-NUM X(16).
CREATE TABLE IF NOT EXISTS card_xref (
    xref_card_num  VARCHAR(16) NOT NULL PRIMARY KEY,
    xref_cust_id   BIGINT      NOT NULL,
    xref_acct_id   BIGINT      NOT NULL REFERENCES account(acct_id)
);
CREATE INDEX IF NOT EXISTS idx_xref_cust ON card_xref(xref_cust_id);
CREATE INDEX IF NOT EXISTS idx_xref_acct ON card_xref(xref_acct_id);

-- TRAN-RECORD (CVTRA05Y.cpy) — keyed by TRAN-ID X(16).
CREATE TABLE IF NOT EXISTS transaction (
    tran_id             VARCHAR(16)     NOT NULL PRIMARY KEY,
    tran_type_cd        CHAR(2)         NOT NULL,
    tran_cat_cd         INTEGER         NOT NULL,
    tran_source         VARCHAR(10),
    tran_desc           VARCHAR(100),
    tran_amt            NUMERIC(11, 2)  NOT NULL,
    tran_merchant_id    BIGINT,
    tran_merchant_name  VARCHAR(50),
    tran_merchant_city  VARCHAR(50),
    tran_merchant_zip   VARCHAR(10),
    tran_card_num       VARCHAR(16)     NOT NULL,
    tran_orig_ts        TIMESTAMP WITH TIME ZONE NOT NULL,
    tran_proc_ts        TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_txn_card ON transaction(tran_card_num);
CREATE INDEX IF NOT EXISTS idx_txn_orig_ts ON transaction(tran_orig_ts);

-- TCATBAL — transaction category balance, composite-keyed.
CREATE TABLE IF NOT EXISTS tran_category_balance (
    acct_id       BIGINT          NOT NULL,
    tran_type_cd  CHAR(2)         NOT NULL,
    tran_cat_cd   INTEGER         NOT NULL,
    tran_cat_bal  NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    PRIMARY KEY (acct_id, tran_type_cd, tran_cat_cd),
    CONSTRAINT fk_tcb_acct FOREIGN KEY (acct_id) REFERENCES account(acct_id)
);
