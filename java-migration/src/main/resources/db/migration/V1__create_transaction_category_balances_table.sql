-- Flyway migration: Create transaction_category_balances table
-- Migrated from COBOL copybook CVTRA01Y (TRAN-CAT-BAL-RECORD, RECLN 50)
-- VSAM KSDS file: MFE.CARDDEMO.TCATBALF.PS

CREATE TABLE transaction_category_balances (
    account_id     BIGINT        NOT NULL,
    type_code      VARCHAR(2)    NOT NULL,
    category_code  INTEGER       NOT NULL,
    balance        DECIMAL(11,2) NOT NULL DEFAULT 0.00,
    version        BIGINT        NOT NULL DEFAULT 0,

    PRIMARY KEY (account_id, type_code, category_code)
);
