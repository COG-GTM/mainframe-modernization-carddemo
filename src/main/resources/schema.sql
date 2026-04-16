-- CardDemo Batch Transaction Posting Schema
-- Maps VSAM files to PostgreSQL tables

-- DALYTRAN (input, sequential) - daily transaction file
CREATE TABLE IF NOT EXISTS daily_transaction (
    dalytran_id          VARCHAR(16) PRIMARY KEY,
    dalytran_type_cd     VARCHAR(2),
    dalytran_cat_cd      INTEGER,
    dalytran_source      VARCHAR(10),
    dalytran_desc        VARCHAR(100),
    dalytran_amt         NUMERIC(11,2),
    dalytran_merchant_id BIGINT,
    dalytran_merchant_name VARCHAR(50),
    dalytran_merchant_city VARCHAR(50),
    dalytran_merchant_zip  VARCHAR(10),
    dalytran_card_num    VARCHAR(16),
    dalytran_orig_ts     VARCHAR(26),
    dalytran_proc_ts     VARCHAR(26)
);

-- TRANFILE (output, indexed by TRAN-ID) - posted transactions
CREATE TABLE IF NOT EXISTS transaction (
    tran_id              VARCHAR(16) PRIMARY KEY,
    tran_type_cd         VARCHAR(2),
    tran_cat_cd          INTEGER,
    tran_source          VARCHAR(10),
    tran_desc            VARCHAR(100),
    tran_amt             NUMERIC(11,2),
    tran_merchant_id     BIGINT,
    tran_merchant_name   VARCHAR(50),
    tran_merchant_city   VARCHAR(50),
    tran_merchant_zip    VARCHAR(10),
    tran_card_num        VARCHAR(16),
    tran_orig_ts         VARCHAR(26),
    tran_proc_ts         VARCHAR(26)
);

-- ACCTFILE (I-O, indexed by ACCT-ID) - account master
CREATE TABLE IF NOT EXISTS account (
    acct_id               BIGINT PRIMARY KEY,
    acct_active_status    VARCHAR(1),
    acct_curr_bal         NUMERIC(12,2),
    acct_credit_limit     NUMERIC(12,2),
    acct_cash_credit_limit NUMERIC(12,2),
    acct_open_date        VARCHAR(10),
    acct_expiration_date  VARCHAR(10),
    acct_reissue_date     VARCHAR(10),
    acct_curr_cyc_credit  NUMERIC(12,2),
    acct_curr_cyc_debit   NUMERIC(12,2),
    acct_addr_zip         VARCHAR(10),
    acct_group_id         VARCHAR(10)
);

-- XREFFILE (input, indexed by card number) - card cross-reference
CREATE TABLE IF NOT EXISTS card_xref (
    xref_card_num        VARCHAR(16) PRIMARY KEY,
    xref_cust_id         BIGINT,
    xref_acct_id         BIGINT
);

-- TCATBALF (I-O, indexed by ACCT-ID+TYPE-CD+CAT-CD) - transaction category balances
CREATE TABLE IF NOT EXISTS tran_cat_bal (
    trancat_acct_id      BIGINT,
    trancat_type_cd      VARCHAR(2),
    trancat_cd           INTEGER,
    tran_cat_bal         NUMERIC(11,2),
    PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
);

-- DALYREJS (output, sequential) - rejected transactions with validation trailer
CREATE TABLE IF NOT EXISTS rejected_transaction (
    seq_id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tran_id               VARCHAR(16),
    validation_fail_reason INTEGER,
    validation_fail_desc  VARCHAR(76),
    original_record       VARCHAR(1000)
);
