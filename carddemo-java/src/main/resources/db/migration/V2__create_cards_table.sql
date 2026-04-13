-- Flyway migration: Create cards table
-- Source: COBOL copybook CVACT02Y.cpy (CARD-RECORD, 150 bytes)
-- Seed data: carddata.txt (50 records)

CREATE TABLE cards (
    card_num            VARCHAR(16)  NOT NULL PRIMARY KEY,   -- PIC X(16)
    card_acct_id        BIGINT,                              -- PIC 9(11)
    card_cvv_cd         INTEGER,                             -- PIC 9(03)
    card_embossed_name  VARCHAR(50),                         -- PIC X(50)
    card_expiration_date DATE,                               -- PIC X(10) YYYY-MM-DD
    card_active_status  VARCHAR(1)                           -- PIC X(01)
);
