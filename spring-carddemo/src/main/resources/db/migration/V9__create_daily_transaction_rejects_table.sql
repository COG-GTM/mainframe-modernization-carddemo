-- Daily transaction rejects table
-- Same fields as daily_transactions plus reject reason fields
-- Derived from the reject logic in CBTRN02C.cbl
CREATE TABLE daily_transaction_rejects (
    id                          BIGINT AUTO_INCREMENT NOT NULL,
    transaction_id              VARCHAR(16),
    transaction_type            VARCHAR(2),
    transaction_category        INTEGER,
    transaction_source          VARCHAR(10),
    transaction_description     VARCHAR(100),
    transaction_amount          DECIMAL(11,2),
    merchant_id                 VARCHAR(9),
    merchant_name               VARCHAR(50),
    merchant_city               VARCHAR(50),
    merchant_zip                VARCHAR(10),
    card_number                 VARCHAR(16),
    origin_timestamp            VARCHAR(26),
    processing_timestamp        VARCHAR(26),
    reject_reason_code          INTEGER,
    reject_reason_description   VARCHAR(200),
    CONSTRAINT pk_daily_transaction_rejects PRIMARY KEY (id)
);
