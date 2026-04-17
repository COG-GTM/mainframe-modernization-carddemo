-- Flyway migration V5: Create card cross-reference table
-- Migrated from: CXACAIX VSAM AIX PATH on CARDXREF (CVACT03Y.cpy — Card XREF, RECLN 50)
-- Primary key: XREF-CARD-NUM PIC X(16)
-- Cross-reference: card_num -> cust_id -> acct_id

CREATE TABLE card_xref (
    card_num    VARCHAR(16)  NOT NULL PRIMARY KEY,
    cust_id     BIGINT       NOT NULL,
    acct_id     BIGINT       NOT NULL
);

CREATE INDEX idx_card_xref_acct_id ON card_xref (acct_id);
CREATE INDEX idx_card_xref_cust_id ON card_xref (cust_id);

-- Seed data: sample cross-references linking cards to accounts and customers
INSERT INTO card_xref (card_num, cust_id, acct_id)
VALUES
    ('4111111111111111', 100000001, 80001000001),
    ('4111111111112222', 100000001, 80001000001),
    ('4222222222221111', 100000002, 80001000002),
    ('4333333333331111', 100000003, 80001000003),
    ('4444444444441111', 100000004, 80001000004),
    ('4444444444442222', 100000004, 80001000004),
    ('4555555555551111', 100000005, 80001000005);
