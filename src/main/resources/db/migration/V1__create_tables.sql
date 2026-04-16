-- Transaction records (maps to TRANFILE / CVTRA05Y.cpy TRAN-RECORD, 350 bytes)
CREATE TABLE IF NOT EXISTS transaction_record (
    tran_id            VARCHAR(16)    NOT NULL,
    tran_type_cd       VARCHAR(2)     NOT NULL,
    tran_cat_cd        INTEGER        NOT NULL,
    tran_source        VARCHAR(10),
    tran_desc          VARCHAR(100),
    tran_amt           NUMERIC(11,2)  NOT NULL,
    tran_merchant_id   NUMERIC(9),
    tran_merchant_name VARCHAR(50),
    tran_merchant_city VARCHAR(50),
    tran_merchant_zip  VARCHAR(10),
    tran_card_num      VARCHAR(16)    NOT NULL,
    tran_orig_ts       VARCHAR(26),
    tran_proc_ts       VARCHAR(26)    NOT NULL,
    PRIMARY KEY (tran_id)
);

-- Card cross-reference (maps to CARDXREF / CVACT03Y.cpy, 50 bytes)
CREATE TABLE IF NOT EXISTS card_xref (
    xref_card_num  VARCHAR(16)  NOT NULL,
    xref_cust_id   NUMERIC(9),
    xref_acct_id   VARCHAR(11)  NOT NULL,
    PRIMARY KEY (xref_card_num)
);

-- Transaction type reference (maps to TRANTYPE / CVTRA03Y.cpy, 60 bytes)
CREATE TABLE IF NOT EXISTS tran_type (
    tran_type      VARCHAR(2)   NOT NULL,
    tran_type_desc VARCHAR(50)  NOT NULL,
    PRIMARY KEY (tran_type)
);

-- Transaction category reference (maps to TRANCATG / CVTRA04Y.cpy, 60 bytes)
CREATE TABLE IF NOT EXISTS tran_category (
    tran_type_cd       VARCHAR(2)   NOT NULL,
    tran_cat_cd        INTEGER      NOT NULL,
    tran_cat_type_desc VARCHAR(50)  NOT NULL,
    PRIMARY KEY (tran_type_cd, tran_cat_cd)
);
