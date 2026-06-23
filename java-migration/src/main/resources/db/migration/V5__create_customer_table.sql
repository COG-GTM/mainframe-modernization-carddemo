-- Schema for the Customer entity, migrated from COBOL copybook CVCUS01Y.cpy
-- (VSAM CUSTDAT KSDS, record length 500, key = CUST-ID).
CREATE TABLE customer (
    cust_id                  BIGINT       NOT NULL,
    cust_first_name          VARCHAR(25),
    cust_middle_name         VARCHAR(25),
    cust_last_name           VARCHAR(25),
    cust_addr_line_1         VARCHAR(50),
    cust_addr_line_2         VARCHAR(50),
    cust_addr_line_3         VARCHAR(50),
    cust_addr_state_cd       VARCHAR(2),
    cust_addr_country_cd     VARCHAR(3),
    cust_addr_zip            VARCHAR(10),
    cust_phone_num_1         VARCHAR(15),
    cust_phone_num_2         VARCHAR(15),
    cust_ssn                 VARCHAR(9),
    cust_govt_issued_id      VARCHAR(20),
    cust_dob_yyyy_mm_dd      DATE,
    cust_eft_account_id      VARCHAR(10),
    cust_pri_card_holder_ind VARCHAR(1),
    cust_fico_credit_score   INTEGER,
    CONSTRAINT pk_customer PRIMARY KEY (cust_id)
);

CREATE INDEX idx_customer_last_name ON customer (cust_last_name);
CREATE INDEX idx_customer_fico_credit_score ON customer (cust_fico_credit_score);
