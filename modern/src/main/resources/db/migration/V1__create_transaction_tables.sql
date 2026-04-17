-- Phase 5: Transaction Service Database Schema
-- COBOL Traceability: Creates relational tables mapping the following VSAM files:
--   TRANSACT (CVTRA05Y.cpy) -> transaction
--   TRANTYPE (CVTRA03Y.cpy) -> transaction_type
--   TRANCATG (CVTRA04Y.cpy) -> transaction_category
--   TCATBALF (CVTRA01Y.cpy) -> category_balance
--   ACCTDAT  (CVACT01Y.cpy) -> account
--   CARDXREF (CVACT03Y.cpy) -> card_xref
--   DISCGRP  (CVTRA02Y.cpy) -> disclosure_group
--   (New)                    -> report_request

-- Account table (stub for Phase 5, maps ACCTDAT / CVACT01Y.cpy)
CREATE TABLE IF NOT EXISTS account (
    account_id      VARCHAR(11) NOT NULL PRIMARY KEY,
    active_status   VARCHAR(1),
    current_balance NUMERIC(12, 2),
    credit_limit    NUMERIC(12, 2),
    cash_credit_limit NUMERIC(12, 2),
    open_date       VARCHAR(10),
    expiration_date VARCHAR(10),
    reissue_date    VARCHAR(10),
    current_cycle_credit NUMERIC(12, 2),
    current_cycle_debit  NUMERIC(12, 2),
    address_zip     VARCHAR(10),
    group_id        VARCHAR(10)
);

-- Card cross-reference table (maps CARDXREF / CVACT03Y.cpy)
CREATE TABLE IF NOT EXISTS card_xref (
    card_number VARCHAR(16) NOT NULL PRIMARY KEY,
    customer_id VARCHAR(9),
    account_id  VARCHAR(11) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_card_xref_account ON card_xref(account_id);

-- Transaction type reference table (maps TRANTYPE / CVTRA03Y.cpy)
CREATE TABLE IF NOT EXISTS transaction_type (
    type_code   VARCHAR(2)  NOT NULL PRIMARY KEY,
    description VARCHAR(50) NOT NULL
);

-- Transaction category reference table (maps TRANCATG / CVTRA04Y.cpy)
CREATE TABLE IF NOT EXISTS transaction_category (
    type_code     VARCHAR(2) NOT NULL,
    category_code INT        NOT NULL,
    description   VARCHAR(50) NOT NULL,
    PRIMARY KEY (type_code, category_code)
);

-- Transaction table (maps TRANSACT / CVTRA05Y.cpy, RECLN=350)
CREATE TABLE IF NOT EXISTS transaction (
    transaction_id      VARCHAR(16) NOT NULL PRIMARY KEY,
    type_code           VARCHAR(2)  NOT NULL,
    category_code       INT         NOT NULL,
    source              VARCHAR(10),
    description         VARCHAR(100),
    amount              NUMERIC(11, 2) NOT NULL,
    merchant_id         BIGINT,
    merchant_name       VARCHAR(50),
    merchant_city       VARCHAR(50),
    merchant_zip        VARCHAR(10),
    card_number         VARCHAR(16) NOT NULL,
    origin_timestamp    TIMESTAMP,
    processed_timestamp TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transaction_card ON transaction(card_number);
CREATE INDEX IF NOT EXISTS idx_transaction_origin_ts ON transaction(origin_timestamp);

-- Category balance table (maps TCATBALF / CVTRA01Y.cpy, RECLN=50)
CREATE TABLE IF NOT EXISTS category_balance (
    account_id    VARCHAR(11) NOT NULL,
    type_code     VARCHAR(2)  NOT NULL,
    category_code INT         NOT NULL,
    balance       NUMERIC(11, 2) NOT NULL,
    PRIMARY KEY (account_id, type_code, category_code)
);

-- Disclosure group table (maps DISCGRP / CVTRA02Y.cpy, RECLN=50)
-- Used by CBACT04C for interest rate lookup
CREATE TABLE IF NOT EXISTS disclosure_group (
    account_group_id           VARCHAR(10) NOT NULL,
    transaction_type_code      VARCHAR(2)  NOT NULL,
    transaction_category_code  INT         NOT NULL,
    interest_rate              NUMERIC(6, 2) NOT NULL,
    PRIMARY KEY (account_group_id, transaction_type_code, transaction_category_code)
);

-- Report request table (new — replaces CICS Transient Data queue used by CORPT00C)
CREATE TABLE IF NOT EXISTS report_request (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_date   DATE        NOT NULL,
    end_date     DATE        NOT NULL,
    report_type  VARCHAR(20) NOT NULL,
    status       VARCHAR(20) NOT NULL,
    created_at   TIMESTAMP   NOT NULL,
    completed_at TIMESTAMP
);
