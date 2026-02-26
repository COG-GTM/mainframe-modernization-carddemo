-- ============================================================================
-- Seed data for development and testing
-- Sample data based on CardDemo application patterns
-- ============================================================================

-- Sample accounts (from ACCTDAT VSAM file)
INSERT INTO accounts (acct_id, acct_active_status, acct_curr_bal, acct_credit_limit,
    acct_cash_credit_limit, acct_open_date, acct_expiration_date, acct_reissue_date,
    acct_curr_cyc_credit, acct_curr_cyc_debit, acct_addr_zip, acct_group_id)
VALUES
    (12345678901, 'Y', 1500.00, 10000.00, 2000.00, '2020-01-15', '2025-01-15', '2023-01-15', 500.00, 200.00, '10001', 'GRP001'),
    (12345678902, 'Y', 2500.00, 15000.00, 3000.00, '2019-06-20', '2024-06-20', '2022-06-20', 800.00, 350.00, '90210', 'GRP002');

-- Sample card cross-references (from CCXREF VSAM file)
INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id)
VALUES
    ('4111111111111111', 100000001, 12345678901),
    ('4222222222222222', 100000002, 12345678902);

-- Sample transactions (from TRANSACT VSAM file)
INSERT INTO transactions (tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc,
    tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city, tran_merchant_zip,
    tran_card_num, tran_orig_ts, tran_proc_ts)
VALUES
    ('0000000000000001', '01', 5001, 'ONLINE', 'Online purchase - Electronics', -250.99, 123456789, 'Best Electronics', 'Seattle', '98101', '4111111111111111', '2024-01-15', '2024-01-15'),
    ('0000000000000002', '01', 5002, 'POS', 'Restaurant dining', -85.50, 987654321, 'Fine Dining Place', 'New York', '10001', '4111111111111111', '2024-01-16', '2024-01-16'),
    ('0000000000000003', '02', 6001, 'ONLINE', 'Payment received', 500.00, 111222333, 'Bank Transfer', 'Chicago', '60601', '4222222222222222', '2024-01-17', '2024-01-17');
