-- Transactions table derived from CVTRA05Y.cpy (TRAN-RECORD)
CREATE TABLE transactions (
    transaction_id          VARCHAR(16)    NOT NULL,
    transaction_type        VARCHAR(2),
    transaction_category    INTEGER,
    transaction_source      VARCHAR(10),
    transaction_description VARCHAR(100),
    transaction_amount      DECIMAL(11,2),
    merchant_id             VARCHAR(9),
    merchant_name           VARCHAR(50),
    merchant_city           VARCHAR(50),
    merchant_zip            VARCHAR(10),
    card_number             VARCHAR(16),
    origin_timestamp        VARCHAR(26),
    processing_timestamp    VARCHAR(26),
    CONSTRAINT pk_transactions PRIMARY KEY (transaction_id)
);
