-- Account table matching CVACT01Y.cpy ACCT-RECORD (300-byte layout)
CREATE TABLE accounts (
    acct_id             NUMERIC(11,0)   NOT NULL PRIMARY KEY,
    acct_active_status  CHAR(1)         NOT NULL DEFAULT 'Y',
    acct_curr_bal       NUMERIC(12,2)   NOT NULL DEFAULT 0,
    acct_credit_limit   NUMERIC(12,2)   NOT NULL DEFAULT 0,
    acct_cash_credit_limit NUMERIC(12,2) NOT NULL DEFAULT 0,
    acct_open_date      VARCHAR(10),
    acct_expiration_date VARCHAR(10),
    acct_reissue_date   VARCHAR(10),
    acct_curr_cyc_credit NUMERIC(12,2)  NOT NULL DEFAULT 0,
    acct_curr_cyc_debit  NUMERIC(12,2)  NOT NULL DEFAULT 0,
    acct_addr_zip       VARCHAR(10),
    acct_group_id       VARCHAR(10),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- Customer table matching CVCUS01Y.cpy CUST-RECORD (500-byte layout)
CREATE TABLE customers (
    cust_id                 NUMERIC(9,0)    NOT NULL PRIMARY KEY,
    cust_first_name         VARCHAR(25),
    cust_middle_name        VARCHAR(25),
    cust_last_name          VARCHAR(25),
    cust_addr_line_1        VARCHAR(50),
    cust_addr_line_2        VARCHAR(50),
    cust_addr_line_3        VARCHAR(50),
    cust_addr_state_cd      CHAR(2),
    cust_addr_country_cd    CHAR(3),
    cust_addr_zip           VARCHAR(10),
    cust_phone_num_1        VARCHAR(15),
    cust_phone_num_2        VARCHAR(15),
    cust_ssn                NUMERIC(9,0),
    cust_govt_issued_id     VARCHAR(20),
    cust_dob_yyyy_mm_dd     VARCHAR(10),
    cust_eft_account_id     VARCHAR(10),
    cust_pri_card_holder_ind CHAR(1),
    cust_fico_credit_score  NUMERIC(3,0),
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Card cross-reference table matching CVACT03Y.cpy CARD-XREF-RECORD (50-byte layout)
CREATE TABLE card_xref (
    xref_card_num   VARCHAR(16)     NOT NULL PRIMARY KEY,
    xref_cust_id    NUMERIC(9,0)    NOT NULL,
    xref_acct_id    NUMERIC(11,0)   NOT NULL
);

CREATE INDEX idx_card_xref_acct_id ON card_xref(xref_acct_id);
CREATE INDEX idx_card_xref_cust_id ON card_xref(xref_cust_id);
