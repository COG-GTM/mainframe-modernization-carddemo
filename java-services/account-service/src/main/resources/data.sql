-- Sample account data migrated from COBOL VSAM file (ACCTDAT)
-- Record layout from CVACT01Y.cpy: ACCT-ID PIC 9(11), ACCT-ACTIVE-STATUS PIC X(01), etc.

INSERT INTO accounts (account_id, active_status, current_balance, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (10000000001, 'Y', 1500.00, 5000.00, 1500.00, '2020-03-15', '2026-03-15', '2024-03-15', 200.00, 150.00, '60601', 'GRP001');

INSERT INTO accounts (account_id, active_status, current_balance, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (10000000002, 'Y', 3200.50, 10000.00, 3000.00, '2019-07-01', '2025-07-01', '2023-07-01', 500.00, 300.00, '10001', 'GRP002');

INSERT INTO accounts (account_id, active_status, current_balance, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (10000000003, 'N', 0.00, 7500.00, 2500.00, '2021-01-10', '2027-01-10', NULL, 0.00, 0.00, '90210', 'GRP001');

INSERT INTO accounts (account_id, active_status, current_balance, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (10000000004, 'Y', 875.25, 3000.00, 1000.00, '2022-11-20', '2028-11-20', '2026-11-20', 100.00, 75.00, '30301', 'GRP003');

INSERT INTO accounts (account_id, active_status, current_balance, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (10000000005, 'Y', 4500.00, 15000.00, 5000.00, '2018-05-30', '2025-05-30', '2023-05-30', 1000.00, 800.00, '94102', 'GRP002');

-- Card cross-reference data migrated from COBOL VSAM file (CARDXREF)
-- Record layout from CVACT03Y.cpy: XREF-CARD-NUM PIC X(16), XREF-CUST-ID PIC 9(09), XREF-ACCT-ID PIC 9(11)

INSERT INTO card_xref (card_number, customer_id, account_id)
VALUES ('4111111111111111', 100000001, 10000000001);

INSERT INTO card_xref (card_number, customer_id, account_id)
VALUES ('4222222222222222', 100000002, 10000000002);

INSERT INTO card_xref (card_number, customer_id, account_id)
VALUES ('4333333333333333', 100000003, 10000000003);

INSERT INTO card_xref (card_number, customer_id, account_id)
VALUES ('4444444444444444', 100000004, 10000000004);

INSERT INTO card_xref (card_number, customer_id, account_id)
VALUES ('4555555555555555', 100000005, 10000000005);
