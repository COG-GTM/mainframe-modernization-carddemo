CREATE TABLE cards (
    card_num            VARCHAR(16)  NOT NULL,
    card_acct_id        BIGINT       NOT NULL,
    card_cvv_cd         INTEGER      NOT NULL,
    card_embossed_name  VARCHAR(50)  NOT NULL,
    card_expiration_date DATE        NOT NULL,
    card_active_status  VARCHAR(1)   NOT NULL,
    PRIMARY KEY (card_num)
);
