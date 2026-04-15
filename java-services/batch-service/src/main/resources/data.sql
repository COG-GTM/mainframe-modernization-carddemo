-- Sample data for CardDemo batch-service
-- Mirrors COBOL VSAM data files from the mainframe application

-- Accounts (from ACCTDATA VSAM KSDS, copybook CVACT01Y)
INSERT INTO accounts (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit, addr_zip, group_id)
VALUES
(10000000001, 'Y', 5000.00, 15000.00, 5000.00, '2020-01-15', '2027-01-15', '2025-01-15', 500.00, -200.00, '60601', 'GROUP01'),
(10000000002, 'Y', 12500.50, 25000.00, 10000.00, '2019-06-01', '2026-06-01', '2024-06-01', 1200.00, -800.00, '10001', 'GROUP01'),
(10000000003, 'Y', 750.25, 5000.00, 2000.00, '2021-03-20', '2028-03-20', '2026-03-20', 100.00, -50.00, '90210', 'GROUP02'),
(10000000004, 'N', 0.00, 10000.00, 3000.00, '2018-11-10', '2023-11-10', '2021-11-10', 0.00, 0.00, '30301', 'GROUP01'),
(10000000005, 'Y', 22000.00, 50000.00, 15000.00, '2017-08-05', '2027-08-05', '2025-08-05', 3000.00, -1500.00, '94102', 'GROUP03');

-- Cards (from CARDDATA VSAM KSDS, copybook CVACT02Y)
INSERT INTO cards (card_num, acct_id, cvv_cd, embossed_name, expiration_date, active_status)
VALUES
('4111111111111111', 10000000001, 123, 'JOHN A SMITH', '2027-01-15', 'Y'),
('4222222222222222', 10000000002, 456, 'JANE B DOE', '2026-06-01', 'Y'),
('4333333333333333', 10000000003, 789, 'BOB C JONES', '2028-03-20', 'Y'),
('4444444444444444', 10000000004, 321, 'ALICE D BROWN', '2023-11-10', 'N'),
('4555555555555555', 10000000005, 654, 'CHARLIE E WHITE', '2027-08-05', 'Y');

-- Card cross-references (from CARDXREF VSAM KSDS, copybook CVACT03Y)
INSERT INTO card_xref (card_num, cust_id, acct_id)
VALUES
('4111111111111111', 100000001, 10000000001),
('4222222222222222', 100000002, 10000000002),
('4333333333333333', 100000003, 10000000003),
('4444444444444444', 100000004, 10000000004),
('4555555555555555', 100000005, 10000000005);

-- Customers (from CUSTDATA VSAM KSDS, copybook CVCUS01Y)
INSERT INTO customers (cust_id, first_name, middle_name, last_name, addr_line_1, addr_line_2,
    addr_line_3, addr_state_cd, addr_country_cd, addr_zip, phone_num_1, phone_num_2,
    ssn, govt_issued_id, dob, eft_account_id, pri_card_holder_ind, fico_credit_score)
VALUES
(100000001, 'JOHN', 'A', 'SMITH', '123 MAIN ST', 'APT 4B', '', 'IL', 'USA', '60601', '312-555-0101', '', 123456789, 'DL12345678', '1985-03-15', '1234567890', 'Y', 750),
(100000002, 'JANE', 'B', 'DOE', '456 OAK AVE', '', '', 'NY', 'USA', '10001', '212-555-0202', '', 234567890, 'DL23456789', '1990-07-22', '2345678901', 'Y', 810),
(100000003, 'BOB', 'C', 'JONES', '789 PINE RD', 'SUITE 100', '', 'CA', 'USA', '90210', '310-555-0303', '', 345678901, 'DL34567890', '1978-12-01', '3456789012', 'Y', 680),
(100000004, 'ALICE', 'D', 'BROWN', '321 ELM ST', '', '', 'GA', 'USA', '30301', '404-555-0404', '', 456789012, 'DL45678901', '1995-05-10', '4567890123', 'Y', 720),
(100000005, 'CHARLIE', 'E', 'WHITE', '654 BIRCH LN', '', '', 'CA', 'USA', '94102', '415-555-0505', '', 567890123, 'DL56789012', '1982-09-28', '5678901234', 'Y', 790);

-- Daily transactions (from DALYTRAN sequential file, copybook CVTRA06Y)
INSERT INTO daily_transactions (tran_id, type_cd, cat_cd, source, description, amount,
    merchant_id, merchant_name, merchant_city, merchant_zip, card_num, orig_ts, proc_ts, processed)
VALUES
('DT20260415000001', '02', 5001, 'POS', 'GROCERY STORE PURCHASE', -125.50, 900000001, 'WHOLE FOODS MARKET', 'CHICAGO', '60601', '4111111111111111', '2026-04-15-10.30.00.000000', '', false),
('DT20260415000002', '02', 5002, 'POS', 'GAS STATION FILL UP', -45.00, 900000002, 'SHELL OIL', 'NEW YORK', '10001', '4222222222222222', '2026-04-15-11.15.00.000000', '', false),
('DT20260415000003', '03', 5003, 'ONLINE', 'ONLINE ELECTRONICS PURCHASE', -899.99, 900000003, 'BEST BUY', 'LOS ANGELES', '90210', '4333333333333333', '2026-04-15-12.00.00.000000', '', false),
('DT20260415000004', '01', 5004, 'PAYMENT', 'ONLINE PAYMENT RECEIVED', 500.00, 0, '', '', '', '4111111111111111', '2026-04-15-13.00.00.000000', '', false),
('DT20260415000005', '02', 5001, 'POS', 'RESTAURANT DINNER', -78.25, 900000004, 'THE STEAKHOUSE', 'SAN FRANCISCO', '94102', '4555555555555555', '2026-04-15-14.00.00.000000', '', false);

-- Transaction category balances (from TCATBALF VSAM KSDS, copybook CVTRA01Y)
INSERT INTO tran_cat_bal (acct_id, type_cd, cat_cd, balance)
VALUES
(10000000001, '02', 5001, -1250.00),
(10000000001, '01', 5004, 2000.00),
(10000000002, '02', 5002, -500.00),
(10000000003, '03', 5003, -300.00),
(10000000005, '02', 5001, -800.00);
