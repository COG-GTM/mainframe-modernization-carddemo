-- =====================================================================
-- CardDemo interest calculator (modernized CBACT04C) - PostgreSQL schema
-- =====================================================================
-- VSAM KSDS files -> relational tables. Composite VSAM keys become composite
-- primary keys; the XREFFILE alternate index (CXACAIX) becomes a secondary index.
-- Monetary columns are NUMERIC (fixed-point) to match COBOL S9(i)V99 semantics.
--
-- The sample INSERTs below are derived from the ASCII fixtures in app/data/ASCII/
-- (acctdata.txt, cardxref.txt, discgrp.txt, tcatbal.txt); the zoned-decimal
-- overpunch signs in those files have been decoded to plain NUMERIC values and a
-- few balances/rates were given non-zero demo values so the run produces interest.
-- =====================================================================

DROP TABLE IF EXISTS transaction;
DROP TABLE IF EXISTS tran_cat_balance;
DROP TABLE IF EXISTS disclosure_group;
DROP TABLE IF EXISTS card_xref;
DROP TABLE IF EXISTS account;

-- ---------------------------------------------------------------------
-- ACCTFILE / CVACT01Y (ACCOUNT-RECORD, 300 bytes)
-- ---------------------------------------------------------------------
CREATE TABLE account (
    acct_id           BIGINT         NOT NULL,
    active_status     CHAR(1),
    curr_bal          NUMERIC(12, 2) NOT NULL DEFAULT 0,
    credit_limit      NUMERIC(12, 2)          DEFAULT 0,
    cash_credit_limit NUMERIC(12, 2)          DEFAULT 0,
    open_date         VARCHAR(10),
    expiration_date   VARCHAR(10),
    reissue_date      VARCHAR(10),
    curr_cyc_credit   NUMERIC(12, 2)          DEFAULT 0,
    curr_cyc_debit    NUMERIC(12, 2)          DEFAULT 0,
    addr_zip          VARCHAR(10),
    group_id          VARCHAR(10),
    CONSTRAINT pk_account PRIMARY KEY (acct_id)
);

-- ---------------------------------------------------------------------
-- XREFFILE / CVACT03Y (CARD-XREF-RECORD, 50 bytes)
-- Primary key = card number; alternate index (CXACAIX) on account id.
-- ---------------------------------------------------------------------
CREATE TABLE card_xref (
    card_num VARCHAR(16) NOT NULL,
    cust_id  BIGINT      NOT NULL,
    acct_id  BIGINT      NOT NULL,
    CONSTRAINT pk_card_xref PRIMARY KEY (card_num)
);
CREATE INDEX ix_card_xref_acct_id ON card_xref (acct_id);

-- ---------------------------------------------------------------------
-- DISCGRP / CVTRA02Y (DIS-GROUP-RECORD, 50 bytes)
-- Composite key (acct_group_id, tran_type_cd, tran_cat_cd).
-- ---------------------------------------------------------------------
CREATE TABLE disclosure_group (
    acct_group_id VARCHAR(10)   NOT NULL,
    tran_type_cd  CHAR(2)       NOT NULL,
    tran_cat_cd   INTEGER       NOT NULL,
    int_rate      NUMERIC(6, 2) NOT NULL DEFAULT 0,
    CONSTRAINT pk_disclosure_group PRIMARY KEY (acct_group_id, tran_type_cd, tran_cat_cd)
);

-- ---------------------------------------------------------------------
-- TCATBALF / CVTRA01Y (TRAN-CAT-BAL-RECORD, 50 bytes)
-- Composite key (acct_id, type_cd, cat_cd).
-- ---------------------------------------------------------------------
CREATE TABLE tran_cat_balance (
    acct_id      BIGINT         NOT NULL,
    type_cd      CHAR(2)        NOT NULL,
    cat_cd       INTEGER        NOT NULL,
    tran_cat_bal NUMERIC(11, 2) NOT NULL DEFAULT 0,
    CONSTRAINT pk_tran_cat_balance PRIMARY KEY (acct_id, type_cd, cat_cd)
);

-- ---------------------------------------------------------------------
-- TRANSACT / CVTRA05Y (TRAN-RECORD, 350 bytes) - sequential output
-- ---------------------------------------------------------------------
CREATE TABLE transaction (
    tran_id       VARCHAR(16)    NOT NULL,
    type_cd       CHAR(2),
    cat_cd        INTEGER,
    source        VARCHAR(10),
    description   VARCHAR(100),
    amount        NUMERIC(11, 2) DEFAULT 0,
    merchant_id   BIGINT,
    merchant_name VARCHAR(50),
    merchant_city VARCHAR(50),
    merchant_zip  VARCHAR(10),
    card_num      VARCHAR(16),
    orig_ts       VARCHAR(26),
    proc_ts       VARCHAR(26),
    CONSTRAINT pk_transaction PRIMARY KEY (tran_id)
);

-- =====================================================================
-- Sample data (derived from app/data/ASCII/)
-- =====================================================================

-- Accounts (group A000000000 has specific rates; non-zero cycle totals to show reset).
INSERT INTO account (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit,
                     open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit,
                     addr_zip, group_id) VALUES
    (1, 'Y', 1000.00, 5000.00, 2000.00, '2014-11-20', '2025-05-20', '2025-05-20', 150.00, 75.00, '00000', 'A000000000'),
    (2, 'Y',  500.00, 6000.00, 2500.00, '2013-06-19', '2024-08-11', '2024-08-11', 320.00, 40.00, '00000', 'A000000000');

-- Card cross-reference (acct -> card/customer).
INSERT INTO card_xref (card_num, cust_id, acct_id) VALUES
    ('0500024453765740', 5,  1),
    ('0683586198171516', 27, 2);

-- Disclosure groups: specific group A000000000 plus a DEFAULT fallback group.
INSERT INTO disclosure_group (acct_group_id, tran_type_cd, tran_cat_cd, int_rate) VALUES
    ('A000000000', '01', 1, 12.00),
    ('A000000000', '01', 2, 0.00),
    ('DEFAULT',    '01', 2, 5.00),
    ('DEFAULT',    '01', 5, 6.00);

-- Transaction-category balances (the driver file for CBACT04C).
INSERT INTO tran_cat_balance (acct_id, type_cd, cat_cd, tran_cat_bal) VALUES
    (1, '01', 1, 1000.00),   -- rate 12.00 -> interest (1000*12)/1200 = 10.00
    (1, '01', 2,  500.00),   -- group rate 0.00 -> skipped (IF DIS-INT-RATE NOT = 0)
    (2, '01', 5,  600.00);   -- no A000000000/01/5 -> DEFAULT/01/5 rate 6.00 -> (600*6)/1200 = 3.00
