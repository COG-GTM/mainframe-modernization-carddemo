-- Flyway migration V1: Create all CardDemo tables
-- Mapped from COBOL copybooks: CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y,
-- CVTRA05Y, CVTRA06Y, CVTRA03Y, CVTRA04Y, CVTRA01Y, CVTRA02Y, CSUSR01Y

-- Account entity (CVACT01Y.cpy, RECLN 300)
CREATE TABLE accounts (
    acct_id             BIGINT       NOT NULL PRIMARY KEY,
    acct_active_status  VARCHAR(1),
    acct_curr_bal       DECIMAL(12,2),
    acct_credit_limit   DECIMAL(12,2),
    acct_cash_credit_limit DECIMAL(12,2),
    acct_open_date      DATE,
    acct_expiration_date DATE,
    acct_reissue_date   DATE,
    acct_curr_cyc_credit DECIMAL(12,2),
    acct_curr_cyc_debit  DECIMAL(12,2),
    acct_addr_zip       VARCHAR(10),
    acct_group_id       VARCHAR(10)
);

-- Card entity (CVACT02Y.cpy, RECLN 150)
CREATE TABLE cards (
    card_num            VARCHAR(16)  NOT NULL PRIMARY KEY,
    card_acct_id        BIGINT,
    card_cvv_cd         INTEGER,
    card_embossed_name  VARCHAR(50),
    card_expiration_date DATE,
    card_active_status  VARCHAR(1)
);

-- Card cross-reference (CVACT03Y.cpy, RECLN 50)
CREATE TABLE card_xrefs (
    xref_card_num       VARCHAR(16)  NOT NULL PRIMARY KEY,
    xref_cust_id        BIGINT,
    xref_acct_id        BIGINT
);

-- Customer entity (CVCUS01Y.cpy, RECLN 500)
CREATE TABLE customers (
    cust_id             BIGINT       NOT NULL PRIMARY KEY,
    cust_first_name     VARCHAR(25),
    cust_middle_name    VARCHAR(25),
    cust_last_name      VARCHAR(25),
    cust_addr_line_1    VARCHAR(50),
    cust_addr_line_2    VARCHAR(50),
    cust_addr_line_3    VARCHAR(50),
    cust_addr_state_cd  VARCHAR(2),
    cust_addr_country_cd VARCHAR(3),
    cust_addr_zip       VARCHAR(10),
    cust_phone_num_1    VARCHAR(15),
    cust_phone_num_2    VARCHAR(15),
    cust_ssn            BIGINT,
    cust_govt_issued_id VARCHAR(20),
    cust_dob            DATE,
    cust_eft_account_id VARCHAR(10),
    cust_pri_card_holder_ind VARCHAR(1),
    cust_fico_credit_score INTEGER
);

-- Transaction entity (CVTRA05Y.cpy, RECLN 350)
CREATE TABLE transactions (
    tran_id             VARCHAR(16)  NOT NULL PRIMARY KEY,
    tran_type_cd        VARCHAR(2),
    tran_cat_cd         INTEGER,
    tran_source         VARCHAR(10),
    tran_desc           VARCHAR(100),
    tran_amt            DECIMAL(11,2),
    tran_merchant_id    BIGINT,
    tran_merchant_name  VARCHAR(50),
    tran_merchant_city  VARCHAR(50),
    tran_merchant_zip   VARCHAR(10),
    tran_card_num       VARCHAR(16),
    tran_orig_ts        VARCHAR(26),
    tran_proc_ts        VARCHAR(26)
);

-- Daily transaction entity (CVTRA06Y.cpy, RECLN 350)
CREATE TABLE daily_transactions (
    dalytran_id             VARCHAR(16)  NOT NULL PRIMARY KEY,
    dalytran_type_cd        VARCHAR(2),
    dalytran_cat_cd         INTEGER,
    dalytran_source         VARCHAR(10),
    dalytran_desc           VARCHAR(100),
    dalytran_amt            DECIMAL(11,2),
    dalytran_merchant_id    BIGINT,
    dalytran_merchant_name  VARCHAR(50),
    dalytran_merchant_city  VARCHAR(50),
    dalytran_merchant_zip   VARCHAR(10),
    dalytran_card_num       VARCHAR(16),
    dalytran_orig_ts        VARCHAR(26),
    dalytran_proc_ts        VARCHAR(26)
);

-- Transaction type reference (CVTRA03Y.cpy, RECLN 60)
CREATE TABLE transaction_types (
    tran_type           VARCHAR(2)   NOT NULL PRIMARY KEY,
    tran_type_desc      VARCHAR(50)
);

-- Transaction category reference (CVTRA04Y.cpy, RECLN 60)
CREATE TABLE transaction_categories (
    tran_type_cd        VARCHAR(2)   NOT NULL,
    tran_cat_cd         INTEGER      NOT NULL,
    tran_cat_type_desc  VARCHAR(50),
    PRIMARY KEY (tran_type_cd, tran_cat_cd)
);

-- Transaction category balance (CVTRA01Y.cpy, RECLN 50)
CREATE TABLE transaction_category_balances (
    trancat_acct_id     BIGINT       NOT NULL,
    trancat_type_cd     VARCHAR(2)   NOT NULL,
    trancat_cd          INTEGER      NOT NULL,
    tran_cat_bal        DECIMAL(11,2),
    PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
);

-- Disclosure group (CVTRA02Y.cpy, RECLN 50)
CREATE TABLE disclosure_groups (
    dis_acct_group_id   VARCHAR(10)  NOT NULL,
    dis_tran_type_cd    VARCHAR(2)   NOT NULL,
    dis_tran_cat_cd     INTEGER      NOT NULL,
    dis_int_rate        DECIMAL(6,2),
    PRIMARY KEY (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd)
);

-- User security (CSUSR01Y.cpy, RECLN 80)
CREATE TABLE user_security (
    sec_usr_id          VARCHAR(8)   NOT NULL PRIMARY KEY,
    sec_usr_fname       VARCHAR(20),
    sec_usr_lname       VARCHAR(20),
    sec_usr_pwd         VARCHAR(8),
    sec_usr_type        VARCHAR(1)
);
