-- Accounts table derived from CVACT01Y.cpy (ACCOUNT-RECORD)
CREATE TABLE accounts (
    account_id           BIGINT         NOT NULL,
    account_status       VARCHAR(1),
    current_balance      DECIMAL(12,2),
    credit_limit         DECIMAL(12,2),
    cash_credit_limit    DECIMAL(12,2),
    open_date            VARCHAR(10),
    expiration_date      VARCHAR(10),
    reissue_date         VARCHAR(10),
    current_cycle_credit DECIMAL(12,2),
    current_cycle_debit  DECIMAL(12,2),
    address_zip          VARCHAR(10),
    group_id             VARCHAR(10),
    CONSTRAINT pk_accounts PRIMARY KEY (account_id)
);
