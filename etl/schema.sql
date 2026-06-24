-- PostgreSQL schema for the CardDemo Transaction entity.
-- Generated from copybook CVTRA05Y (app/cpy/CVTRA05Y.cpy) via the
-- data-mapper skill. Target of the carddemo_etl pipeline.

CREATE TABLE IF NOT EXISTS transactions (
    tran_id             CHAR(16)        NOT NULL,            -- TRAN-ID            PIC X(16)
    tran_type_cd        CHAR(2)         NOT NULL,            -- TRAN-TYPE-CD       PIC X(02)
    tran_cat_cd         CHAR(4)         NOT NULL,            -- TRAN-CAT-CD        PIC 9(04)
    tran_source         VARCHAR(10),                         -- TRAN-SOURCE        PIC X(10)
    tran_desc           VARCHAR(100),                        -- TRAN-DESC          PIC X(100)
    tran_amt            NUMERIC(11, 2)  NOT NULL,            -- TRAN-AMT           PIC S9(09)V99 (overpunch sign)
    tran_merchant_id    BIGINT          NOT NULL,            -- TRAN-MERCHANT-ID   PIC 9(09)
    tran_merchant_name  VARCHAR(50),                         -- TRAN-MERCHANT-NAME PIC X(50)
    tran_merchant_city  VARCHAR(50),                         -- TRAN-MERCHANT-CITY PIC X(50)
    tran_merchant_zip   VARCHAR(10),                         -- TRAN-MERCHANT-ZIP  PIC X(10)
    tran_card_num       CHAR(16)        NOT NULL,            -- TRAN-CARD-NUM      PIC X(16) (PCI sensitive)
    tran_orig_ts        TIMESTAMP       NOT NULL,            -- TRAN-ORIG-TS       PIC X(26)
    tran_proc_ts        TIMESTAMP,                           -- TRAN-PROC-TS       PIC X(26) (nullable)
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT pk_transactions PRIMARY KEY (tran_id)
);

-- Access paths mirroring common COBOL STARTBR / READ patterns.
CREATE INDEX IF NOT EXISTS ix_transactions_card_num ON transactions (tran_card_num);
CREATE INDEX IF NOT EXISTS ix_transactions_merchant_id ON transactions (tran_merchant_id);
CREATE INDEX IF NOT EXISTS ix_transactions_orig_ts ON transactions (tran_orig_ts);

-- FK candidates (enable once the referenced tables exist):
--   tran_type_cd -> transaction_types (trantype / CVTRA03Y)
--   tran_cat_cd  -> transaction_categories (trancatg / CVTRA04Y)
--   tran_card_num -> cards (carddat / CVACT02Y)
