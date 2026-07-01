-- Relational schema for the migrated CardDemo account master.
-- Replaces VSAM KSDS dataset AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS.
-- Source layout: copybook CVACT01Y (ACCOUNT-RECORD, RECLN 300).

CREATE TABLE IF NOT EXISTS account (
    -- ACCT-ID PIC 9(11)  -- VSAM primary key
    acct_id           BIGINT         NOT NULL,
    -- ACCT-ACTIVE-STATUS PIC X(01)
    active_status     CHAR(1)        NOT NULL,
    -- ACCT-CURR-BAL PIC S9(10)V99
    curr_bal          NUMERIC(12, 2) NOT NULL,
    -- ACCT-CREDIT-LIMIT PIC S9(10)V99
    credit_limit      NUMERIC(12, 2) NOT NULL,
    -- ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
    cash_credit_limit NUMERIC(12, 2) NOT NULL,
    -- ACCT-OPEN-DATE PIC X(10) -- YYYY-MM-DD, kept as text for byte parity
    open_date         VARCHAR(10)    NOT NULL,
    -- ACCT-EXPIRAION-DATE PIC X(10)
    expiration_date   VARCHAR(10)    NOT NULL,
    -- ACCT-REISSUE-DATE PIC X(10)
    reissue_date      VARCHAR(10)    NOT NULL,
    -- ACCT-CURR-CYC-CREDIT PIC S9(10)V99
    curr_cyc_credit   NUMERIC(12, 2) NOT NULL,
    -- ACCT-CURR-CYC-DEBIT PIC S9(10)V99
    curr_cyc_debit    NUMERIC(12, 2) NOT NULL,
    -- ACCT-ADDR-ZIP PIC X(10)
    addr_zip          VARCHAR(10)    NOT NULL,
    -- ACCT-GROUP-ID PIC X(10)
    group_id          VARCHAR(10)    NOT NULL,
    CONSTRAINT pk_account PRIMARY KEY (acct_id)
);
