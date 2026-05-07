-- Flyway migration: Create transactions table
-- Migrated from COBOL copybook CVTRA05Y.cpy (TRAN-RECORD, RECLN 350)

CREATE TABLE transactions (
    tran_id            VARCHAR(16)    NOT NULL PRIMARY KEY,
    tran_type_cd       VARCHAR(2),
    tran_cat_cd        INTEGER,
    tran_source        VARCHAR(10),
    tran_desc          VARCHAR(100),
    tran_amt           DECIMAL(11, 2),
    tran_merchant_id   BIGINT,
    tran_merchant_name VARCHAR(50),
    tran_merchant_city VARCHAR(50),
    tran_merchant_zip  VARCHAR(10),
    tran_card_num      VARCHAR(16),
    tran_orig_ts       VARCHAR(26),
    tran_proc_ts       VARCHAR(26)
);
