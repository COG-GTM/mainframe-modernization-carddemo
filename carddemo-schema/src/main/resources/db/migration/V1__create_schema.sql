-- ============================================================================
-- CardDemo PostgreSQL Schema
-- Migration V1: Create all tables from VSAM file structures
--
-- Maps 12 VSAM files to PostgreSQL relational tables with proper
-- primary keys, foreign keys, and constraints.
--
-- Source copybooks: CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSUSR01Y,
--                   CVTRA05Y, CVTRA06Y, CVTRA01Y, CVTRA02Y, CVTRA03Y,
--                   CVTRA04Y
-- ============================================================================

-- ============================================================================
-- 1. accounts (ACCTDAT - KSDS)
--    Source: CVACT01Y (RECLN 300), Key: ACCT-ID PIC 9(11)
-- ============================================================================
CREATE TABLE accounts (
    acct_id            BIGINT        PRIMARY KEY,                -- PIC 9(11)
    active_status      CHAR(1)       NOT NULL,                   -- PIC X(01), Y/N
    curr_bal           NUMERIC(12,2) NOT NULL,                   -- PIC S9(10)V99
    credit_limit       NUMERIC(12,2) NOT NULL,                   -- PIC S9(10)V99
    cash_credit_limit  NUMERIC(12,2) NOT NULL,                   -- PIC S9(10)V99
    open_date          VARCHAR(10),                              -- PIC X(10)
    expiration_date    VARCHAR(10),                              -- PIC X(10)
    reissue_date       VARCHAR(10),                              -- PIC X(10)
    curr_cyc_credit    NUMERIC(12,2) NOT NULL DEFAULT 0,         -- PIC S9(10)V99
    curr_cyc_debit     NUMERIC(12,2) NOT NULL DEFAULT 0,         -- PIC S9(10)V99
    addr_zip           VARCHAR(10),                              -- PIC X(10)
    group_id           VARCHAR(10),                              -- PIC X(10)

    CONSTRAINT chk_accounts_active_status CHECK (active_status IN ('Y', 'N'))
);

COMMENT ON TABLE accounts IS 'Credit card accounts - migrated from VSAM ACCTDAT (KSDS)';
COMMENT ON COLUMN accounts.acct_id IS 'Account identifier - PIC 9(11)';
COMMENT ON COLUMN accounts.active_status IS 'Account active flag: Y=active, N=inactive';
COMMENT ON COLUMN accounts.curr_bal IS 'Current balance - PIC S9(10)V99';
COMMENT ON COLUMN accounts.credit_limit IS 'Credit limit - PIC S9(10)V99';
COMMENT ON COLUMN accounts.cash_credit_limit IS 'Cash advance credit limit - PIC S9(10)V99';

-- ============================================================================
-- 2. customers (CUSTDAT - KSDS)
--    Source: CVCUS01Y (RECLN 500), Key: CUST-ID PIC 9(09)
-- ============================================================================
CREATE TABLE customers (
    cust_id              BIGINT       PRIMARY KEY,               -- PIC 9(09)
    first_name           VARCHAR(25),                            -- PIC X(25)
    middle_name          VARCHAR(25),                            -- PIC X(25)
    last_name            VARCHAR(25),                            -- PIC X(25)
    addr_line_1          VARCHAR(50),                            -- PIC X(50)
    addr_line_2          VARCHAR(50),                            -- PIC X(50)
    addr_line_3          VARCHAR(50),                            -- PIC X(50)
    addr_state_cd        CHAR(2),                                -- PIC X(02)
    addr_country_cd      CHAR(3),                                -- PIC X(03)
    addr_zip             VARCHAR(10),                            -- PIC X(10)
    phone_num_1          VARCHAR(15),                            -- PIC X(15)
    phone_num_2          VARCHAR(15),                            -- PIC X(15)
    ssn                  BIGINT,                                 -- PIC 9(09)
    govt_issued_id       VARCHAR(20),                            -- PIC X(20)
    dob_yyyy_mm_dd       VARCHAR(10),                            -- PIC X(10)
    eft_account_id       VARCHAR(10),                            -- PIC X(10)
    pri_card_holder_ind  CHAR(1),                                -- PIC X(01)
    fico_credit_score    SMALLINT                                -- PIC 9(03)
);

COMMENT ON TABLE customers IS 'Customer master data - migrated from VSAM CUSTDAT (KSDS)';
COMMENT ON COLUMN customers.cust_id IS 'Customer identifier - PIC 9(09)';
COMMENT ON COLUMN customers.ssn IS 'Social Security Number - PIC 9(09)';
COMMENT ON COLUMN customers.fico_credit_score IS 'FICO credit score - PIC 9(03)';

-- ============================================================================
-- 3. cards (CARDDAT - KSDS)
--    Source: CVACT02Y (RECLN 150), Key: CARD-NUM PIC X(16)
-- ============================================================================
CREATE TABLE cards (
    card_num          VARCHAR(16)   PRIMARY KEY,                 -- PIC X(16)
    acct_id           BIGINT        NOT NULL,                    -- PIC 9(11)
    cvv_cd            SMALLINT      NOT NULL,                    -- PIC 9(03)
    embossed_name     VARCHAR(50),                               -- PIC X(50)
    expiration_date   VARCHAR(10),                               -- PIC X(10)
    active_status     CHAR(1)       NOT NULL,                    -- PIC X(01)

    CONSTRAINT fk_cards_account FOREIGN KEY (acct_id) REFERENCES accounts(acct_id),
    CONSTRAINT chk_cards_active_status CHECK (active_status IN ('Y', 'N'))
);

COMMENT ON TABLE cards IS 'Credit card data - migrated from VSAM CARDDAT (KSDS)';
COMMENT ON COLUMN cards.card_num IS 'Card number - PIC X(16)';
COMMENT ON COLUMN cards.cvv_cd IS 'CVV security code - PIC 9(03)';

-- ============================================================================
-- 4. card_xref (CARDXREF - KSDS)
--    Source: CVACT03Y (RECLN 50), Key: XREF-CARD-NUM PIC X(16)
-- ============================================================================
CREATE TABLE card_xref (
    card_num  VARCHAR(16)  PRIMARY KEY,                          -- PIC X(16)
    cust_id   BIGINT       NOT NULL,                             -- PIC 9(09)
    acct_id   BIGINT       NOT NULL,                             -- PIC 9(11)

    CONSTRAINT fk_card_xref_customer FOREIGN KEY (cust_id) REFERENCES customers(cust_id),
    CONSTRAINT fk_card_xref_account  FOREIGN KEY (acct_id) REFERENCES accounts(acct_id)
);

COMMENT ON TABLE card_xref IS 'Card cross-reference linking cards to customers and accounts - migrated from VSAM CARDXREF (KSDS)';

-- ============================================================================
-- 5. users (USRSEC - KSDS)
--    Source: CSUSR01Y (RECLN 80), Key: SEC-USR-ID PIC X(08)
-- ============================================================================
CREATE TABLE users (
    usr_id      VARCHAR(8)   PRIMARY KEY,                        -- PIC X(08)
    first_name  VARCHAR(20),                                     -- PIC X(20)
    last_name   VARCHAR(20),                                     -- PIC X(20)
    password    VARCHAR(72),                                     -- bcrypt hash (replaces PIC X(08) plaintext)
    usr_type    CHAR(1)      NOT NULL DEFAULT 'U',               -- PIC X(01)

    CONSTRAINT chk_users_type CHECK (usr_type IN ('A', 'U'))     -- A=Admin, U=User
);

COMMENT ON TABLE users IS 'User security data - migrated from VSAM USRSEC (KSDS)';
COMMENT ON COLUMN users.usr_id IS 'User identifier - PIC X(08)';
COMMENT ON COLUMN users.password IS 'Password hash (bcrypt) - originally PIC X(08) plaintext';
COMMENT ON COLUMN users.usr_type IS 'User type: A=Admin, U=User';

-- ============================================================================
-- 6. transaction_types (TRANTYPE - KSDS)
--    Source: CVTRA03Y (RECLN 60), Key: TRAN-TYPE PIC X(02)
-- ============================================================================
CREATE TABLE transaction_types (
    type_cd      CHAR(2)      PRIMARY KEY,                       -- PIC X(02)
    description  VARCHAR(50)                                     -- PIC X(50)
);

COMMENT ON TABLE transaction_types IS 'Transaction type reference data - migrated from VSAM TRANTYPE (KSDS)';

-- ============================================================================
-- 7. transaction_categories (TRANCATG - KSDS)
--    Source: CVTRA04Y (RECLN 60), Composite Key: TRAN-TYPE-CD + TRAN-CAT-CD
-- ============================================================================
CREATE TABLE transaction_categories (
    type_cd      CHAR(2)      NOT NULL,                          -- PIC X(02)
    cat_cd       SMALLINT     NOT NULL,                          -- PIC 9(04)
    description  VARCHAR(50),                                    -- PIC X(50)

    PRIMARY KEY (type_cd, cat_cd),
    CONSTRAINT fk_tran_cat_type FOREIGN KEY (type_cd) REFERENCES transaction_types(type_cd)
);

COMMENT ON TABLE transaction_categories IS 'Transaction category reference data - migrated from VSAM TRANCATG (KSDS)';

-- ============================================================================
-- 8. transactions (TRANSACT - KSDS)
--    Source: CVTRA05Y (RECLN 350), Key: TRAN-ID PIC X(16)
-- ============================================================================
CREATE TABLE transactions (
    tran_id        VARCHAR(16)    PRIMARY KEY,                   -- PIC X(16)
    type_cd        CHAR(2)        NOT NULL,                      -- PIC X(02)
    cat_cd         SMALLINT       NOT NULL,                      -- PIC 9(04)
    source         VARCHAR(10),                                  -- PIC X(10)
    description    VARCHAR(100),                                 -- PIC X(100)
    amount         NUMERIC(11,2)  NOT NULL,                      -- PIC S9(09)V99
    merchant_id    BIGINT,                                       -- PIC 9(09)
    merchant_name  VARCHAR(50),                                  -- PIC X(50)
    merchant_city  VARCHAR(50),                                  -- PIC X(50)
    merchant_zip   VARCHAR(10),                                  -- PIC X(10)
    card_num       VARCHAR(16)    NOT NULL,                      -- PIC X(16)
    orig_ts        VARCHAR(26),                                  -- PIC X(26)
    proc_ts        VARCHAR(26)                                   -- PIC X(26)
);

COMMENT ON TABLE transactions IS 'Transaction records - migrated from VSAM TRANSACT (KSDS)';
COMMENT ON COLUMN transactions.tran_id IS 'Transaction identifier - PIC X(16)';
COMMENT ON COLUMN transactions.orig_ts IS 'Original timestamp - PIC X(26)';
COMMENT ON COLUMN transactions.proc_ts IS 'Processing timestamp - PIC X(26)';

-- ============================================================================
-- 9. daily_transactions (DALYTRAN - ESDS)
--    Source: CVTRA06Y (RECLN 350) - same structure as transactions, append-only
--    ESDS has no primary key; uses a surrogate sequence in PostgreSQL.
-- ============================================================================
CREATE TABLE daily_transactions (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,  -- surrogate PK for ESDS
    tran_id        VARCHAR(16)    NOT NULL,                      -- PIC X(16)
    type_cd        CHAR(2)        NOT NULL,                      -- PIC X(02)
    cat_cd         SMALLINT       NOT NULL,                      -- PIC 9(04)
    source         VARCHAR(10),                                  -- PIC X(10)
    description    VARCHAR(100),                                 -- PIC X(100)
    amount         NUMERIC(11,2)  NOT NULL,                      -- PIC S9(09)V99
    merchant_id    BIGINT,                                       -- PIC 9(09)
    merchant_name  VARCHAR(50),                                  -- PIC X(50)
    merchant_city  VARCHAR(50),                                  -- PIC X(50)
    merchant_zip   VARCHAR(10),                                  -- PIC X(10)
    card_num       VARCHAR(16)    NOT NULL,                      -- PIC X(16)
    orig_ts        VARCHAR(26),                                  -- PIC X(26)
    proc_ts        VARCHAR(26)                                   -- PIC X(26)
);

COMMENT ON TABLE daily_transactions IS 'Daily transaction log (append-only) - migrated from VSAM DALYTRAN (ESDS)';

-- ============================================================================
-- 10. daily_rejects (DALYREJS)
--     Rejected transactions with reject reason
-- ============================================================================
CREATE TABLE daily_rejects (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,  -- surrogate PK
    tran_id         VARCHAR(16)    NOT NULL,                     -- PIC X(16)
    type_cd         CHAR(2)        NOT NULL,                     -- PIC X(02)
    cat_cd          SMALLINT       NOT NULL,                     -- PIC 9(04)
    source          VARCHAR(10),                                 -- PIC X(10)
    description     VARCHAR(100),                                -- PIC X(100)
    amount          NUMERIC(11,2)  NOT NULL,                     -- PIC S9(09)V99
    merchant_id     BIGINT,                                      -- PIC 9(09)
    merchant_name   VARCHAR(50),                                 -- PIC X(50)
    merchant_city   VARCHAR(50),                                 -- PIC X(50)
    merchant_zip    VARCHAR(10),                                 -- PIC X(10)
    card_num        VARCHAR(16)    NOT NULL,                     -- PIC X(16)
    orig_ts         VARCHAR(26),                                 -- PIC X(26)
    proc_ts         VARCHAR(26),                                 -- PIC X(26)
    reject_cd       VARCHAR(10),                                 -- reject reason code
    reject_message  VARCHAR(100)                                 -- human-readable reject reason
);

COMMENT ON TABLE daily_rejects IS 'Rejected daily transactions with error details - migrated from VSAM DALYREJS';

-- ============================================================================
-- 11. tran_cat_balances (TCATBALF - KSDS)
--     Source: CVTRA01Y (RECLN 50)
--     Composite Key: TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD
-- ============================================================================
CREATE TABLE tran_cat_balances (
    acct_id   BIGINT        NOT NULL,                            -- PIC 9(11)
    type_cd   CHAR(2)       NOT NULL,                            -- PIC X(02)
    cat_cd    SMALLINT      NOT NULL,                            -- PIC 9(04)
    balance   NUMERIC(11,2) NOT NULL DEFAULT 0,                  -- PIC S9(09)V99

    PRIMARY KEY (acct_id, type_cd, cat_cd)
);

COMMENT ON TABLE tran_cat_balances IS 'Transaction category balances per account - migrated from VSAM TCATBALF (KSDS)';

-- ============================================================================
-- 12. disclosure_groups (DISCGRP - KSDS)
--     Source: CVTRA02Y (RECLN 50)
--     Composite Key: DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD
-- ============================================================================
CREATE TABLE disclosure_groups (
    acct_group_id  VARCHAR(10)    NOT NULL,                      -- PIC X(10)
    tran_type_cd   CHAR(2)        NOT NULL,                      -- PIC X(02)
    tran_cat_cd    SMALLINT       NOT NULL,                      -- PIC 9(04)
    int_rate       NUMERIC(6,2)   NOT NULL,                      -- PIC S9(04)V99

    PRIMARY KEY (acct_group_id, tran_type_cd, tran_cat_cd)
);

COMMENT ON TABLE disclosure_groups IS 'Disclosure group interest rates - migrated from VSAM DISCGRP (KSDS)';
COMMENT ON COLUMN disclosure_groups.int_rate IS 'Interest rate for this disclosure group - PIC S9(04)V99';
