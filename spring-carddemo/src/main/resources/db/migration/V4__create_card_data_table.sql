-- Card data table derived from CVACT02Y.cpy (CARD-RECORD)
CREATE TABLE card_data (
    card_number      VARCHAR(16)  NOT NULL,
    account_id       BIGINT       NOT NULL,
    cvv_code         INTEGER,
    embossed_name    VARCHAR(50),
    expiration_date  VARCHAR(10),
    card_status      VARCHAR(1),
    CONSTRAINT pk_card_data PRIMARY KEY (card_number),
    CONSTRAINT fk_card_data_account FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);
