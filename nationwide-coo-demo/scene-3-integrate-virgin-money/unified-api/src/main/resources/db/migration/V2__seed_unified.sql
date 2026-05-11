-- V2__seed_unified.sql — seed members in both source systems so the unified
-- API can demonstrate routing.

-- Nationwide member
INSERT INTO nw_customer (cust_id, cust_first_name, cust_middle_name, cust_last_name,
                         cust_addr_line_1, cust_addr_line_2, cust_addr_line_3,
                         cust_addr_state_cd, cust_addr_country_cd, cust_addr_zip,
                         cust_phone_num_1, cust_dob, cust_fico_credit_score)
VALUES
    (1234, 'Helen', NULL, 'Wright',
     'Pipers Way', 'Swindon', NULL,
     '  ', 'GBR', 'SN38 1NW',
     '01793 655555', '1973-08-22', 802);

INSERT INTO nw_account (acct_id, acct_active_status, acct_curr_bal, acct_credit_limit,
                        acct_open_date, acct_expiration_date, acct_addr_zip)
VALUES
    (12345678901, 'Y', 245.50, 5000.00, '2019-01-12', '2028-01-12', 'SN38 1NW');

INSERT INTO nw_card_xref (xref_card_num, xref_cust_id, xref_acct_id) VALUES
    ('4929123456789010', 1234, 12345678901);

-- Virgin Money member
INSERT INTO vm_primary_account (id, account_number, account_balance) VALUES
    (1, 30100001, 1842.75);
INSERT INTO vm_savings_account (id, account_number, account_balance) VALUES
    (1, 30200001, 18430.00);
INSERT INTO vm_user (user_id, username, first_name, last_name, email, phone,
                    enabled, primary_account_id, savings_account_id)
VALUES
    (1, 'sgupta', 'Sanjay', 'Gupta', 'sanjay.gupta@example.co.uk', '07700 900111',
     TRUE, 1, 1);
