-- PostgreSQL schema for the CardDemo Account entity (copybook CVACT01Y / VSAM ACCTDAT).
-- Generated as the load target of the Account ETL.

CREATE TABLE IF NOT EXISTS accounts (
    acct_id            BIGINT         PRIMARY KEY,                 -- ACCT-ID                PIC 9(11)
    active_status      CHAR(1)        NOT NULL,                    -- ACCT-ACTIVE-STATUS     PIC X(01)
    curr_bal           NUMERIC(12,2)  NOT NULL,                    -- ACCT-CURR-BAL          PIC S9(10)V99
    credit_limit       NUMERIC(12,2)  NOT NULL,                    -- ACCT-CREDIT-LIMIT      PIC S9(10)V99
    cash_credit_limit  NUMERIC(12,2)  NOT NULL,                    -- ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
    open_date          DATE,                                       -- ACCT-OPEN-DATE         PIC X(10)
    expiration_date    DATE,                                       -- ACCT-EXPIRAION-DATE    PIC X(10)
    reissue_date       DATE,                                       -- ACCT-REISSUE-DATE      PIC X(10)
    curr_cyc_credit    NUMERIC(12,2)  NOT NULL,                    -- ACCT-CURR-CYC-CREDIT   PIC S9(10)V99
    curr_cyc_debit     NUMERIC(12,2)  NOT NULL,                    -- ACCT-CURR-CYC-DEBIT    PIC S9(10)V99
    addr_zip           VARCHAR(10),                                -- ACCT-ADDR-ZIP          PIC X(10)
    group_id           VARCHAR(10),                                -- ACCT-GROUP-ID          PIC X(10)
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT chk_active_status CHECK (active_status IN ('Y', 'N')),
    CONSTRAINT chk_credit_ge_cash CHECK (credit_limit >= cash_credit_limit)
);
