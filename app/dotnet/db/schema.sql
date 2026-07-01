-- CardDemo modernized relational schema (PostgreSQL)
-- Target for the migrated batch programs CBACT01C (account) and CBACT02C (card).
--
-- Field types are derived from the COBOL copybooks:
--   CVACT01Y (ACCOUNT-RECORD, RECLN 300)
--   CVACT02Y (CARD-RECORD,   RECLN 150)
--
-- Signed COBOL PIC S9(10)V99 zoned-decimal fields map to NUMERIC(12, 2).
-- Date fields are stored as fixed-width text to preserve the original
-- "YYYY-MM-DD" (or blank) representation exactly.

CREATE TABLE IF NOT EXISTS account (
    acct_id                BIGINT         PRIMARY KEY,               -- ACCT-ID PIC 9(11)
    acct_active_status     CHAR(1)        NOT NULL DEFAULT '',       -- ACCT-ACTIVE-STATUS PIC X(01)
    acct_curr_bal          NUMERIC(12, 2) NOT NULL DEFAULT 0,        -- ACCT-CURR-BAL PIC S9(10)V99
    acct_credit_limit      NUMERIC(12, 2) NOT NULL DEFAULT 0,        -- ACCT-CREDIT-LIMIT
    acct_cash_credit_limit NUMERIC(12, 2) NOT NULL DEFAULT 0,        -- ACCT-CASH-CREDIT-LIMIT
    acct_open_date         VARCHAR(10)    NOT NULL DEFAULT '',       -- ACCT-OPEN-DATE PIC X(10)
    acct_expiration_date   VARCHAR(10)    NOT NULL DEFAULT '',       -- ACCT-EXPIRAION-DATE
    acct_reissue_date      VARCHAR(10)    NOT NULL DEFAULT '',       -- ACCT-REISSUE-DATE
    acct_curr_cyc_credit   NUMERIC(12, 2) NOT NULL DEFAULT 0,        -- ACCT-CURR-CYC-CREDIT
    acct_curr_cyc_debit    NUMERIC(12, 2) NOT NULL DEFAULT 0,        -- ACCT-CURR-CYC-DEBIT
    acct_addr_zip          VARCHAR(10)    NOT NULL DEFAULT '',       -- ACCT-ADDR-ZIP
    acct_group_id          VARCHAR(10)    NOT NULL DEFAULT ''        -- ACCT-GROUP-ID
);

CREATE TABLE IF NOT EXISTS card (
    card_num             VARCHAR(16) PRIMARY KEY,                    -- CARD-NUM PIC X(16)
    card_acct_id         BIGINT      NOT NULL,                       -- CARD-ACCT-ID PIC 9(11)
    card_cvv_cd          INTEGER     NOT NULL DEFAULT 0,             -- CARD-CVV-CD PIC 9(03)
    card_embossed_name   VARCHAR(50) NOT NULL DEFAULT '',            -- CARD-EMBOSSED-NAME PIC X(50)
    card_expiration_date VARCHAR(10) NOT NULL DEFAULT '',            -- CARD-EXPIRAION-DATE PIC X(10)
    card_active_status   CHAR(1)     NOT NULL DEFAULT ''             -- CARD-ACTIVE-STATUS PIC X(01)
);

-- Mirrors the CARDFILE alternate index (AIX) on account id, which supports
-- non-primary-key access from card to account.
CREATE INDEX IF NOT EXISTS ix_card_acct_id ON card (card_acct_id);
