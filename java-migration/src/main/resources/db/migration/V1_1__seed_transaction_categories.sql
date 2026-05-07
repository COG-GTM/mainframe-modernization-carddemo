-- Flyway migration: Seed transaction_categories with 18 records
-- Source: app/data/ASCII/trancatg.txt (fixed-width, 60 bytes per record)
-- Layout: TRAN-TYPE-CD(2) + TRAN-CAT-CD(4) + TRAN-CAT-TYPE-DESC(50) + FILLER(4)

INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('01', 1, 'Regular Sales Draft');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('01', 2, 'Regular Cash Advance');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('01', 3, 'Convenience Check Debit');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('01', 4, 'ATM Cash Advance');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('01', 5, 'Interest Amount');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('02', 1, 'Cash payment');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('02', 2, 'Electronic payment');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('02', 3, 'Check payment');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('03', 1, 'Credit to Account');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('03', 2, 'Credit to Purchase balance');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('03', 3, 'Credit to Cash balance');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('04', 1, 'Zero dollar authorization');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('04', 2, 'Online purchase authorization');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('04', 3, 'Travel booking authorization');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('05', 1, 'Refund credit');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('06', 1, 'Fraud reversal');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('06', 2, 'Non-fraud reversal');
INSERT INTO transaction_categories (type_code, category_code, description) VALUES ('07', 1, 'Sales draft credit adjustment');
