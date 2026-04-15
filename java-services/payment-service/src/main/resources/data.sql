-- Sample data for payment-service
-- Based on COBOL CardDemo sample data structures

-- Accounts (from CVACT01Y.cpy / ACCTDAT file)
INSERT INTO accounts (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit, addr_zip, group_id)
VALUES (12345678901, 'Y', 1500.00, 5000.00, 1000.00,
    '2020-01-15', '2025-12-31', '2024-01-15', 200.00, 350.00, '10001', 'GRP001');

INSERT INTO accounts (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit, addr_zip, group_id)
VALUES (12345678902, 'Y', 0.00, 3000.00, 500.00,
    '2019-06-20', '2024-06-20', '2023-06-20', 0.00, 0.00, '90210', 'GRP002');

INSERT INTO accounts (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit, addr_zip, group_id)
VALUES (12345678903, 'Y', 2750.50, 10000.00, 2000.00,
    '2021-03-10', '2026-03-10', '2025-03-10', 500.00, 750.50, '60601', 'GRP001');

-- Card Cross-References (from CVACT03Y.cpy / CXACAIX file)
INSERT INTO card_cross_references (xref_card_num, xref_cust_id, xref_acct_id)
VALUES ('4111111111111111', 100000001, 12345678901);

INSERT INTO card_cross_references (xref_card_num, xref_cust_id, xref_acct_id)
VALUES ('4222222222222222', 100000002, 12345678902);

INSERT INTO card_cross_references (xref_card_num, xref_cust_id, xref_acct_id)
VALUES ('4333333333333333', 100000003, 12345678903);

-- Transactions (from CVTRA05Y.cpy / TRANSACT file)
INSERT INTO transactions (tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc,
    tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city, tran_merchant_zip,
    tran_card_num, tran_orig_ts, tran_proc_ts)
VALUES ('0000000000000100', '01', 5001, 'POS TERM', 'GROCERY PURCHASE',
    45.99, 123456789, 'WHOLE FOODS', 'NEW YORK', '10001',
    '4111111111111111', '2024-01-15 10:30:00.000000', '2024-01-15 10:30:00.000000');

INSERT INTO transactions (tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc,
    tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city, tran_merchant_zip,
    tran_card_num, tran_orig_ts, tran_proc_ts)
VALUES ('0000000000000101', '01', 5002, 'ONLINE', 'ELECTRONICS PURCHASE',
    299.99, 987654321, 'BEST BUY', 'LOS ANGELES', '90001',
    '4333333333333333', '2024-01-16 14:15:00.000000', '2024-01-16 14:15:00.000000');
