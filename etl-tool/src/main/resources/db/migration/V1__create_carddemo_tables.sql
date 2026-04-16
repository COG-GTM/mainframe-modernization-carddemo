-- CardDemo EBCDIC Data Migration Schema
-- Derived from COBOL copybook record layouts

CREATE TABLE IF NOT EXISTS accounts (
    acct_id             BIGINT          NOT NULL,
    acct_active_status  VARCHAR(1),
    acct_curr_bal       NUMERIC(12,2),
    acct_credit_limit   NUMERIC(12,2),
    acct_cash_credit_limit NUMERIC(12,2),
    acct_open_date      VARCHAR(10),
    acct_expiration_date VARCHAR(10),
    acct_reissue_date   VARCHAR(10),
    acct_curr_cyc_credit NUMERIC(12,2),
    acct_curr_cyc_debit  NUMERIC(12,2),
    acct_addr_zip       VARCHAR(10),
    acct_group_id       VARCHAR(10),
    CONSTRAINT pk_accounts PRIMARY KEY (acct_id)
);

CREATE TABLE IF NOT EXISTS customers (
    cust_id                 BIGINT          NOT NULL,
    cust_first_name         VARCHAR(25),
    cust_middle_name        VARCHAR(25),
    cust_last_name          VARCHAR(25),
    cust_addr_line_1        VARCHAR(50),
    cust_addr_line_2        VARCHAR(50),
    cust_addr_line_3        VARCHAR(50),
    cust_addr_state_cd      VARCHAR(2),
    cust_addr_country_cd    VARCHAR(3),
    cust_addr_zip           VARCHAR(10),
    cust_phone_num_1        VARCHAR(15),
    cust_phone_num_2        VARCHAR(15),
    cust_ssn                BIGINT,
    cust_govt_issued_id     VARCHAR(20),
    cust_dob_yyyy_mm_dd     VARCHAR(10),
    cust_eft_account_id     VARCHAR(10),
    cust_pri_card_holder_ind VARCHAR(1),
    cust_fico_credit_score  INTEGER,
    CONSTRAINT pk_customers PRIMARY KEY (cust_id)
);

CREATE TABLE IF NOT EXISTS cards (
    card_num            VARCHAR(16)     NOT NULL,
    card_acct_id        BIGINT          NOT NULL,
    card_cvv_cd         INTEGER,
    card_embossed_name  VARCHAR(50),
    card_expiration_date VARCHAR(10),
    card_active_status  VARCHAR(1),
    CONSTRAINT pk_cards PRIMARY KEY (card_num)
);

CREATE TABLE IF NOT EXISTS card_xref (
    xref_card_num       VARCHAR(16)     NOT NULL,
    xref_cust_id        BIGINT          NOT NULL,
    xref_acct_id        BIGINT          NOT NULL,
    CONSTRAINT pk_card_xref PRIMARY KEY (xref_card_num)
);

CREATE TABLE IF NOT EXISTS user_security (
    sec_usr_id          VARCHAR(8)      NOT NULL,
    sec_usr_fname       VARCHAR(20),
    sec_usr_lname       VARCHAR(20),
    sec_usr_pwd         VARCHAR(8),
    sec_usr_type        VARCHAR(1),
    CONSTRAINT pk_user_security PRIMARY KEY (sec_usr_id)
);

CREATE TABLE IF NOT EXISTS transactions (
    tran_id             VARCHAR(16)     NOT NULL,
    tran_type_cd        VARCHAR(2),
    tran_cat_cd         INTEGER,
    tran_source         VARCHAR(10),
    tran_desc           VARCHAR(100),
    tran_amt            NUMERIC(11,2),
    tran_merchant_id    BIGINT,
    tran_merchant_name  VARCHAR(50),
    tran_merchant_city  VARCHAR(50),
    tran_merchant_zip   VARCHAR(10),
    tran_card_num       VARCHAR(16),
    tran_orig_ts        VARCHAR(26),
    tran_proc_ts        VARCHAR(26),
    CONSTRAINT pk_transactions PRIMARY KEY (tran_id)
);

CREATE TABLE IF NOT EXISTS daily_transactions (
    tran_id             VARCHAR(16)     NOT NULL,
    tran_type_cd        VARCHAR(2),
    tran_cat_cd         INTEGER,
    tran_source         VARCHAR(10),
    tran_desc           VARCHAR(100),
    tran_amt            NUMERIC(11,2),
    tran_merchant_id    BIGINT,
    tran_merchant_name  VARCHAR(50),
    tran_merchant_city  VARCHAR(50),
    tran_merchant_zip   VARCHAR(10),
    tran_card_num       VARCHAR(16),
    tran_orig_ts        VARCHAR(26),
    tran_proc_ts        VARCHAR(26),
    CONSTRAINT pk_daily_transactions PRIMARY KEY (tran_id)
);

CREATE TABLE IF NOT EXISTS tran_cat_balance (
    trancat_acct_id     BIGINT          NOT NULL,
    trancat_type_cd     VARCHAR(2)      NOT NULL,
    trancat_cd          INTEGER         NOT NULL,
    tran_cat_bal        NUMERIC(11,2),
    CONSTRAINT pk_tran_cat_balance PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
);

CREATE TABLE IF NOT EXISTS disclosure_group (
    dis_acct_group_id   VARCHAR(10)     NOT NULL,
    dis_tran_type_cd    VARCHAR(2)      NOT NULL,
    dis_tran_cat_cd     INTEGER         NOT NULL,
    dis_int_rate        NUMERIC(6,2),
    CONSTRAINT pk_disclosure_group PRIMARY KEY (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd)
);

CREATE TABLE IF NOT EXISTS tran_type (
    tran_type           VARCHAR(2)      NOT NULL,
    tran_type_desc      VARCHAR(50),
    CONSTRAINT pk_tran_type PRIMARY KEY (tran_type)
);

CREATE TABLE IF NOT EXISTS tran_category (
    tran_type_cd        VARCHAR(2)      NOT NULL,
    tran_cat_cd         INTEGER         NOT NULL,
    tran_cat_type_desc  VARCHAR(50),
    CONSTRAINT pk_tran_category PRIMARY KEY (tran_type_cd, tran_cat_cd)
);
