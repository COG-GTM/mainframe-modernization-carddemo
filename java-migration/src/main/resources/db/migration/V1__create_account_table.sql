-- Schema for the Account entity, migrated from COBOL copybook CVACT01Y.cpy
-- (VSAM ACCTDAT KSDS, record length 300, key = ACCT-ID).
CREATE TABLE account (
    acct_id                BIGINT         NOT NULL,
    acct_active_status     VARCHAR(1),
    acct_curr_bal          NUMERIC(12, 2),
    acct_credit_limit      NUMERIC(12, 2),
    acct_cash_credit_limit NUMERIC(12, 2),
    acct_open_date         DATE,
    acct_expiration_date   DATE,
    acct_reissue_date      DATE,
    acct_curr_cyc_credit   NUMERIC(12, 2),
    acct_curr_cyc_debit    NUMERIC(12, 2),
    acct_addr_zip          VARCHAR(10),
    acct_group_id          VARCHAR(10),
    CONSTRAINT pk_account PRIMARY KEY (acct_id)
);

CREATE INDEX idx_account_active_status ON account (acct_active_status);
CREATE INDEX idx_account_group_id ON account (acct_group_id);
