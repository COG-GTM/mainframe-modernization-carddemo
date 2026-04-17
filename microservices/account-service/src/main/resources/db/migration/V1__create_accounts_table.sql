-- V1: Create accounts table
-- Mapped from COBOL copybook CVACT01Y (ACCOUNT-RECORD, RECLN 300)
-- Original VSAM file: ACCTDAT (Key Sequenced Data Set, primary key ACCT-ID)

CREATE TABLE accounts (
    acct_id               VARCHAR(11)    PRIMARY KEY,
    acct_active_status    CHAR(1),
    acct_curr_bal         DECIMAL(12,2),
    acct_credit_limit     DECIMAL(12,2),
    acct_cash_credit_limit DECIMAL(12,2),
    acct_open_date        VARCHAR(10),
    acct_expiration_date  VARCHAR(10),
    acct_reissue_date     VARCHAR(10),
    acct_curr_cyc_credit  DECIMAL(12,2),
    acct_curr_cyc_debit   DECIMAL(12,2),
    acct_addr_zip         VARCHAR(10),
    acct_group_id         VARCHAR(10)
);
