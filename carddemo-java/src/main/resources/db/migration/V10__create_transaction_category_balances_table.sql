-- Flyway migration: Create transaction_category_balances table
-- Source: COBOL copybook CVTRA01Y.cpy (TRAN-CAT-BAL-RECORD, 50 bytes)
-- Seed data: tcatbal.txt

CREATE TABLE transaction_category_balances (
    trancat_acct_id      BIGINT       NOT NULL,               -- PIC 9(11)
    trancat_type_cd      VARCHAR(2)   NOT NULL,               -- PIC X(02)
    trancat_cd           INTEGER      NOT NULL,               -- PIC 9(04)
    tran_cat_bal         DECIMAL(11,2),                       -- PIC S9(09)V99
    PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
);
