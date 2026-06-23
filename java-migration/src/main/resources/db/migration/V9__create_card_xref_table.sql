-- Schema for the CardXref entity, migrated from COBOL copybook CVACT03Y.cpy
-- (VSAM CARDXREF KSDS, record length 50, key = XREF-CARD-NUM).
CREATE TABLE card_xref (
    xref_card_num VARCHAR(16) NOT NULL,
    xref_cust_id  BIGINT,
    xref_acct_id  BIGINT,
    CONSTRAINT pk_card_xref PRIMARY KEY (xref_card_num)
);

CREATE INDEX idx_card_xref_acct_id ON card_xref (xref_acct_id);
CREATE INDEX idx_card_xref_cust_id ON card_xref (xref_cust_id);
