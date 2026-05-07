-- Flyway migration: Create accounts table
-- Mapped from COBOL copybook CVACT01Y.cpy (ACCOUNT-RECORD, RECLN 300)

CREATE TABLE accounts (
    id                   BIGINT       NOT NULL,
    active_status        VARCHAR(1)   NOT NULL,
    current_balance      DECIMAL(12,2) NOT NULL,
    credit_limit         DECIMAL(12,2) NOT NULL,
    cash_credit_limit    DECIMAL(12,2) NOT NULL,
    open_date            DATE         NOT NULL,
    expiration_date      DATE         NOT NULL,
    reissue_date         DATE         NOT NULL,
    current_cycle_credit DECIMAL(12,2) NOT NULL,
    current_cycle_debit  DECIMAL(12,2) NOT NULL,
    address_zip          VARCHAR(10),
    group_id             VARCHAR(10),
    PRIMARY KEY (id)
);
