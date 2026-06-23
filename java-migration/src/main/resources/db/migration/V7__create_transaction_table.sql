-- Schema for the Transaction entity, migrated from COBOL copybook CVTRA05Y.cpy
-- (VSAM TRANSACT KSDS, record length 350, key = TRAN-ID).
CREATE TABLE transaction (
    tran_id            VARCHAR(16)   NOT NULL,
    tran_type_cd       VARCHAR(2),
    tran_cat_cd        INTEGER,
    tran_source        VARCHAR(10),
    tran_desc          VARCHAR(100),
    tran_amt           NUMERIC(11, 2),
    tran_merchant_id   BIGINT,
    tran_merchant_name VARCHAR(50),
    tran_merchant_city VARCHAR(50),
    tran_merchant_zip  VARCHAR(10),
    tran_card_num      VARCHAR(16),
    tran_orig_ts       TIMESTAMP(6),
    tran_proc_ts       TIMESTAMP(6),
    CONSTRAINT pk_transaction PRIMARY KEY (tran_id)
);

CREATE INDEX idx_transaction_card_num ON transaction (tran_card_num);
CREATE INDEX idx_transaction_type_cd ON transaction (tran_type_cd);
