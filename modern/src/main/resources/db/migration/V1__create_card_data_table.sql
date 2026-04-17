-- Card data table matching CVACT02Y.cpy CARD-RECORD (150-byte VSAM layout)
-- VSAM file: CARDDAT (KSDS, primary key = CARD-NUM)
--
-- Migrated from: COCRDLIC.cbl, COCRDSLC.cbl, COCRDUPC.cbl
-- CICS file control: READ/REWRITE/STARTBR/READNEXT/READPREV on CARDDAT
CREATE TABLE card_data (
    card_num                VARCHAR(16)     NOT NULL PRIMARY KEY,
    card_acct_id            NUMERIC(11,0)   NOT NULL,
    card_cvv_cd             NUMERIC(3,0)    NOT NULL,
    card_embossed_name      VARCHAR(50),
    card_expiration_date    VARCHAR(10),
    card_active_status      CHAR(1)         NOT NULL DEFAULT 'Y',
    version                 BIGINT          NOT NULL DEFAULT 0
);

-- Index on account ID for filtered queries (replaces CXACAIX alternate index path)
CREATE INDEX idx_card_data_acct_id ON card_data(card_acct_id);

-- Seed sample card records linked to accounts
-- Data derived from app/data/EBCDIC/AWS.M2.CARDDEMO.CARDDATA.PS sample file
INSERT INTO card_data (card_num, card_acct_id, card_cvv_cd, card_embossed_name, card_expiration_date, card_active_status) VALUES
    ('4111111111111111', 00000000001, 123, 'JOHN DOE',               '12-31-2026', 'Y'),
    ('4111111111111112', 00000000001, 456, 'JOHN DOE',               '06-30-2027', 'Y'),
    ('4222222222222222', 00000000002, 789, 'JANE SMITH',             '03-15-2026', 'Y'),
    ('4333333333333333', 00000000003, 321, 'ROBERT JOHNSON',         '09-30-2025', 'N'),
    ('4444444444444444', 00000000004, 654, 'MARIA GARCIA',           '01-31-2027', 'Y'),
    ('4555555555555555', 00000000005, 987, 'WILLIAM BROWN',          '07-31-2026', 'Y'),
    ('4666666666666666', 00000000006, 111, 'PATRICIA WILLIAMS',      '11-30-2026', 'Y'),
    ('4777777777777777', 00000000007, 222, 'JAMES JONES',            '04-30-2027', 'N'),
    ('4888888888888888', 00000000008, 333, 'ELIZABETH MILLER',       '08-31-2026', 'Y'),
    ('4999999999999999', 00000000009, 444, 'DAVID DAVIS',            '02-28-2027', 'Y'),
    ('5111111111111111', 00000000010, 555, 'MARY WILSON',            '10-31-2026', 'Y'),
    ('5222222222222222', 00000000001, 666, 'JOHN DOE BUSINESS',      '05-31-2027', 'Y');
