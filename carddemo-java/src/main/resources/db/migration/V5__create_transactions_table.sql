-- Flyway migration: Create transactions table
-- Source: COBOL copybook CVTRA05Y.cpy (TRAN-RECORD, 350 bytes)

CREATE TABLE transactions (
    tran_id              VARCHAR(16)  NOT NULL PRIMARY KEY,   -- PIC X(16)
    tran_type_cd         VARCHAR(2),                          -- PIC X(02)
    tran_cat_cd          INTEGER,                             -- PIC 9(04)
    tran_source          VARCHAR(10),                         -- PIC X(10)
    tran_desc            VARCHAR(100),                        -- PIC X(100)
    tran_amt             DECIMAL(11,2),                       -- PIC S9(09)V99
    tran_merchant_id     BIGINT,                              -- PIC 9(09)
    tran_merchant_name   VARCHAR(50),                         -- PIC X(50)
    tran_merchant_city   VARCHAR(50),                         -- PIC X(50)
    tran_merchant_zip    VARCHAR(10),                         -- PIC X(10)
    tran_card_num        VARCHAR(16),                         -- PIC X(16)
    tran_orig_ts         VARCHAR(26),                         -- PIC X(26)
    tran_proc_ts         VARCHAR(26)                          -- PIC X(26)
);
