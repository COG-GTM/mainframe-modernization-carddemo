-- Card cross-reference table derived from CVACT03Y.cpy (CARD-XREF-RECORD)
-- Central lookup table that links cards -> accounts -> customers
CREATE TABLE card_xref (
    card_number  VARCHAR(16)  NOT NULL,
    account_id   BIGINT       NOT NULL,
    customer_id  BIGINT       NOT NULL,
    CONSTRAINT pk_card_xref PRIMARY KEY (card_number),
    CONSTRAINT fk_card_xref_account  FOREIGN KEY (account_id)  REFERENCES accounts(account_id),
    CONSTRAINT fk_card_xref_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);
