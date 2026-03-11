-- Flyway migration V1: Create the accounts table.
-- Mirrors the VSAM KSDS file ACCTDAT defined by copybook CVACT01Y.

CREATE TABLE accounts (
    acct_id           BIGINT        NOT NULL PRIMARY KEY,
    active_status     VARCHAR(1),
    curr_bal          DECIMAL(13,2),
    credit_limit      DECIMAL(13,2),
    cash_credit_limit DECIMAL(13,2),
    open_date         DATE,
    expiration_date   DATE,
    reissue_date      DATE,
    curr_cyc_credit   DECIMAL(13,2),
    curr_cyc_debit    DECIMAL(13,2),
    group_id          VARCHAR(10)
);
