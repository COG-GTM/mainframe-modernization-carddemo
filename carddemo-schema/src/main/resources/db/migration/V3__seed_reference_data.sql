-- ============================================================================
-- CardDemo PostgreSQL Schema
-- Migration V3: Seed reference data for transaction types and categories
--
-- Populates the transaction_types and transaction_categories lookup tables
-- with standard credit card transaction codes used throughout the CardDemo
-- application (batch posting, online inquiry, reporting programs).
-- ============================================================================

-- ============================================================================
-- Transaction Types (TRANTYPE)
-- Source: VSAM TRANTYPE file, referenced in CVTRA03Y copybook
-- ============================================================================
INSERT INTO transaction_types (type_cd, description) VALUES
    ('SA', 'Sale'),
    ('RT', 'Return'),
    ('CA', 'Cash Advance'),
    ('PA', 'Payment'),
    ('BA', 'Balance Adjustment'),
    ('FE', 'Fee'),
    ('IN', 'Interest Charge'),
    ('CR', 'Credit'),
    ('DB', 'Debit'),
    ('TR', 'Transfer');

-- ============================================================================
-- Transaction Categories (TRANCATG)
-- Source: VSAM TRANCATG file, referenced in CVTRA04Y copybook
-- Composite key: type_cd + cat_cd
-- ============================================================================

-- Sale categories
INSERT INTO transaction_categories (type_cd, cat_cd, description) VALUES
    ('SA', 5001, 'Retail Purchase'),
    ('SA', 5002, 'Online Purchase'),
    ('SA', 5003, 'Recurring Subscription'),
    ('SA', 5004, 'Grocery'),
    ('SA', 5005, 'Fuel'),
    ('SA', 5006, 'Restaurant'),
    ('SA', 5007, 'Travel'),
    ('SA', 5008, 'Entertainment'),
    ('SA', 5009, 'Healthcare'),
    ('SA', 5010, 'Utilities');

-- Return categories
INSERT INTO transaction_categories (type_cd, cat_cd, description) VALUES
    ('RT', 5001, 'Retail Return'),
    ('RT', 5002, 'Online Return'),
    ('RT', 5003, 'Subscription Refund');

-- Cash Advance categories
INSERT INTO transaction_categories (type_cd, cat_cd, description) VALUES
    ('CA', 6001, 'ATM Withdrawal'),
    ('CA', 6002, 'Over-the-Counter Cash Advance'),
    ('CA', 6003, 'Convenience Check');

-- Payment categories
INSERT INTO transaction_categories (type_cd, cat_cd, description) VALUES
    ('PA', 7001, 'Minimum Payment'),
    ('PA', 7002, 'Full Payment'),
    ('PA', 7003, 'Partial Payment'),
    ('PA', 7004, 'Auto-Pay');

-- Balance Adjustment categories
INSERT INTO transaction_categories (type_cd, cat_cd, description) VALUES
    ('BA', 8001, 'Dispute Credit'),
    ('BA', 8002, 'Dispute Reversal'),
    ('BA', 8003, 'Promotional Credit'),
    ('BA', 8004, 'Goodwill Adjustment');

-- Fee categories
INSERT INTO transaction_categories (type_cd, cat_cd, description) VALUES
    ('FE', 9001, 'Annual Fee'),
    ('FE', 9002, 'Late Payment Fee'),
    ('FE', 9003, 'Over-Limit Fee'),
    ('FE', 9004, 'Foreign Transaction Fee'),
    ('FE', 9005, 'Balance Transfer Fee'),
    ('FE', 9006, 'Cash Advance Fee'),
    ('FE', 9007, 'Returned Payment Fee');

-- Interest Charge categories
INSERT INTO transaction_categories (type_cd, cat_cd, description) VALUES
    ('IN', 1001, 'Purchase Interest'),
    ('IN', 1002, 'Cash Advance Interest'),
    ('IN', 1003, 'Balance Transfer Interest'),
    ('IN', 1004, 'Penalty Interest');

-- Credit categories
INSERT INTO transaction_categories (type_cd, cat_cd, description) VALUES
    ('CR', 2001, 'Merchant Credit'),
    ('CR', 2002, 'Promotional Cashback'),
    ('CR', 2003, 'Reward Redemption');

-- Debit categories
INSERT INTO transaction_categories (type_cd, cat_cd, description) VALUES
    ('DB', 3001, 'Account Debit'),
    ('DB', 3002, 'Service Charge');

-- Transfer categories
INSERT INTO transaction_categories (type_cd, cat_cd, description) VALUES
    ('TR', 4001, 'Balance Transfer In'),
    ('TR', 4002, 'Balance Transfer Out'),
    ('TR', 4003, 'Account-to-Account Transfer');
