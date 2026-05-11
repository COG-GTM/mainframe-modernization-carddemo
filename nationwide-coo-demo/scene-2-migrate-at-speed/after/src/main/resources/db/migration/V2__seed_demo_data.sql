-- V2__seed_demo_data.sql
-- Seed data for the demo. Enough to exercise the success path and each
-- of the six rejection paths exposed by ValidationFailure.

INSERT INTO account (acct_id, acct_active_status, acct_curr_bal, acct_credit_limit,
                     acct_cash_credit_limit, acct_open_date, acct_expiration_date,
                     acct_reissue_date, acct_curr_cyc_credit, acct_curr_cyc_debit,
                     acct_addr_zip, acct_group_id)
VALUES
    -- Healthy active account
    (12345678901, 'Y', 100.00,  5000.00, 1000.00, '2018-04-12', '2027-04-12', '2023-04-12', 0, 0, 'SW1A 1AA', 'STD'),
    -- Closed account
    (12345678902, 'N',   0.00,  5000.00, 1000.00, '2014-04-12', '2024-04-12', '2019-04-12', 0, 0, 'NW1 2BB',  'STD'),
    -- Account near credit limit
    (12345678903, 'Y', 4990.00, 5000.00, 1000.00, '2019-04-12', '2028-04-12', '2024-04-12', 0, 0, 'M1 1CC',   'STD');

INSERT INTO card (card_num, card_acct_id, card_cvv_cd, card_embossed_name,
                  card_expiration_date, card_active_status)
VALUES
    ('4929123456789010', 12345678901, 123, 'MEMBER ONE',       '2027-04-30', 'Y'),
    ('4929123456789020', 12345678902, 456, 'CLOSED CUSTOMER',  '2024-04-30', 'Y'),
    ('4929123456789030', 12345678903, 789, 'NEAR LIMIT',       '2028-04-30', 'Y'),
    ('4929123456789040', 12345678901, 321, 'EXPIRED CARD',     '2020-04-30', 'Y'),
    ('4929123456789050', 12345678901, 654, 'INACTIVE CARD',    '2027-04-30', 'N');

INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id) VALUES
    ('4929123456789010', 100000001, 12345678901),
    ('4929123456789020', 100000002, 12345678902),
    ('4929123456789030', 100000003, 12345678903),
    ('4929123456789040', 100000001, 12345678901),
    ('4929123456789050', 100000001, 12345678901);
