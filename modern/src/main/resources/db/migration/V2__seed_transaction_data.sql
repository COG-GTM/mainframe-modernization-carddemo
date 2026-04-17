-- Phase 5: Seed data for transaction service
-- Sample data derived from CardDemo mainframe application test datasets

-- Transaction types (from TRANTYPE VSAM file)
INSERT INTO transaction_type (type_code, description) VALUES
('01', 'Purchase'),
('02', 'Payment'),
('03', 'Cash Advance'),
('04', 'Balance Transfer'),
('05', 'Fee'),
('06', 'Interest Charge'),
('07', 'Credit Adjustment'),
('08', 'Debit Adjustment');

-- Transaction categories (from TRANCATG VSAM file)
INSERT INTO transaction_category (type_code, category_code, description) VALUES
('01', 5001, 'Retail Purchase'),
('01', 5002, 'Online Purchase'),
('01', 5003, 'Recurring Purchase'),
('02', 2001, 'Online Payment'),
('02', 2002, 'Phone Payment'),
('02', 2003, 'Auto Payment'),
('03', 3001, 'ATM Cash Advance'),
('03', 3002, 'Counter Cash Advance'),
('04', 4001, 'Promotional Balance Transfer'),
('04', 4002, 'Standard Balance Transfer'),
('05', 5501, 'Annual Fee'),
('05', 5502, 'Late Payment Fee'),
('05', 5503, 'Over Limit Fee'),
('06', 6001, 'Purchase Interest'),
('06', 6002, 'Cash Advance Interest'),
('07', 7001, 'Merchant Credit'),
('07', 7002, 'Dispute Credit'),
('08', 8001, 'Returned Payment');

-- Sample accounts (from ACCTDAT VSAM file)
INSERT INTO account (account_id, active_status, current_balance, credit_limit,
    cash_credit_limit, open_date, expiration_date, reissue_date,
    current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES
('00000000001', 'Y', 1500.00, 5000.00, 1000.00, '2020-01-15', '2025-01-15', '2024-01-15', 200.00, 350.00, '10001', 'GOLD'),
('00000000002', 'Y', 3200.50, 10000.00, 2500.00, '2019-06-01', '2024-06-01', '2023-06-01', 500.00, 800.00, '90210', 'PLATINUM'),
('00000000003', 'Y', 750.25, 3000.00, 500.00, '2021-03-20', '2026-03-20', '2025-03-20', 100.00, 150.00, '60601', 'STANDARD'),
('00000000004', 'N', 0.00, 2000.00, 400.00, '2018-11-10', '2023-11-10', '2022-11-10', 0.00, 0.00, '30301', 'STANDARD'),
('00000000005', 'Y', 4500.75, 15000.00, 5000.00, '2017-08-05', '2027-08-05', '2022-08-05', 1000.00, 1200.00, '20001', 'PLATINUM');

-- Card cross-references (from CARDXREF VSAM file)
INSERT INTO card_xref (card_number, customer_id, account_id) VALUES
('4111111111111111', '000000001', '00000000001'),
('4222222222222222', '000000002', '00000000002'),
('4333333333333333', '000000003', '00000000003'),
('4444444444444444', '000000004', '00000000004'),
('4555555555555555', '000000005', '00000000005');

-- Category balances (from TCATBALF VSAM file)
INSERT INTO category_balance (account_id, type_code, category_code, balance) VALUES
('00000000001', '01', 5001, 800.00),
('00000000001', '01', 5002, 350.00),
('00000000001', '02', 2001, -200.00),
('00000000002', '01', 5001, 1500.00),
('00000000002', '01', 5003, 900.50),
('00000000002', '03', 3001, 500.00),
('00000000003', '01', 5001, 600.25),
('00000000003', '02', 2001, -100.00),
('00000000005', '01', 5001, 2000.00),
('00000000005', '04', 4001, 1500.75);

-- Disclosure groups for interest calculation (from DISCGRP VSAM file)
INSERT INTO disclosure_group (account_group_id, transaction_type_code, transaction_category_code, interest_rate) VALUES
('STANDARD', '01', 5001, 19.99),
('STANDARD', '01', 5002, 19.99),
('STANDARD', '01', 5003, 19.99),
('STANDARD', '03', 3001, 24.99),
('STANDARD', '03', 3002, 24.99),
('GOLD', '01', 5001, 16.99),
('GOLD', '01', 5002, 16.99),
('GOLD', '01', 5003, 16.99),
('GOLD', '03', 3001, 22.99),
('PLATINUM', '01', 5001, 14.99),
('PLATINUM', '01', 5002, 14.99),
('PLATINUM', '01', 5003, 14.99),
('PLATINUM', '03', 3001, 19.99);

-- Sample transactions (from TRANSACT VSAM file)
INSERT INTO transaction (transaction_id, type_code, category_code, source, description,
    amount, merchant_id, merchant_name, merchant_city, merchant_zip,
    card_number, origin_timestamp, processed_timestamp)
VALUES
('0000000000000001', '01', 5001, 'POS TERM', 'GROCERY STORE PURCHASE', 45.67, 100000001, 'WHOLE FOODS MARKET', 'NEW YORK', '10001', '4111111111111111', '2024-01-15 10:30:00', '2024-01-15 10:30:05'),
('0000000000000002', '01', 5002, 'ONLINE', 'ONLINE BOOK ORDER', 29.99, 100000002, 'AMAZON.COM', 'SEATTLE', '98101', '4111111111111111', '2024-01-16 14:22:00', '2024-01-16 14:22:10'),
('0000000000000003', '02', 2001, 'ONLINE', 'BILL PAYMENT - ONLINE', 200.00, 999999999, 'BILL PAYMENT', 'N/A', 'N/A', '4111111111111111', '2024-01-20 09:00:00', '2024-01-20 09:00:05'),
('0000000000000004', '01', 5001, 'POS TERM', 'RESTAURANT DINNER', 85.50, 100000003, 'OLIVE GARDEN', 'LOS ANGELES', '90210', '4222222222222222', '2024-01-17 19:45:00', '2024-01-17 19:45:08'),
('0000000000000005', '03', 3001, 'ATM', 'ATM CASH WITHDRAWAL', 500.00, 100000004, 'CHASE ATM', 'LOS ANGELES', '90210', '4222222222222222', '2024-01-18 11:00:00', '2024-01-18 11:00:03'),
('0000000000000006', '01', 5001, 'POS TERM', 'GAS STATION FUEL', 55.25, 100000005, 'SHELL GAS STATION', 'CHICAGO', '60601', '4333333333333333', '2024-01-19 08:15:00', '2024-01-19 08:15:06'),
('0000000000000007', '01', 5003, 'ONLINE', 'STREAMING SUBSCRIPTION', 15.99, 100000006, 'NETFLIX', 'LOS GATOS', '95032', '4222222222222222', '2024-01-01 00:00:00', '2024-01-01 00:00:01'),
('0000000000000008', '05', 5501, 'SYSTEM', 'ANNUAL FEE', 95.00, 0, 'CARD ISSUER', 'N/A', 'N/A', '4555555555555555', '2024-01-01 00:00:00', '2024-01-01 00:00:01'),
('0000000000000009', '04', 4001, 'ONLINE', 'PROMOTIONAL BALANCE TRANSFER', 1500.75, 100000007, 'BALANCE TRANSFER', 'N/A', 'N/A', '4555555555555555', '2024-01-10 12:00:00', '2024-01-10 12:00:05'),
('0000000000000010', '01', 5001, 'POS TERM', 'ELECTRONICS PURCHASE', 299.99, 100000008, 'BEST BUY', 'WASHINGTON', '20001', '4555555555555555', '2024-01-22 16:30:00', '2024-01-22 16:30:10');
