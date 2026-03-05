-- Seed data for development and testing
-- Derived from CardDemo application test defaults

-- Users: one admin, one regular user
INSERT INTO users (user_id, first_name, last_name, password, user_type)
VALUES ('ADMIN001', 'Admin', 'User', 'ADMIN123', 'A');

INSERT INTO users (user_id, first_name, last_name, password, user_type)
VALUES ('USER0001', 'John', 'Doe', 'USER1234', 'U');

-- Customers
INSERT INTO customers (customer_id, first_name, middle_name, last_name,
    address_line_1, address_line_2, address_line_3, state_code, country_code,
    zip_code, phone_number_1, phone_number_2, ssn, govt_issued_id,
    date_of_birth, eft_account_id, pri_card_holder_ind, fico_score)
VALUES (100000001, 'John', 'Michael', 'Smith',
    '123 Main Street', 'Apt 4B', '', 'NY', 'US',
    '10001', '212-555-0101', '212-555-0102', '123456789', 'DL12345678',
    '1985-03-15', '1234567890', 'Y', 750);

INSERT INTO customers (customer_id, first_name, middle_name, last_name,
    address_line_1, address_line_2, address_line_3, state_code, country_code,
    zip_code, phone_number_1, phone_number_2, ssn, govt_issued_id,
    date_of_birth, eft_account_id, pri_card_holder_ind, fico_score)
VALUES (100000002, 'Jane', 'Marie', 'Johnson',
    '456 Oak Avenue', '', '', 'CA', 'US',
    '90210', '310-555-0201', '310-555-0202', '987654321', 'DL87654321',
    '1990-07-22', '0987654321', 'Y', 680);

-- Accounts
INSERT INTO accounts (account_id, account_status, current_balance, credit_limit,
    cash_credit_limit, open_date, expiration_date, reissue_date,
    current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (12345678901, 'Y', 1500.00, 5000.00,
    1000.00, '2020-01-15', '2025-01-15', '2023-01-15',
    500.00, 200.00, '10001', 'GROUP001');

INSERT INTO accounts (account_id, account_status, current_balance, credit_limit,
    cash_credit_limit, open_date, expiration_date, reissue_date,
    current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (12345678902, 'Y', 2500.00, 10000.00,
    2000.00, '2019-06-01', '2024-06-01', '2022-06-01',
    1000.00, 750.00, '90210', 'GROUP002');

-- Card data
INSERT INTO card_data (card_number, account_id, cvv_code, embossed_name, expiration_date, card_status)
VALUES ('4111111111111111', 12345678901, 123, 'JOHN M SMITH', '2025-01-15', 'Y');

INSERT INTO card_data (card_number, account_id, cvv_code, embossed_name, expiration_date, card_status)
VALUES ('4222222222222222', 12345678902, 456, 'JANE M JOHNSON', '2024-06-01', 'Y');

-- Card cross-references
INSERT INTO card_xref (card_number, account_id, customer_id)
VALUES ('4111111111111111', 12345678901, 100000001);

INSERT INTO card_xref (card_number, account_id, customer_id)
VALUES ('4222222222222222', 12345678902, 100000002);

-- Transactions
INSERT INTO transactions (transaction_id, transaction_type, transaction_category,
    transaction_source, transaction_description, transaction_amount,
    merchant_id, merchant_name, merchant_city, merchant_zip,
    card_number, origin_timestamp, processing_timestamp)
VALUES ('TXN0000000000001', 'PU', 5411,
    'ONLINE', 'Grocery Store Purchase', 45.67,
    '123456789', 'Whole Foods Market', 'New York', '10001',
    '4111111111111111', '2024-01-15 10:30:00.000000', '2024-01-15 10:30:05.000000');

INSERT INTO transactions (transaction_id, transaction_type, transaction_category,
    transaction_source, transaction_description, transaction_amount,
    merchant_id, merchant_name, merchant_city, merchant_zip,
    card_number, origin_timestamp, processing_timestamp)
VALUES ('TXN0000000000002', 'PU', 5812,
    'POS', 'Restaurant Dining', 78.50,
    '987654321', 'The Italian Place', 'Los Angeles', '90210',
    '4222222222222222', '2024-01-16 19:15:00.000000', '2024-01-16 19:15:03.000000');

INSERT INTO transactions (transaction_id, transaction_type, transaction_category,
    transaction_source, transaction_description, transaction_amount,
    merchant_id, merchant_name, merchant_city, merchant_zip,
    card_number, origin_timestamp, processing_timestamp)
VALUES ('TXN0000000000003', 'CR', 5411,
    'ONLINE', 'Refund - Grocery Store', -15.00,
    '123456789', 'Whole Foods Market', 'New York', '10001',
    '4111111111111111', '2024-01-17 08:00:00.000000', '2024-01-17 08:00:02.000000');

-- Transaction types
INSERT INTO transaction_types (type_code, type_description) VALUES ('PU', 'Purchase');
INSERT INTO transaction_types (type_code, type_description) VALUES ('CR', 'Credit/Return');
INSERT INTO transaction_types (type_code, type_description) VALUES ('CA', 'Cash Advance');
INSERT INTO transaction_types (type_code, type_description) VALUES ('PM', 'Payment');
INSERT INTO transaction_types (type_code, type_description) VALUES ('BA', 'Balance Adjustment');

-- Daily transactions (batch staging)
INSERT INTO daily_transactions (transaction_id, transaction_type, transaction_category,
    transaction_source, transaction_description, transaction_amount,
    merchant_id, merchant_name, merchant_city, merchant_zip,
    card_number, origin_timestamp, processing_timestamp)
VALUES ('TXN0000000000004', 'PU', 5999,
    'POS', 'Electronics Purchase', 299.99,
    '111222333', 'Best Electronics', 'New York', '10002',
    '4111111111111111', '2024-01-18 14:00:00.000000', NULL);
