-- Flyway migration: Create transaction_categories table
-- Source: COBOL copybook CVTRA04Y.cpy (TRAN-CAT-RECORD, 60 bytes)
-- Seed data: trancatg.txt

CREATE TABLE transaction_categories (
    tran_type_cd         VARCHAR(2)   NOT NULL,               -- PIC X(02)
    tran_cat_cd          INTEGER      NOT NULL,               -- PIC 9(04)
    tran_cat_type_desc   VARCHAR(50),                         -- PIC X(50)
    PRIMARY KEY (tran_type_cd, tran_cat_cd)
);
