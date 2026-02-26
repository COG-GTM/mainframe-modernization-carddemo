-- ==========================================================================
-- Flyway Migration V1: Create schema for CardDemo Transaction Service
--
-- Migrates VSAM file structures to relational tables:
--   TRANSACT (KSDS) -> transactions
--   CCXREF   (KSDS) -> card_cross_references
--   ACCTDAT  (KSDS) -> accounts
--
-- Source COBOL copybooks:
--   CVTRA05Y (TRAN-RECORD, 350 bytes)
--   CVACT03Y (CARD-XREF-RECORD, 50 bytes)
--   CVACT01Y (ACCOUNT-RECORD, 300 bytes)
-- ==========================================================================

-- Sequence for auto-generating transaction IDs
-- Replaces the COBOL pattern: STARTBR HIGH-VALUES, READPREV, ADD 1
CREATE SEQUENCE tran_id_seq START WITH 1 INCREMENT BY 1;

-- Transaction table (from VSAM TRANSACT file, copybook CVTRA05Y)
CREATE TABLE transactions (
    transaction_id   BIGINT       NOT NULL DEFAULT nextval('tran_id_seq'),
    type_code        VARCHAR(2)   NOT NULL,
    category_code    INTEGER      NOT NULL,
    source           VARCHAR(10)  NOT NULL,
    description      VARCHAR(100) NOT NULL,
    amount           DECIMAL(11,2) NOT NULL,
    merchant_id      BIGINT       NOT NULL,
    merchant_name    VARCHAR(50)  NOT NULL,
    merchant_city    VARCHAR(50)  NOT NULL,
    merchant_zip     VARCHAR(10)  NOT NULL,
    card_number      VARCHAR(16)  NOT NULL,
    originated_ts    TIMESTAMP    NOT NULL,
    processed_ts     TIMESTAMP    NOT NULL,
    PRIMARY KEY (transaction_id)
);

-- Index on card_number for lookups by card
CREATE INDEX idx_transactions_card_number ON transactions (card_number);

-- Card cross-reference table (from VSAM CCXREF + CXACAIX files, copybook CVACT03Y)
-- Primary key is card_number (matches CCXREF KSDS key = XREF-CARD-NUM)
-- Index on account_id replaces the CXACAIX alternate index
CREATE TABLE card_cross_references (
    card_number  VARCHAR(16)  NOT NULL,
    customer_id  BIGINT       NOT NULL,
    account_id   BIGINT       NOT NULL,
    PRIMARY KEY (card_number)
);

-- Alternate index replacement: CXACAIX was an AIX path on CCXREF keyed by XREF-ACCT-ID
CREATE INDEX idx_card_xref_account_id ON card_cross_references (account_id);

-- Account table (from VSAM ACCTDAT file, copybook CVACT01Y)
CREATE TABLE accounts (
    account_id           BIGINT        NOT NULL,
    active_status        VARCHAR(1)    NOT NULL,
    current_balance      DECIMAL(12,2) NOT NULL,
    credit_limit         DECIMAL(12,2) NOT NULL,
    cash_credit_limit    DECIMAL(12,2) NOT NULL,
    open_date            DATE,
    expiration_date      DATE,
    reissue_date         DATE,
    current_cycle_credit DECIMAL(12,2) NOT NULL,
    current_cycle_debit  DECIMAL(12,2) NOT NULL,
    address_zip          VARCHAR(10),
    group_id             VARCHAR(10),
    PRIMARY KEY (account_id)
);
