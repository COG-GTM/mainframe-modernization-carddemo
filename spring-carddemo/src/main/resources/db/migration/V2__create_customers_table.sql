-- Customers table derived from CVCUS01Y.cpy (CUSTOMER-RECORD)
CREATE TABLE customers (
    customer_id         BIGINT       NOT NULL,
    first_name          VARCHAR(25),
    middle_name         VARCHAR(25),
    last_name           VARCHAR(25),
    address_line_1      VARCHAR(50),
    address_line_2      VARCHAR(50),
    address_line_3      VARCHAR(50),
    state_code          VARCHAR(2),
    country_code        VARCHAR(3),
    zip_code            VARCHAR(10),
    phone_number_1      VARCHAR(15),
    phone_number_2      VARCHAR(15),
    ssn                 VARCHAR(9),
    govt_issued_id      VARCHAR(20),
    date_of_birth       VARCHAR(10),
    eft_account_id      VARCHAR(10),
    pri_card_holder_ind VARCHAR(1),
    fico_score          INTEGER,
    CONSTRAINT pk_customers PRIMARY KEY (customer_id)
);
