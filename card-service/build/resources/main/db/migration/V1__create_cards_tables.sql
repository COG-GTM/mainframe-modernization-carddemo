-- =============================================================================
-- Card Service Schema
-- Ported from COBOL copybook CVACT02Y.cpy (CARD-RECORD, 150 bytes)
-- and CVACT03Y.cpy (CARD-XREF-RECORD, 50 bytes)
-- =============================================================================

CREATE TABLE cards (
    card_num            VARCHAR(16)  NOT NULL,
    card_acct_id        NUMERIC(11)  NOT NULL,
    card_cvv_cd         NUMERIC(3)   NOT NULL,
    card_embossed_name  VARCHAR(50)  NOT NULL,
    card_expiration_date VARCHAR(10) NOT NULL,
    card_active_status  CHAR(1)      NOT NULL DEFAULT 'N',

    CONSTRAINT pk_cards PRIMARY KEY (card_num),
    CONSTRAINT chk_card_active_status CHECK (card_active_status IN ('Y', 'N'))
);

CREATE INDEX idx_cards_acct_id ON cards (card_acct_id);

CREATE TABLE card_xref (
    xref_card_num  VARCHAR(16)  NOT NULL,
    xref_cust_id   NUMERIC(9)   NOT NULL,
    xref_acct_id   NUMERIC(11)  NOT NULL,

    CONSTRAINT pk_card_xref PRIMARY KEY (xref_card_num),
    CONSTRAINT fk_card_xref_card FOREIGN KEY (xref_card_num) REFERENCES cards(card_num)
);

CREATE INDEX idx_card_xref_acct_id ON card_xref (xref_acct_id);
CREATE INDEX idx_card_xref_cust_id ON card_xref (xref_cust_id);
