-- V2: Seed sample transaction data
-- Sample data representative of COBOL TRANSACT.VSAM.KSDS records
INSERT INTO transactions (tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc, tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city, tran_merchant_zip, tran_card_num, tran_orig_ts, tran_proc_ts) VALUES
('0000000000000001', '01', 5001, 'ONLINE', 'Grocery Store Purchase', 125.50, '100000001', 'FreshMart Groceries', 'New York', '10001', '4111111111111111', '2024-01-15-10.30.00.000000', '2024-01-15-10.30.05.000000'),
('0000000000000002', '02', 5002, 'POS', 'Gas Station Fill Up', 45.00, '100000002', 'QuickFuel Station', 'Los Angeles', '90001', '4111111111111111', '2024-01-16-14.20.00.000000', '2024-01-16-14.20.03.000000'),
('0000000000000003', '01', 5003, 'ONLINE', 'Electronics Purchase', 899.99, '100000003', 'TechWorld Electronics', 'Chicago', '60601', '4222222222222222', '2024-01-17-09.15.00.000000', '2024-01-17-09.15.02.000000'),
('0000000000000004', '03', 5004, 'POS', 'Restaurant Dinner', 67.80, '100000004', 'Golden Dragon', 'San Francisco', '94102', '4222222222222222', '2024-01-18-19.45.00.000000', '2024-01-18-19.45.01.000000'),
('0000000000000005', '01', 5001, 'ONLINE', 'Monthly Subscription', 14.99, '100000005', 'StreamPlus Media', 'Seattle', '98101', '4333333333333333', '2024-01-19-00.00.00.000000', '2024-01-19-00.00.01.000000');
