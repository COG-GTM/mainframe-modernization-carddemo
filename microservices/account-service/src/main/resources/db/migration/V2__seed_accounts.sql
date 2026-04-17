-- V2: Seed sample accounts
-- Data modeled after CardDemo sample ACCTDATA file (app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS)

INSERT INTO accounts (acct_id, acct_active_status, acct_curr_bal, acct_credit_limit,
    acct_cash_credit_limit, acct_open_date, acct_expiration_date, acct_reissue_date,
    acct_curr_cyc_credit, acct_curr_cyc_debit, acct_addr_zip, acct_group_id)
VALUES
    ('00000000001', 'Y', 1500.00, 5000.00, 1500.00, '2020-01-15', '2025-01-15', '2023-01-15', 200.00, 350.00, '60601', 'GROUP01'),
    ('00000000002', 'Y', 3200.50, 10000.00, 3000.00, '2019-06-20', '2024-06-20', '2022-06-20', 500.00, 1200.00, '10001', 'GROUP02'),
    ('00000000003', 'N', 0.00, 7500.00, 2500.00, '2021-03-10', '2026-03-10', '2024-03-10', 0.00, 0.00, '90210', 'GROUP01');
