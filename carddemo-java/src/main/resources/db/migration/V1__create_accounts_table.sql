-- Flyway migration: Create accounts table
-- Source: COBOL copybook CVACT01Y.cpy (ACCOUNT-RECORD, 300 bytes)
-- Seed data: acctdata.txt (50 records)

CREATE TABLE accounts (
    acct_id             BIGINT       NOT NULL PRIMARY KEY,   -- PIC 9(11)
    acct_active_status  VARCHAR(1),                          -- PIC X(01)
    acct_curr_bal       DECIMAL(12,2),                       -- PIC S9(10)V99
    acct_credit_limit   DECIMAL(12,2),                       -- PIC S9(10)V99
    acct_cash_credit_limit DECIMAL(12,2),                    -- PIC S9(10)V99
    acct_open_date      DATE,                                -- PIC X(10) YYYY-MM-DD
    acct_expiration_date DATE,                               -- PIC X(10) YYYY-MM-DD
    acct_reissue_date   DATE,                                -- PIC X(10) YYYY-MM-DD
    acct_curr_cyc_credit DECIMAL(12,2),                      -- PIC S9(10)V99
    acct_curr_cyc_debit  DECIMAL(12,2),                      -- PIC S9(10)V99
    acct_addr_zip       VARCHAR(10),                         -- PIC X(10)
    acct_group_id       VARCHAR(10)                          -- PIC X(10)
);
