-- Flyway migration: Create card_xref table
-- Source: COBOL copybook CVACT03Y.cpy (CARD-XREF-RECORD, 50 bytes)
-- Seed data: cardxref.txt (50 records)

CREATE TABLE card_xref (
    xref_card_num       VARCHAR(16)  NOT NULL PRIMARY KEY,   -- PIC X(16)
    xref_cust_id        BIGINT,                              -- PIC 9(09)
    xref_acct_id        BIGINT                               -- PIC 9(11)
);
