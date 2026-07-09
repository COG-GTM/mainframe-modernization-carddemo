-- CS-1 data model: relational schema backing the CardDemo VSAM data copybooks.
--
-- PostgreSQL-compatible DDL (also runs on H2 in PostgreSQL mode for dev/test). Column
-- lengths mirror the COBOL copybook PIC clauses. Monetary/interest columns are NUMERIC
-- with the copybook's implied decimal scale (V99 -> scale 2). Fixed-width, zero-padded
-- numeric identifiers (account/card/customer/merchant ids, etc.) are stored as CHAR/VARCHAR
-- to preserve formatting and avoid accidental arithmetic.

-- CVCUS01Y -> CUSTOMER-RECORD (RECLN 500)
CREATE TABLE customer (
    cust_id                   VARCHAR(9)   NOT NULL,
    cust_first_name           VARCHAR(25),
    cust_middle_name          VARCHAR(25),
    cust_last_name            VARCHAR(25),
    cust_addr_line_1          VARCHAR(50),
    cust_addr_line_2          VARCHAR(50),
    cust_addr_line_3          VARCHAR(50),
    cust_addr_state_cd        VARCHAR(2),
    cust_addr_country_cd      VARCHAR(3),
    cust_addr_zip             VARCHAR(10),
    cust_phone_num_1          VARCHAR(15),
    cust_phone_num_2          VARCHAR(15),
    cust_ssn                  VARCHAR(9),
    cust_govt_issued_id       VARCHAR(20),
    cust_dob_yyyy_mm_dd       VARCHAR(10),
    cust_eft_account_id       VARCHAR(10),
    cust_pri_card_holder_ind  VARCHAR(1),
    cust_fico_credit_score    INTEGER,
    CONSTRAINT pk_customer PRIMARY KEY (cust_id)
);

-- CVACT01Y -> ACCOUNT-RECORD (RECLN 300)
CREATE TABLE account (
    acct_id                   VARCHAR(11)  NOT NULL,
    acct_active_status        VARCHAR(1),
    acct_curr_bal             NUMERIC(12,2),
    acct_credit_limit         NUMERIC(12,2),
    acct_cash_credit_limit    NUMERIC(12,2),
    acct_open_date            VARCHAR(10),
    acct_expiration_date      VARCHAR(10),
    acct_reissue_date         VARCHAR(10),
    acct_curr_cyc_credit      NUMERIC(12,2),
    acct_curr_cyc_debit       NUMERIC(12,2),
    acct_addr_zip             VARCHAR(10),
    acct_group_id             VARCHAR(10),
    CONSTRAINT pk_account PRIMARY KEY (acct_id)
);

-- CVACT02Y -> CARD-RECORD (RECLN 150)
CREATE TABLE card (
    card_num                  VARCHAR(16)  NOT NULL,
    card_acct_id              VARCHAR(11),
    card_cvv_cd               VARCHAR(3),
    card_embossed_name        VARCHAR(50),
    card_expiration_date      VARCHAR(10),
    card_active_status        VARCHAR(1),
    CONSTRAINT pk_card PRIMARY KEY (card_num),
    CONSTRAINT fk_card_account FOREIGN KEY (card_acct_id) REFERENCES account (acct_id)
);
CREATE INDEX ix_card_acct_id ON card (card_acct_id);

-- CVACT03Y -> CARD-XREF-RECORD (RECLN 50): customer<->account<->card cross reference
CREATE TABLE card_xref (
    xref_card_num             VARCHAR(16)  NOT NULL,
    xref_cust_id              VARCHAR(9),
    xref_acct_id              VARCHAR(11),
    CONSTRAINT pk_card_xref PRIMARY KEY (xref_card_num),
    CONSTRAINT fk_xref_card FOREIGN KEY (xref_card_num) REFERENCES card (card_num),
    CONSTRAINT fk_xref_account FOREIGN KEY (xref_acct_id) REFERENCES account (acct_id),
    CONSTRAINT fk_xref_customer FOREIGN KEY (xref_cust_id) REFERENCES customer (cust_id)
);
CREATE INDEX ix_xref_acct_id ON card_xref (xref_acct_id);
CREATE INDEX ix_xref_cust_id ON card_xref (xref_cust_id);

-- CVTRA03Y -> TRAN-TYPE-RECORD (RECLN 60)
CREATE TABLE transaction_type (
    tran_type                 VARCHAR(2)   NOT NULL,
    tran_type_desc            VARCHAR(50),
    CONSTRAINT pk_transaction_type PRIMARY KEY (tran_type)
);

-- CVTRA04Y -> TRAN-CAT-RECORD (RECLN 60)
CREATE TABLE transaction_category (
    tran_type_cd              VARCHAR(2)   NOT NULL,
    tran_cat_cd               INTEGER      NOT NULL,
    tran_cat_type_desc        VARCHAR(50),
    CONSTRAINT pk_transaction_category PRIMARY KEY (tran_type_cd, tran_cat_cd)
);

-- CVTRA02Y -> DIS-GROUP-RECORD (RECLN 50): interest rate per group/type/category
CREATE TABLE disclosure_group (
    dis_acct_group_id         VARCHAR(10)  NOT NULL,
    dis_tran_type_cd          VARCHAR(2)   NOT NULL,
    dis_tran_cat_cd           INTEGER      NOT NULL,
    dis_int_rate              NUMERIC(6,2),
    CONSTRAINT pk_disclosure_group PRIMARY KEY (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd)
);

-- CVTRA01Y -> TRAN-CAT-BAL-RECORD (RECLN 50)
CREATE TABLE tran_cat_balance (
    trancat_acct_id           VARCHAR(11)  NOT NULL,
    trancat_type_cd           VARCHAR(2)   NOT NULL,
    trancat_cd                INTEGER      NOT NULL,
    tran_cat_bal              NUMERIC(11,2),
    CONSTRAINT pk_tran_cat_balance PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
);

-- CVTRA05Y -> TRAN-RECORD (RECLN 350): online transactions (no seed; posted at runtime)
CREATE TABLE card_transaction (
    tran_id                   VARCHAR(16)  NOT NULL,
    tran_type_cd              VARCHAR(2),
    tran_cat_cd               INTEGER,
    tran_source               VARCHAR(10),
    tran_desc                 VARCHAR(100),
    tran_amt                  NUMERIC(11,2),
    tran_merchant_id          VARCHAR(9),
    tran_merchant_name        VARCHAR(50),
    tran_merchant_city        VARCHAR(50),
    tran_merchant_zip         VARCHAR(10),
    tran_card_num             VARCHAR(16),
    tran_orig_ts              VARCHAR(26),
    tran_proc_ts              VARCHAR(26),
    CONSTRAINT pk_card_transaction PRIMARY KEY (tran_id)
);

-- CVTRA06Y -> DALYTRAN-RECORD (RECLN 350): daily transaction input
CREATE TABLE daily_transaction (
    dalytran_id               VARCHAR(16)  NOT NULL,
    tran_type_cd              VARCHAR(2),
    tran_cat_cd               INTEGER,
    tran_source               VARCHAR(10),
    tran_desc                 VARCHAR(100),
    tran_amt                  NUMERIC(11,2),
    tran_merchant_id          VARCHAR(9),
    tran_merchant_name        VARCHAR(50),
    tran_merchant_city        VARCHAR(50),
    tran_merchant_zip         VARCHAR(10),
    tran_card_num             VARCHAR(16),
    tran_orig_ts              VARCHAR(26),
    tran_proc_ts              VARCHAR(26),
    CONSTRAINT pk_daily_transaction PRIMARY KEY (dalytran_id)
);

-- CSUSR01Y -> SEC-USER-DATA (RECLN 80): security users (seed from DUSRSECJ.jcl inline data)
CREATE TABLE sec_user (
    sec_usr_id                VARCHAR(8)   NOT NULL,
    sec_usr_fname             VARCHAR(20),
    sec_usr_lname             VARCHAR(20),
    sec_usr_pwd               VARCHAR(8),
    sec_usr_type              VARCHAR(1),
    CONSTRAINT pk_sec_user PRIMARY KEY (sec_usr_id)
);
