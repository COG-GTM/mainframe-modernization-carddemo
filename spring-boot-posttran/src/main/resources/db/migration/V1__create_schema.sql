-- ============================================================================
-- CardDemo Post Transaction Schema
-- Migrated from COBOL VSAM datasets used by CBTRN02C / POSTTRAN JCL
-- ============================================================================

-- ----------------------------------------------------------------------------
-- daily_transactions — staging table for daily transaction input
-- Maps to: DALYTRAN (AWS.M2.CARDDEMO.DALYTRAN.PS)
-- Copybook: CVTRA06Y (DALYTRAN-RECORD, 350 bytes)
-- ----------------------------------------------------------------------------
CREATE TABLE daily_transactions (
    tran_id             VARCHAR(16)     NOT NULL PRIMARY KEY,
    tran_type_cd        VARCHAR(2)      NOT NULL,
    tran_cat_cd         INTEGER         NOT NULL,
    tran_source         VARCHAR(10),
    tran_desc           VARCHAR(100),
    tran_amt            DECIMAL(11,2)   NOT NULL,
    tran_merchant_id    BIGINT,
    tran_merchant_name  VARCHAR(50),
    tran_merchant_city  VARCHAR(50),
    tran_merchant_zip   VARCHAR(10),
    tran_card_num       VARCHAR(16)     NOT NULL,
    tran_orig_ts        VARCHAR(26),
    tran_proc_ts        VARCHAR(26)
);

-- ----------------------------------------------------------------------------
-- transactions — posted transactions output
-- Maps to: TRANFILE (AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS)
-- Copybook: CVTRA05Y (TRAN-RECORD, 350 bytes)
-- ----------------------------------------------------------------------------
CREATE TABLE transactions (
    tran_id             VARCHAR(16)     NOT NULL PRIMARY KEY,
    tran_type_cd        VARCHAR(2)      NOT NULL,
    tran_cat_cd         INTEGER         NOT NULL,
    tran_source         VARCHAR(10),
    tran_desc           VARCHAR(100),
    tran_amt            DECIMAL(11,2)   NOT NULL,
    tran_merchant_id    BIGINT,
    tran_merchant_name  VARCHAR(50),
    tran_merchant_city  VARCHAR(50),
    tran_merchant_zip   VARCHAR(10),
    tran_card_num       VARCHAR(16)     NOT NULL,
    tran_orig_ts        VARCHAR(26),
    tran_proc_ts        VARCHAR(26)
);

-- ----------------------------------------------------------------------------
-- card_xref — card-to-account cross-reference
-- Maps to: XREFFILE (AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS)
-- Copybook: CVACT03Y (CARD-XREF-RECORD, 50 bytes)
-- ----------------------------------------------------------------------------
CREATE TABLE card_xref (
    xref_card_num       VARCHAR(16)     NOT NULL PRIMARY KEY,
    xref_cust_id        BIGINT,
    xref_acct_id        BIGINT
);

-- ----------------------------------------------------------------------------
-- accounts — account master data
-- Maps to: ACCTFILE (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS)
-- Copybook: CVACT01Y (ACCOUNT-RECORD, 300 bytes)
-- ----------------------------------------------------------------------------
CREATE TABLE accounts (
    acct_id                 BIGINT          NOT NULL PRIMARY KEY,
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

-- ----------------------------------------------------------------------------
-- transaction_category_balances — per-category balance aggregation
-- Maps to: TCATBALF (AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS)
-- Copybook: CVTRA01Y (TRAN-CAT-BAL-RECORD, 50 bytes)
-- Composite key: (trancat_acct_id, trancat_type_cd, trancat_cd)
-- ----------------------------------------------------------------------------
CREATE TABLE transaction_category_balances (
    trancat_acct_id     BIGINT          NOT NULL,
    trancat_type_cd     VARCHAR(2)      NOT NULL,
    trancat_cd          INTEGER         NOT NULL,
    tran_cat_bal        DECIMAL(11,2),
    PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
);

-- ----------------------------------------------------------------------------
-- transaction_rejects — rejected transaction records
-- Maps to: DALYREJS (GDG reject file, LRECL=430)
-- Contains original DALYTRAN fields + validation failure trailer
-- ----------------------------------------------------------------------------
CREATE TABLE transaction_rejects (
    reject_id                       BIGSERIAL       PRIMARY KEY,
    tran_id                         VARCHAR(16)     NOT NULL,
    tran_type_cd                    VARCHAR(2),
    tran_cat_cd                     INTEGER,
    tran_source                     VARCHAR(10),
    tran_desc                       VARCHAR(100),
    tran_amt                        DECIMAL(11,2),
    tran_merchant_id                BIGINT,
    tran_merchant_name              VARCHAR(50),
    tran_merchant_city              VARCHAR(50),
    tran_merchant_zip               VARCHAR(10),
    tran_card_num                   VARCHAR(16),
    tran_orig_ts                    VARCHAR(26),
    tran_proc_ts                    VARCHAR(26),
    validation_fail_reason          INTEGER         NOT NULL,
    validation_fail_reason_desc     VARCHAR(76)     NOT NULL
);

-- Indexes for common lookups
CREATE INDEX idx_card_xref_acct_id ON card_xref(xref_acct_id);
CREATE INDEX idx_transactions_card_num ON transactions(tran_card_num);
CREATE INDEX idx_transaction_rejects_tran_id ON transaction_rejects(tran_id);
