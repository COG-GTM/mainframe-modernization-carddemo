-- Sample data for report-service (mirrors CardDemo mainframe data structures)

-- Customers (from CVCUS01Y.cpy CUSTOMER-RECORD layout)
INSERT INTO customers (cust_id, cust_first_name, cust_middle_name, cust_last_name,
    cust_addr_line_1, cust_addr_line_2, cust_addr_line_3,
    cust_addr_state_cd, cust_addr_country_cd, cust_addr_zip,
    cust_phone_num_1, cust_phone_num_2, cust_ssn, cust_govt_issued_id,
    cust_dob, cust_eft_account_id, cust_pri_card_holder_ind, cust_fico_credit_score)
VALUES
('000000001', 'John', 'A', 'Smith', '123 Main St', 'Apt 4B', 'Springfield',
 'IL', 'USA', '62704', '217-555-0101', '217-555-0102', '123456789', 'DL12345678',
 '1985-03-15', '0000000001', 'Y', 750),
('000000002', 'Jane', 'M', 'Doe', '456 Oak Ave', 'Suite 200', 'Chicago',
 'IL', 'USA', '60601', '312-555-0201', '312-555-0202', '987654321', 'DL87654321',
 '1990-07-22', '0000000002', 'Y', 680),
('000000003', 'Robert', 'B', 'Johnson', '789 Pine Rd', '', 'Seattle',
 'WA', 'USA', '98101', '206-555-0301', '', '456789123', 'DL45678912',
 '1978-11-08', '0000000003', 'Y', 720);

-- Accounts (from CVACT01Y.cpy ACCOUNT-RECORD layout)
INSERT INTO accounts (acct_id, acct_active_status, acct_curr_bal, acct_credit_limit,
    acct_cash_credit_limit, acct_open_date, acct_expiration_date, acct_reissue_date,
    acct_curr_cyc_credit, acct_curr_cyc_debit, acct_addr_zip, acct_group_id)
VALUES
('00000000001', 'Y', 1500.75, 10000.00, 2000.00, '2020-01-15', '2025-01-15', '2024-01-15',
 500.00, 2000.75, '62704', '000000001'),
('00000000002', 'Y', 3200.50, 15000.00, 3000.00, '2019-06-01', '2024-06-01', '2023-06-01',
 1200.00, 4400.50, '60601', '000000002'),
('00000000003', 'Y', 750.25, 8000.00, 1500.00, '2021-03-10', '2026-03-10', '2025-03-10',
 250.00, 1000.25, '98101', '000000003');

-- Transactions (from CVTRA05Y.cpy TRAN-RECORD layout)
-- Account 1 transactions
INSERT INTO transactions (tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc,
    tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city, tran_merchant_zip,
    tran_card_num, tran_orig_ts, tran_proc_ts, account_id)
VALUES
('0000000000000001', 'SA', 5001, 'ONLINE', 'Purchase at Amazon',
 125.50, 100000001, 'Amazon.com', 'Seattle', '98101',
 '4111111111111111', '2024-01-10 10:30:00', '2024-01-10 10:30:00', '00000000001'),
('0000000000000002', 'SA', 5002, 'POS', 'Grocery shopping at Whole Foods',
 89.75, 100000002, 'Whole Foods Market', 'Springfield', '62704',
 '4111111111111111', '2024-01-12 14:15:00', '2024-01-12 14:15:00', '00000000001'),
('0000000000000003', 'CR', 6001, 'ONLINE', 'Payment received - thank you',
 -500.00, 0, 'PAYMENT', 'Springfield', '62704',
 '4111111111111111', '2024-01-15 09:00:00', '2024-01-15 09:00:00', '00000000001'),
('0000000000000004', 'SA', 5003, 'POS', 'Restaurant dinner at Olive Garden',
 67.30, 100000003, 'Olive Garden', 'Springfield', '62704',
 '4111111111111111', '2024-01-18 19:45:00', '2024-01-18 19:45:00', '00000000001'),
('0000000000000005', 'SA', 5004, 'ONLINE', 'Electronics purchase at Best Buy',
 349.99, 100000004, 'Best Buy', 'Chicago', '60601',
 '4111111111111111', '2024-01-22 11:20:00', '2024-01-22 11:20:00', '00000000001'),

-- Account 2 transactions
('0000000000000006', 'SA', 5001, 'ONLINE', 'Subscription renewal Netflix',
 15.99, 100000005, 'Netflix', 'Los Gatos', '95032',
 '4222222222222222', '2024-01-05 00:01:00', '2024-01-05 00:01:00', '00000000002'),
('0000000000000007', 'SA', 5002, 'POS', 'Fuel purchase at Shell',
 55.40, 100000006, 'Shell Gas Station', 'Chicago', '60601',
 '4222222222222222', '2024-01-08 07:30:00', '2024-01-08 07:30:00', '00000000002'),
('0000000000000008', 'SA', 5005, 'POS', 'Clothing purchase at Nordstrom',
 210.00, 100000007, 'Nordstrom', 'Chicago', '60601',
 '4222222222222222', '2024-01-14 16:00:00', '2024-01-14 16:00:00', '00000000002'),
('0000000000000009', 'CR', 6001, 'ONLINE', 'Payment received - thank you',
 -1200.00, 0, 'PAYMENT', 'Chicago', '60601',
 '4222222222222222', '2024-01-20 09:00:00', '2024-01-20 09:00:00', '00000000002'),
('0000000000000010', 'SA', 5006, 'ONLINE', 'Travel booking at Expedia',
 890.00, 100000008, 'Expedia', 'Seattle', '98101',
 '4222222222222222', '2024-01-25 13:45:00', '2024-01-25 13:45:00', '00000000002'),

-- Account 3 transactions
('0000000000000011', 'SA', 5002, 'POS', 'Grocery shopping at Trader Joes',
 45.60, 100000009, 'Trader Joes', 'Seattle', '98101',
 '4333333333333333', '2024-01-03 10:00:00', '2024-01-03 10:00:00', '00000000003'),
('0000000000000012', 'SA', 5007, 'POS', 'Coffee at Starbucks',
 6.75, 100000010, 'Starbucks', 'Seattle', '98101',
 '4333333333333333', '2024-01-07 08:15:00', '2024-01-07 08:15:00', '00000000003'),
('0000000000000013', 'SA', 5003, 'POS', 'Lunch at Chipotle',
 12.50, 100000011, 'Chipotle', 'Seattle', '98101',
 '4333333333333333', '2024-01-11 12:30:00', '2024-01-11 12:30:00', '00000000003'),
('0000000000000014', 'CR', 6001, 'ONLINE', 'Payment received - thank you',
 -250.00, 0, 'PAYMENT', 'Seattle', '98101',
 '4333333333333333', '2024-01-16 09:00:00', '2024-01-16 09:00:00', '00000000003'),
('0000000000000015', 'SA', 5008, 'ONLINE', 'Book purchase at Barnes Noble',
 32.99, 100000012, 'Barnes and Noble', 'Seattle', '98101',
 '4333333333333333', '2024-01-28 15:30:00', '2024-01-28 15:30:00', '00000000003');
