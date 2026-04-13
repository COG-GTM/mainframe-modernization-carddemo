-- Flyway migration: Create customers table
-- Source: COBOL copybook CVCUS01Y.cpy (CUSTOMER-RECORD, 500 bytes)
-- Seed data: custdata.txt (50 records)

CREATE TABLE customers (
    cust_id              BIGINT       NOT NULL PRIMARY KEY,   -- PIC 9(09)
    cust_first_name      VARCHAR(25),                         -- PIC X(25)
    cust_middle_name     VARCHAR(25),                         -- PIC X(25)
    cust_last_name       VARCHAR(25),                         -- PIC X(25)
    cust_addr_line_1     VARCHAR(50),                         -- PIC X(50)
    cust_addr_line_2     VARCHAR(50),                         -- PIC X(50)
    cust_addr_line_3     VARCHAR(50),                         -- PIC X(50)
    cust_addr_state_cd   VARCHAR(2),                          -- PIC X(02)
    cust_addr_country_cd VARCHAR(3),                          -- PIC X(03)
    cust_addr_zip        VARCHAR(10),                         -- PIC X(10)
    cust_phone_num_1     VARCHAR(15),                         -- PIC X(15)
    cust_phone_num_2     VARCHAR(15),                         -- PIC X(15)
    cust_ssn             BIGINT,                              -- PIC 9(09)
    cust_govt_issued_id  VARCHAR(20),                         -- PIC X(20)
    cust_dob             DATE,                                -- PIC X(10) YYYY-MM-DD
    cust_eft_account_id  VARCHAR(10),                         -- PIC X(10)
    cust_pri_card_holder_ind VARCHAR(1),                      -- PIC X(01)
    cust_fico_credit_score INTEGER                            -- PIC 9(03)
);
