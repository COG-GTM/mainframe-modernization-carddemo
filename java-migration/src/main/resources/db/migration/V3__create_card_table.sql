-- Schema for the Card entity, migrated from COBOL copybook CVACT02Y.cpy
-- (VSAM CARDDAT KSDS, record length 150, key = CARD-NUM).
CREATE TABLE card (
    card_num             VARCHAR(16) NOT NULL,
    card_acct_id         BIGINT,
    card_cvv_cd          INTEGER,
    card_embossed_name   VARCHAR(50),
    card_expiration_date DATE,
    card_active_status   VARCHAR(1),
    CONSTRAINT pk_card PRIMARY KEY (card_num)
);

CREATE INDEX idx_card_acct_id ON card (card_acct_id);
CREATE INDEX idx_card_active_status ON card (card_active_status);
