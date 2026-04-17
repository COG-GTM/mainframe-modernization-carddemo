-- Flyway migration V4: Create customer table
-- Migrated from: CUSTDAT VSAM KSDS file (CVCUS01Y.cpy — Customer Master, RECLN 500)
-- Primary key: CUST-ID PIC 9(09)

CREATE TABLE customer (
    cust_id              BIGINT       NOT NULL PRIMARY KEY,
    first_name           VARCHAR(25),
    middle_name          VARCHAR(25),
    last_name            VARCHAR(25),
    addr_line_1          VARCHAR(50),
    addr_line_2          VARCHAR(50),
    addr_line_3          VARCHAR(50),
    addr_state_cd        VARCHAR(2),
    addr_country_cd      VARCHAR(3),
    addr_zip             VARCHAR(10),
    phone_num_1          VARCHAR(15),
    phone_num_2          VARCHAR(15),
    ssn                  BIGINT,
    govt_issued_id       VARCHAR(20),
    dob                  DATE,
    eft_account_id       VARCHAR(10),
    pri_card_holder_ind  VARCHAR(1),
    fico_credit_score    INTEGER
);

-- Seed data: sample customers matching CardDemo CUSTDAT sample data
INSERT INTO customer (cust_id, first_name, middle_name, last_name, addr_line_1, addr_line_2, addr_line_3, addr_state_cd, addr_country_cd, addr_zip, phone_num_1, phone_num_2, ssn, govt_issued_id, dob, eft_account_id, pri_card_holder_ind, fico_credit_score)
VALUES
    (100000001, 'John',    'M',  'Smith',   '123 Main Street',      'Apt 4B',    NULL,          'NY', 'USA', '10001',     '212-555-0101', '212-555-0102', 123456789, 'DL-NY-12345',   '1985-04-12', 'EFT000001', 'Y', 750),
    (100000002, 'Jane',    'A',  'Doe',     '456 Oak Avenue',       NULL,         NULL,          'CA', 'USA', '90210',     '310-555-0201', NULL,           234567890, 'DL-CA-23456',   '1990-08-25', 'EFT000002', 'Y', 680),
    (100000003, 'Robert',  'J',  'Johnson', '789 Pine Road',        'Suite 100',  'Building C',  'TX', 'USA', '75001',     '214-555-0301', '214-555-0302', 345678901, 'PP-US-34567',   '1978-12-01', 'EFT000003', 'Y', 720),
    (100000004, 'Maria',   'L',  'Garcia',  '321 Elm Boulevard',    NULL,         NULL,          'FL', 'USA', '33101',     '305-555-0401', NULL,           456789012, 'DL-FL-45678',   '1982-06-15', 'EFT000004', 'N', 695),
    (100000005, 'William', NULL, 'Chen',    '654 Birch Lane',       'Unit 7',     NULL,          'WA', 'USA', '98101',     '206-555-0501', '206-555-0502', 567890123, 'DL-WA-56789',   '1995-02-28', 'EFT000005', 'Y', 810);
