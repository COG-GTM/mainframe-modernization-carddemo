-- ==========================================================================
-- Flyway Migration V2: Seed test data for development/demo
--
-- Provides sample card cross-reference and account records
-- so the transaction service can be tested end-to-end with H2.
-- ==========================================================================

INSERT INTO card_cross_references (card_number, customer_id, account_id)
VALUES ('4000123456789010', 123456789, 12345678901);

INSERT INTO card_cross_references (card_number, customer_id, account_id)
VALUES ('4000567890123456', 987654321, 98765432109);

INSERT INTO accounts (account_id, active_status, current_balance, credit_limit,
    cash_credit_limit, open_date, expiration_date, reissue_date,
    current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (12345678901, 'Y', 5000.00, 10000.00, 2000.00,
    '2020-01-15', '2027-01-15', '2025-01-15',
    500.00, 1200.00, '98101', 'GRP001');

INSERT INTO accounts (account_id, active_status, current_balance, credit_limit,
    cash_credit_limit, open_date, expiration_date, reissue_date,
    current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (98765432109, 'Y', 3000.00, 8000.00, 1500.00,
    '2021-06-01', '2028-06-01', '2026-06-01',
    300.00, 800.00, '10001', 'GRP002');
