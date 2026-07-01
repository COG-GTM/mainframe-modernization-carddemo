namespace CardDemo.Data.Database;

/// <summary>
/// Idempotent PostgreSQL DDL for the modernized CardDemo account/card store.
/// Kept in sync with db/schema.sql.
/// </summary>
public static class SchemaScript
{
    public const string CreateTables = """
        CREATE TABLE IF NOT EXISTS account (
            acct_id                BIGINT         PRIMARY KEY,
            acct_active_status     CHAR(1)        NOT NULL DEFAULT '',
            acct_curr_bal          NUMERIC(12, 2) NOT NULL DEFAULT 0,
            acct_credit_limit      NUMERIC(12, 2) NOT NULL DEFAULT 0,
            acct_cash_credit_limit NUMERIC(12, 2) NOT NULL DEFAULT 0,
            acct_open_date         VARCHAR(10)    NOT NULL DEFAULT '',
            acct_expiration_date   VARCHAR(10)    NOT NULL DEFAULT '',
            acct_reissue_date      VARCHAR(10)    NOT NULL DEFAULT '',
            acct_curr_cyc_credit   NUMERIC(12, 2) NOT NULL DEFAULT 0,
            acct_curr_cyc_debit    NUMERIC(12, 2) NOT NULL DEFAULT 0,
            acct_addr_zip          VARCHAR(10)    NOT NULL DEFAULT '',
            acct_group_id          VARCHAR(10)    NOT NULL DEFAULT ''
        );

        CREATE TABLE IF NOT EXISTS card (
            card_num             VARCHAR(16) PRIMARY KEY,
            card_acct_id         BIGINT      NOT NULL,
            card_cvv_cd          INTEGER     NOT NULL DEFAULT 0,
            card_embossed_name   VARCHAR(50) NOT NULL DEFAULT '',
            card_expiration_date VARCHAR(10) NOT NULL DEFAULT '',
            card_active_status   CHAR(1)     NOT NULL DEFAULT ''
        );

        -- Mirrors the CARDFILE alternate index (AIX) on account id.
        CREATE INDEX IF NOT EXISTS ix_card_acct_id ON card (card_acct_id);
        """;
}
