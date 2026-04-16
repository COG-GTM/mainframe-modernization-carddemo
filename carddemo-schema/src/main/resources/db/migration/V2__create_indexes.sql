-- ============================================================================
-- CardDemo PostgreSQL Schema
-- Migration V2: Create secondary indexes matching VSAM alternate index paths
--
-- These indexes mirror the VSAM AIX (Alternate Index) paths defined in the
-- mainframe LISTCAT and COBOL SELECT statements, plus additional indexes
-- for common query patterns identified in the COBOL programs.
-- ============================================================================

-- ============================================================================
-- CARDDAT AIX: Alternate index on cards by acct_id
-- VSAM: AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX (KEYLEN 11, RKP 16)
-- Enables lookup of all cards belonging to an account.
-- ============================================================================
CREATE INDEX idx_cards_acct_id ON cards (acct_id);

-- ============================================================================
-- CARDXREF AIX: CXACAIX - Alternate index on card_xref by acct_id
-- Enables lookup of all card cross-references for a given account.
-- Used by account inquiry and transaction processing programs.
-- ============================================================================
CREATE INDEX idx_card_xref_acct_id ON card_xref (acct_id);

-- ============================================================================
-- CARDXREF AIX: Alternate index on card_xref by cust_id
-- Enables lookup of all cards for a given customer.
-- Used by customer inquiry programs.
-- ============================================================================
CREATE INDEX idx_card_xref_cust_id ON card_xref (cust_id);

-- ============================================================================
-- TRANSACT: Index on transactions by card_num
-- Supports transaction listing by card number (COCRDSLC, COTRN00C).
-- ============================================================================
CREATE INDEX idx_transactions_card_num ON transactions (card_num);

-- ============================================================================
-- TRANSACT: Index on transactions by type_cd and cat_cd
-- Supports transaction category reporting and batch processing.
-- ============================================================================
CREATE INDEX idx_transactions_type_cat ON transactions (type_cd, cat_cd);

-- ============================================================================
-- DALYTRAN: Index on daily_transactions by card_num
-- Supports daily transaction processing lookups by card.
-- ============================================================================
CREATE INDEX idx_daily_transactions_card_num ON daily_transactions (card_num);

-- ============================================================================
-- DALYTRAN: Index on daily_transactions by tran_id
-- Supports lookup of specific transactions in the daily log.
-- ============================================================================
CREATE INDEX idx_daily_transactions_tran_id ON daily_transactions (tran_id);

-- ============================================================================
-- DALYREJS: Index on daily_rejects by card_num
-- Supports reject investigation by card number.
-- ============================================================================
CREATE INDEX idx_daily_rejects_card_num ON daily_rejects (card_num);

-- ============================================================================
-- DALYREJS: Index on daily_rejects by tran_id
-- Supports linking rejected transactions back to originals.
-- ============================================================================
CREATE INDEX idx_daily_rejects_tran_id ON daily_rejects (tran_id);

-- ============================================================================
-- TCATBALF: Index on tran_cat_balances by acct_id
-- Already part of composite PK, but explicit index for single-column lookups
-- used in account balance summary queries.
-- ============================================================================
CREATE INDEX idx_tran_cat_balances_acct_id ON tran_cat_balances (acct_id);

-- ============================================================================
-- ACCOUNTS: Index on accounts by group_id
-- Supports disclosure group lookups and batch interest calculation.
-- ============================================================================
CREATE INDEX idx_accounts_group_id ON accounts (group_id);

-- ============================================================================
-- CUSTOMERS: Index on customers by last_name
-- Supports customer search by name in online programs.
-- ============================================================================
CREATE INDEX idx_customers_last_name ON customers (last_name);

-- ============================================================================
-- CUSTOMERS: Index on customers by ssn
-- Supports customer lookup by SSN.
-- ============================================================================
CREATE UNIQUE INDEX idx_customers_ssn ON customers (ssn) WHERE ssn IS NOT NULL;
