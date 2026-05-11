-- V1__init_unified.sql — both source-system schemas, side by side.

-- Nationwide (CardDemo VSAM-shaped tables)
CREATE TABLE IF NOT EXISTS nw_customer (
    cust_id              BIGINT      NOT NULL PRIMARY KEY,
    cust_first_name      VARCHAR(25),
    cust_middle_name     VARCHAR(25),
    cust_last_name       VARCHAR(25),
    cust_addr_line_1     VARCHAR(50),
    cust_addr_line_2     VARCHAR(50),
    cust_addr_line_3     VARCHAR(50),
    cust_addr_state_cd   CHAR(2),
    cust_addr_country_cd CHAR(3),
    cust_addr_zip        VARCHAR(10),
    cust_phone_num_1     VARCHAR(15),
    cust_phone_num_2     VARCHAR(15),
    cust_dob             DATE,
    cust_fico_credit_score INTEGER
);

CREATE TABLE IF NOT EXISTS nw_account (
    acct_id              BIGINT          NOT NULL PRIMARY KEY,
    acct_active_status   CHAR(1)         NOT NULL,
    acct_curr_bal        NUMERIC(12, 2)  NOT NULL,
    acct_credit_limit    NUMERIC(12, 2)  NOT NULL,
    acct_open_date       DATE,
    acct_expiration_date DATE,
    acct_addr_zip        VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS nw_card_xref (
    xref_card_num VARCHAR(16) NOT NULL PRIMARY KEY,
    xref_cust_id  BIGINT      NOT NULL REFERENCES nw_customer(cust_id),
    xref_acct_id  BIGINT      NOT NULL REFERENCES nw_account(acct_id)
);
CREATE INDEX IF NOT EXISTS idx_nw_xref_cust ON nw_card_xref(xref_cust_id);

-- Virgin Money (Spring Boot / JPA-shaped tables)
CREATE TABLE IF NOT EXISTS vm_primary_account (
    id              BIGINT          NOT NULL PRIMARY KEY,
    account_number  INTEGER,
    account_balance NUMERIC(12, 2)
);

CREATE TABLE IF NOT EXISTS vm_savings_account (
    id              BIGINT          NOT NULL PRIMARY KEY,
    account_number  INTEGER,
    account_balance NUMERIC(12, 2)
);

CREATE TABLE IF NOT EXISTS vm_user (
    user_id            BIGINT NOT NULL PRIMARY KEY,
    username           VARCHAR(255),
    first_name         VARCHAR(255),
    last_name          VARCHAR(255),
    email              VARCHAR(255) NOT NULL UNIQUE,
    phone              VARCHAR(255),
    enabled            BOOLEAN NOT NULL DEFAULT TRUE,
    primary_account_id BIGINT REFERENCES vm_primary_account(id),
    savings_account_id BIGINT REFERENCES vm_savings_account(id)
);
