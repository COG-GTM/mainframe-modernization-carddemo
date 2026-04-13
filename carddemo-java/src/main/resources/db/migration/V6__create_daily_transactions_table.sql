-- Flyway migration: Create daily_transactions table
-- Source: COBOL copybook CVTRA06Y.cpy (DALYTRAN-RECORD, 350 bytes)
-- Seed data: dailytran.txt (300 records)

CREATE TABLE daily_transactions (
    dalytran_id              VARCHAR(16)  NOT NULL PRIMARY KEY,   -- PIC X(16)
    dalytran_type_cd         VARCHAR(2),                          -- PIC X(02)
    dalytran_cat_cd          INTEGER,                             -- PIC 9(04)
    dalytran_source          VARCHAR(10),                         -- PIC X(10)
    dalytran_desc            VARCHAR(100),                        -- PIC X(100)
    dalytran_amt             DECIMAL(11,2),                       -- PIC S9(09)V99
    dalytran_merchant_id     BIGINT,                              -- PIC 9(09)
    dalytran_merchant_name   VARCHAR(50),                         -- PIC X(50)
    dalytran_merchant_city   VARCHAR(50),                         -- PIC X(50)
    dalytran_merchant_zip    VARCHAR(10),                         -- PIC X(10)
    dalytran_card_num        VARCHAR(16),                         -- PIC X(16)
    dalytran_orig_ts         VARCHAR(26),                         -- PIC X(26)
    dalytran_proc_ts         VARCHAR(26)                          -- PIC X(26)
);
