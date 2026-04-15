-- Transaction Types (from COBOL TRANTYPE file - CVTRA03Y layout)
INSERT INTO transaction_types (type_code, description) VALUES ('01', 'Purchase');
INSERT INTO transaction_types (type_code, description) VALUES ('02', 'Return');
INSERT INTO transaction_types (type_code, description) VALUES ('03', 'Payment');
INSERT INTO transaction_types (type_code, description) VALUES ('04', 'Cash Advance');
INSERT INTO transaction_types (type_code, description) VALUES ('05', 'Balance Transfer');

-- Transaction Category Balances (from COBOL TCATBALF file - CVTRA01Y layout)
INSERT INTO transaction_category_balances (account_id, type_code, category_code, balance)
    VALUES (12345678901, '01', 5001, 1250.50);
INSERT INTO transaction_category_balances (account_id, type_code, category_code, balance)
    VALUES (12345678901, '02', 5002, 200.00);
INSERT INTO transaction_category_balances (account_id, type_code, category_code, balance)
    VALUES (98765432101, '01', 5001, 3400.75);

-- Sample Transactions (from COBOL TRANSACT VSAM KSDS file - CVTRA05Y layout)
INSERT INTO transactions (transaction_id, type_code, category_code, source, description, amount,
    merchant_id, merchant_name, merchant_city, merchant_zip, card_number,
    origin_timestamp, processed_timestamp)
VALUES ('0000000000000001', '01', 5001, 'ONLINE', 'Grocery Store Purchase',
    45.67, 123456789, 'Fresh Foods Market', 'New York', '10001',
    '4567890123456789', '2024-01-15-10.30.00.000000', '2024-01-15-10.30.05.000000');

INSERT INTO transactions (transaction_id, type_code, category_code, source, description, amount,
    merchant_id, merchant_name, merchant_city, merchant_zip, card_number,
    origin_timestamp, processed_timestamp)
VALUES ('0000000000000002', '01', 5001, 'POS', 'Gas Station Fill-up',
    52.30, 987654321, 'QuickFuel Station', 'Chicago', '60601',
    '4567890123456789', '2024-01-16-08.15.00.000000', '2024-01-16-08.15.03.000000');

INSERT INTO transactions (transaction_id, type_code, category_code, source, description, amount,
    merchant_id, merchant_name, merchant_city, merchant_zip, card_number,
    origin_timestamp, processed_timestamp)
VALUES ('0000000000000003', '02', 5002, 'ONLINE', 'Returned Electronics Item',
    199.99, 111222333, 'ElectroMart', 'San Francisco', '94102',
    '9876543210987654', '2024-01-17-14.45.00.000000', '2024-01-17-14.45.10.000000');

INSERT INTO transactions (transaction_id, type_code, category_code, source, description, amount,
    merchant_id, merchant_name, merchant_city, merchant_zip, card_number,
    origin_timestamp, processed_timestamp)
VALUES ('0000000000000004', '03', 5001, 'ONLINE', 'Monthly Credit Card Payment',
    500.00, 0, 'CardDemo Bank', 'Dallas', '75201',
    '4567890123456789', '2024-02-01-09.00.00.000000', '2024-02-01-09.00.02.000000');

INSERT INTO transactions (transaction_id, type_code, category_code, source, description, amount,
    merchant_id, merchant_name, merchant_city, merchant_zip, card_number,
    origin_timestamp, processed_timestamp)
VALUES ('0000000000000005', '04', 5001, 'ATM', 'Cash Advance at ATM',
    200.00, 444555666, 'Downtown ATM', 'Miami', '33101',
    '9876543210987654', '2024-02-05-16.20.00.000000', '2024-02-05-16.20.08.000000');
