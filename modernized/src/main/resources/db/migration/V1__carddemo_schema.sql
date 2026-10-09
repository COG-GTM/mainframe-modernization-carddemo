-- CardDemo schema derived from the COBOL copybooks under app/cpy.
-- Column lengths and numeric precision mirror the original VSAM record layouts: COBOL display
-- integers (PIC 9(n)) become BIGINT/INTEGER with a CHECK that keeps the original digit width,
-- and PIC S9(n)V99 amounts become NUMERIC(n+2,2) so no precision is lost.

-- CSUSR01Y - user security record (80 bytes)
CREATE TABLE sec_user (
    sec_usr_id    VARCHAR(8)     NOT NULL,
    sec_usr_fname VARCHAR(20) NOT NULL,
    sec_usr_lname VARCHAR(20) NOT NULL,
    sec_usr_pwd   VARCHAR(8)  NOT NULL,
    sec_usr_type  VARCHAR(1)     NOT NULL,
    CONSTRAINT pk_sec_user PRIMARY KEY (sec_usr_id)
);

-- CVCUS01Y - customer record (500 bytes)
CREATE TABLE customer (
    cust_id                  BIGINT  NOT NULL,
    first_name               VARCHAR(25),
    middle_name              VARCHAR(25),
    last_name                VARCHAR(25),
    addr_line_1              VARCHAR(50),
    addr_line_2              VARCHAR(50),
    addr_line_3              VARCHAR(50),
    addr_state_cd            VARCHAR(2),
    addr_country_cd          VARCHAR(3),
    addr_zip                 VARCHAR(10),
    phone_num_1              VARCHAR(15),
    phone_num_2              VARCHAR(15),
    ssn                      BIGINT,
    govt_issued_id           VARCHAR(20),
    dob_yyyy_mm_dd           VARCHAR(10),
    eft_account_id           VARCHAR(10),
    pri_card_holder_ind      VARCHAR(1),
    fico_credit_score        INTEGER,
    CONSTRAINT pk_customer PRIMARY KEY (cust_id)
);

-- CVACT01Y - account record (300 bytes)
CREATE TABLE account (
    acct_id                  BIGINT   NOT NULL,
    acct_active_status       VARCHAR(1),
    acct_curr_bal            NUMERIC(12,2) NOT NULL DEFAULT 0,
    acct_credit_limit        NUMERIC(12,2) NOT NULL DEFAULT 0,
    acct_cash_credit_limit   NUMERIC(12,2) NOT NULL DEFAULT 0,
    acct_open_date           VARCHAR(10),
    acct_expiraion_date      VARCHAR(10),
    acct_reissue_date        VARCHAR(10),
    acct_curr_cyc_credit     NUMERIC(12,2) NOT NULL DEFAULT 0,
    acct_curr_cyc_debit      NUMERIC(12,2) NOT NULL DEFAULT 0,
    acct_addr_zip            VARCHAR(10),
    acct_group_id            VARCHAR(10),
    CONSTRAINT pk_account PRIMARY KEY (acct_id)
);

-- CVACT02Y - card record (150 bytes)
CREATE TABLE card (
    card_num            VARCHAR(16)    NOT NULL,
    card_acct_id        BIGINT NOT NULL,
    card_cvv_cd         INTEGER,
    card_embossed_name  VARCHAR(50),
    card_expiraion_date VARCHAR(10),
    card_active_status  VARCHAR(1),
    CONSTRAINT pk_card PRIMARY KEY (card_num),
    CONSTRAINT fk_card_account FOREIGN KEY (card_acct_id) REFERENCES account (acct_id)
);
CREATE INDEX ix_card_acct_id ON card (card_acct_id);

-- CVACT03Y - card/account/customer cross reference (50 bytes, AIX on account id)
CREATE TABLE card_xref (
    xref_card_num VARCHAR(16)    NOT NULL,
    xref_cust_id  BIGINT  NOT NULL,
    xref_acct_id  BIGINT NOT NULL,
    CONSTRAINT pk_card_xref PRIMARY KEY (xref_card_num),
    CONSTRAINT fk_xref_account FOREIGN KEY (xref_acct_id) REFERENCES account (acct_id),
    CONSTRAINT fk_xref_customer FOREIGN KEY (xref_cust_id) REFERENCES customer (cust_id)
);
CREATE INDEX ix_xref_acct_id ON card_xref (xref_acct_id);
CREATE INDEX ix_xref_cust_id ON card_xref (xref_cust_id);

-- CVTRA03Y - transaction type (60 bytes)
CREATE TABLE transaction_type (
    tran_type      VARCHAR(2)     NOT NULL,
    tran_type_desc VARCHAR(50),
    CONSTRAINT pk_transaction_type PRIMARY KEY (tran_type)
);

-- CVTRA04Y - transaction category type (60 bytes)
CREATE TABLE transaction_category_type (
    tran_type_cd       VARCHAR(2)    NOT NULL,
    tran_cat_cd        INTEGER NOT NULL,
    tran_cat_type_desc VARCHAR(50),
    CONSTRAINT pk_transaction_category_type PRIMARY KEY (tran_type_cd, tran_cat_cd)
);

-- CVTRA02Y - disclosure group / interest rates (50 bytes)
CREATE TABLE disclosure_group (
    dis_acct_group_id VARCHAR(10)  NOT NULL,
    dis_tran_type_cd  VARCHAR(2)      NOT NULL,
    dis_tran_cat_cd   INTEGER   NOT NULL,
    dis_int_rate      NUMERIC(6,2) NOT NULL DEFAULT 0,
    CONSTRAINT pk_disclosure_group PRIMARY KEY (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd)
);

-- CVTRA01Y - transaction category balance (50 bytes)
CREATE TABLE transaction_category_balance (
    trancat_acct_id BIGINT   NOT NULL,
    trancat_type_cd VARCHAR(2)       NOT NULL,
    trancat_cd      INTEGER    NOT NULL,
    tran_cat_bal    NUMERIC(11,2) NOT NULL DEFAULT 0,
    CONSTRAINT pk_transaction_category_balance PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
);

-- CVTRA05Y - posted transaction (350 bytes)
CREATE TABLE transaction (
    tran_id            VARCHAR(16)      NOT NULL,
    tran_type_cd       VARCHAR(2),
    tran_cat_cd        INTEGER,
    tran_source        VARCHAR(10),
    tran_desc          VARCHAR(100),
    tran_amt           NUMERIC(11,2) NOT NULL DEFAULT 0,
    tran_merchant_id   BIGINT,
    tran_merchant_name VARCHAR(50),
    tran_merchant_city VARCHAR(50),
    tran_merchant_zip  VARCHAR(10),
    tran_card_num      VARCHAR(16),
    tran_orig_ts       VARCHAR(26),
    tran_proc_ts       VARCHAR(26),
    CONSTRAINT pk_transaction PRIMARY KEY (tran_id)
);
CREATE INDEX ix_transaction_card_num ON transaction (tran_card_num);
CREATE INDEX ix_transaction_proc_ts ON transaction (tran_proc_ts);

-- CVTRA06Y - daily (unposted) transaction (350 bytes), input of the POSTTRAN job
CREATE TABLE daily_transaction (
    dalytran_id            VARCHAR(16)      NOT NULL,
    dalytran_type_cd       VARCHAR(2),
    dalytran_cat_cd        INTEGER,
    dalytran_source        VARCHAR(10),
    dalytran_desc          VARCHAR(100),
    dalytran_amt           NUMERIC(11,2) NOT NULL DEFAULT 0,
    dalytran_merchant_id   BIGINT,
    dalytran_merchant_name VARCHAR(50),
    dalytran_merchant_city VARCHAR(50),
    dalytran_merchant_zip  VARCHAR(10),
    dalytran_card_num      VARCHAR(16),
    dalytran_orig_ts       VARCHAR(26),
    dalytran_proc_ts       VARCHAR(26),
    CONSTRAINT pk_daily_transaction PRIMARY KEY (dalytran_id)
);

-- DALYREJS - rejected daily transactions written by CBTRN02C (350 byte record + 80 byte trailer)
CREATE TABLE daily_transaction_reject (
    id                  BIGINT GENERATED BY DEFAULT AS IDENTITY,
    dalytran_id         VARCHAR(16),
    dalytran_card_num   VARCHAR(16),
    dalytran_amt        NUMERIC(11,2),
    validation_trailer  VARCHAR(80),
    reason_code         INTEGER     NOT NULL,
    reason_desc         VARCHAR(76) NOT NULL,
    rejected_at         TIMESTAMP   NOT NULL,
    CONSTRAINT pk_daily_transaction_reject PRIMARY KEY (id)
);

-- Digit width checks matching the PIC 9(n) declarations in the copybooks.
ALTER TABLE customer
    ADD CONSTRAINT ck_customer_cust_id CHECK (cust_id BETWEEN 0 AND 999999999),
    ADD CONSTRAINT ck_customer_ssn CHECK (ssn IS NULL OR ssn BETWEEN 0 AND 999999999),
    ADD CONSTRAINT ck_customer_fico CHECK (fico_credit_score IS NULL OR fico_credit_score BETWEEN 0 AND 999);

ALTER TABLE account
    ADD CONSTRAINT ck_account_acct_id CHECK (acct_id BETWEEN 0 AND 99999999999);

ALTER TABLE card
    ADD CONSTRAINT ck_card_acct_id CHECK (card_acct_id BETWEEN 0 AND 99999999999),
    ADD CONSTRAINT ck_card_cvv CHECK (card_cvv_cd IS NULL OR card_cvv_cd BETWEEN 0 AND 999);

ALTER TABLE card_xref
    ADD CONSTRAINT ck_xref_cust_id CHECK (xref_cust_id BETWEEN 0 AND 999999999),
    ADD CONSTRAINT ck_xref_acct_id CHECK (xref_acct_id BETWEEN 0 AND 99999999999);

ALTER TABLE transaction_category_type
    ADD CONSTRAINT ck_tran_cat_type_cd CHECK (tran_cat_cd BETWEEN 0 AND 9999);

ALTER TABLE disclosure_group
    ADD CONSTRAINT ck_disclosure_cat_cd CHECK (dis_tran_cat_cd BETWEEN 0 AND 9999);

ALTER TABLE transaction_category_balance
    ADD CONSTRAINT ck_trancat_acct_id CHECK (trancat_acct_id BETWEEN 0 AND 99999999999),
    ADD CONSTRAINT ck_trancat_cd CHECK (trancat_cd BETWEEN 0 AND 9999);

ALTER TABLE transaction
    ADD CONSTRAINT ck_transaction_cat_cd CHECK (tran_cat_cd IS NULL OR tran_cat_cd BETWEEN 0 AND 9999),
    ADD CONSTRAINT ck_transaction_merchant_id CHECK (tran_merchant_id IS NULL OR tran_merchant_id BETWEEN 0 AND 999999999);

ALTER TABLE daily_transaction
    ADD CONSTRAINT ck_dalytran_cat_cd CHECK (dalytran_cat_cd IS NULL OR dalytran_cat_cd BETWEEN 0 AND 9999),
    ADD CONSTRAINT ck_dalytran_merchant_id CHECK (dalytran_merchant_id IS NULL OR dalytran_merchant_id BETWEEN 0 AND 999999999);
