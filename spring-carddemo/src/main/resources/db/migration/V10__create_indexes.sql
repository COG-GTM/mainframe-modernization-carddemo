-- Indexes on frequently queried columns
CREATE INDEX idx_card_xref_account_id  ON card_xref(account_id);
CREATE INDEX idx_card_xref_customer_id ON card_xref(customer_id);
CREATE INDEX idx_card_data_account_id  ON card_data(account_id);
CREATE INDEX idx_transactions_card_number ON transactions(card_number);
