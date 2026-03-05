-- CardDemo Schema Migration V1
-- Migrated from COBOL VSAM copybooks and DB2 DDL
-- All monetary fields use NUMERIC (BigDecimal in Java)

CREATE TABLE user_security (
    user_id VARCHAR(8) NOT NULL PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20),
    password VARCHAR(72) NOT NULL,
    user_type VARCHAR(5) NOT NULL
);

CREATE TABLE account (
    acct_id BIGINT NOT NULL PRIMARY KEY,
    active_status VARCHAR(1),
    current_balance NUMERIC(12,2),
    credit_limit NUMERIC(12,2),
    cash_credit_limit NUMERIC(12,2),
    open_date DATE,
    expiration_date DATE,
    reissue_date DATE,
    current_cycle_credit NUMERIC(12,2),
    current_cycle_debit NUMERIC(12,2),
    addr_zip VARCHAR(10),
    group_id VARCHAR(10)
);

CREATE TABLE card (
    card_num VARCHAR(16) NOT NULL PRIMARY KEY,
    acct_id BIGINT,
    cvv_code INTEGER,
    embossed_name VARCHAR(50),
    expiration_date DATE,
    active_status VARCHAR(1)
);
CREATE INDEX idx_card_acct_id ON card(acct_id);

CREATE TABLE customer (
    cust_id BIGINT NOT NULL PRIMARY KEY,
    first_name VARCHAR(25),
    middle_name VARCHAR(25),
    last_name VARCHAR(25),
    addr_line_1 VARCHAR(50),
    addr_line_2 VARCHAR(50),
    addr_line_3 VARCHAR(50),
    addr_state_cd VARCHAR(2),
    addr_country_cd VARCHAR(3),
    addr_zip VARCHAR(10),
    phone_num_1 VARCHAR(15),
    phone_num_2 VARCHAR(15),
    ssn VARCHAR(9),
    govt_issued_id VARCHAR(20),
    date_of_birth DATE,
    eft_account_id VARCHAR(10),
    pri_card_holder_ind VARCHAR(1),
    fico_credit_score INTEGER
);

CREATE TABLE card_xref (
    card_num VARCHAR(16) NOT NULL PRIMARY KEY,
    cust_id BIGINT,
    acct_id BIGINT
);
CREATE INDEX idx_card_xref_acct_id ON card_xref(acct_id);
CREATE INDEX idx_card_xref_cust_id ON card_xref(cust_id);

CREATE TABLE transaction (
    transaction_id VARCHAR(16) NOT NULL PRIMARY KEY,
    type_cd VARCHAR(2),
    category_cd INTEGER,
    source VARCHAR(10),
    description VARCHAR(100),
    amount NUMERIC(11,2),
    merchant_id BIGINT,
    merchant_name VARCHAR(50),
    merchant_city VARCHAR(50),
    merchant_zip VARCHAR(10),
    card_num VARCHAR(16),
    orig_timestamp TIMESTAMP,
    proc_timestamp TIMESTAMP
);
CREATE INDEX idx_transaction_card_num ON transaction(card_num);
CREATE INDEX idx_transaction_orig_ts ON transaction(orig_timestamp);

CREATE TABLE daily_transaction (
    transaction_id VARCHAR(16) NOT NULL PRIMARY KEY,
    type_cd VARCHAR(2),
    category_cd INTEGER,
    source VARCHAR(10),
    description VARCHAR(100),
    amount NUMERIC(11,2),
    merchant_id BIGINT,
    merchant_name VARCHAR(50),
    merchant_city VARCHAR(50),
    merchant_zip VARCHAR(10),
    card_num VARCHAR(16),
    orig_timestamp TIMESTAMP,
    proc_timestamp TIMESTAMP
);

CREATE TABLE daily_transaction_reject (
    reject_id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(16),
    card_num VARCHAR(16),
    amount NUMERIC(11,2),
    reject_reason_code INTEGER,
    reject_reason_description VARCHAR(76),
    reject_timestamp TIMESTAMP
);

CREATE TABLE transaction_category_balance (
    acct_id BIGINT NOT NULL,
    tran_type_cd VARCHAR(2) NOT NULL,
    tran_cat_cd INTEGER NOT NULL,
    balance NUMERIC(11,2),
    PRIMARY KEY (acct_id, tran_type_cd, tran_cat_cd)
);

CREATE TABLE disclosure_group (
    group_id VARCHAR(10) NOT NULL,
    tran_type_cd VARCHAR(2) NOT NULL,
    tran_cat_cd INTEGER NOT NULL,
    interest_rate NUMERIC(6,2),
    PRIMARY KEY (group_id, tran_type_cd, tran_cat_cd)
);

CREATE TABLE transaction_type (
    type_code VARCHAR(2) NOT NULL PRIMARY KEY,
    type_description VARCHAR(50)
);

CREATE TABLE transaction_category (
    type_cd VARCHAR(2) NOT NULL,
    category_cd INTEGER NOT NULL,
    category_description VARCHAR(50),
    PRIMARY KEY (type_cd, category_cd)
);

CREATE TABLE authorization_fraud (
    fraud_id BIGSERIAL PRIMARY KEY,
    card_num VARCHAR(16),
    auth_timestamp TIMESTAMP,
    auth_type VARCHAR(10),
    transaction_amt NUMERIC(11,2),
    approved_amt NUMERIC(11,2),
    merchant_id VARCHAR(20),
    auth_fraud_flag VARCHAR(1)
);

CREATE TABLE authorization_summary (
    auth_id BIGSERIAL PRIMARY KEY,
    card_num VARCHAR(16) NOT NULL,
    auth_timestamp TIMESTAMP NOT NULL,
    auth_type VARCHAR(10),
    transaction_amt NUMERIC(11,2),
    approved_amt NUMERIC(11,2),
    auth_status VARCHAR(10),
    decline_reason VARCHAR(50),
    merchant_id VARCHAR(20)
);
CREATE INDEX idx_auth_summary_card_num ON authorization_summary(card_num);
CREATE INDEX idx_auth_summary_timestamp ON authorization_summary(auth_timestamp);

CREATE TABLE authorization_detail (
    detail_id BIGSERIAL PRIMARY KEY,
    auth_id BIGINT NOT NULL REFERENCES authorization_summary(auth_id),
    detail_type VARCHAR(20),
    detail_amount NUMERIC(11,2),
    detail_timestamp TIMESTAMP,
    detail_description VARCHAR(100)
);
