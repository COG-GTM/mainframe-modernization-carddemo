-- V1: Create transactions table
-- Mapped from COBOL TRAN-RECORD (CVTRA05Y.cpy) / VSAM TRANSACT.VSAM.KSDS
CREATE TABLE transactions (
    tran_id           VARCHAR(16)    PRIMARY KEY,
    tran_type_cd      VARCHAR(2),
    tran_cat_cd       INTEGER,
    tran_source       VARCHAR(10),
    tran_desc         VARCHAR(100),
    tran_amt          DECIMAL(11,2),
    tran_merchant_id  VARCHAR(9),
    tran_merchant_name VARCHAR(50),
    tran_merchant_city VARCHAR(50),
    tran_merchant_zip VARCHAR(10),
    tran_card_num     VARCHAR(16)    NOT NULL,
    tran_orig_ts      VARCHAR(26),
    tran_proc_ts      VARCHAR(26)
);

-- Index on card number for efficient lookups
-- Translates VSAM Alternate Index (AIX) on TRANSACT by CARD-NUM
CREATE INDEX idx_tran_card_num ON transactions (tran_card_num);
