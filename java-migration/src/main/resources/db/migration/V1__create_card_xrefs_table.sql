-- Flyway migration: create card_xrefs table
-- Migrated from COBOL copybook CVACT03Y.cpy (CARD-XREF-RECORD, RECLN 50)

CREATE TABLE card_xrefs (
    card_num    VARCHAR(16)  NOT NULL,
    customer_id BIGINT       NOT NULL,
    account_id  BIGINT       NOT NULL,
    PRIMARY KEY (card_num)
);
