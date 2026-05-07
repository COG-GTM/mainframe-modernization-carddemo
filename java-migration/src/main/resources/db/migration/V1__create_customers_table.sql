-- Flyway migration: create customers table
-- Mapped from COBOL copybook CVCUS01Y.cpy (CUSTOMER-RECORD, RECLN 500)

CREATE TABLE customers (
    id                           BIGINT       NOT NULL PRIMARY KEY,  -- CUST-ID PIC 9(09)
    first_name                   VARCHAR(25),                        -- CUST-FIRST-NAME PIC X(25)
    middle_name                  VARCHAR(25),                        -- CUST-MIDDLE-NAME PIC X(25)
    last_name                    VARCHAR(25),                        -- CUST-LAST-NAME PIC X(25)
    address_line1                VARCHAR(50),                        -- CUST-ADDR-LINE-1 PIC X(50)
    address_line2                VARCHAR(50),                        -- CUST-ADDR-LINE-2 PIC X(50)
    address_line3                VARCHAR(50),                        -- CUST-ADDR-LINE-3 PIC X(50)
    state_code                   VARCHAR(2),                         -- CUST-ADDR-STATE-CD PIC X(02)
    country_code                 VARCHAR(3),                         -- CUST-ADDR-COUNTRY-CD PIC X(03)
    zip_code                     VARCHAR(10),                        -- CUST-ADDR-ZIP PIC X(10)
    phone_number1                VARCHAR(15),                        -- CUST-PHONE-NUM-1 PIC X(15)
    phone_number2                VARCHAR(15),                        -- CUST-PHONE-NUM-2 PIC X(15)
    ssn                          BIGINT,                             -- CUST-SSN PIC 9(09)
    govt_issued_id               VARCHAR(20),                        -- CUST-GOVT-ISSUED-ID PIC X(20)
    date_of_birth                DATE,                               -- CUST-DOB-YYYY-MM-DD PIC X(10)
    eft_account_id               VARCHAR(10),                        -- CUST-EFT-ACCOUNT-ID PIC X(10)
    primary_card_holder_indicator VARCHAR(1),                        -- CUST-PRI-CARD-HOLDER-IND PIC X(01)
    fico_credit_score            INTEGER                             -- CUST-FICO-CREDIT-SCORE PIC 9(03)
);
