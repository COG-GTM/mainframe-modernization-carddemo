-- Flyway migration: Create transaction_categories table
-- Migrated from COBOL copybook CVTRA04Y.cpy (TRAN-CAT-RECORD, RECLN = 60)
-- Original VSAM file: TRANCATG (Key-Sequenced Data Set)

CREATE TABLE transaction_categories (
    type_code     VARCHAR(2)  NOT NULL,
    category_code INTEGER     NOT NULL,
    description   VARCHAR(50) NOT NULL,
    PRIMARY KEY (type_code, category_code)
);
