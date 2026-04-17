-- Flyway migration V1: Create cards and card_xref tables
-- Translated from COBOL copybooks CVACT02Y (CARD-RECORD) and CVACT03Y (CARD-XREF-RECORD)

CREATE TABLE cards (
    card_num              VARCHAR(16)  PRIMARY KEY,
    card_acct_id          VARCHAR(11)  NOT NULL,
    card_cvv_cd           VARCHAR(3),
    card_embossed_name    VARCHAR(50),
    card_expiration_date  VARCHAR(10),
    card_active_status    CHAR(1)
);

CREATE TABLE card_xref (
    xref_card_num   VARCHAR(16)  PRIMARY KEY,
    xref_cust_id    VARCHAR(9)   NOT NULL,
    xref_acct_id    VARCHAR(11)  NOT NULL
);

-- Critical index for xref lookups by account ID.
-- 12 programs across 5 other services depend on this lookup path.
CREATE INDEX idx_card_xref_acct_id ON card_xref (xref_acct_id);

-- Index for xref lookups by customer ID.
CREATE INDEX idx_card_xref_cust_id ON card_xref (xref_cust_id);
