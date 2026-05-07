-- Flyway migration: Seed transaction_types with 7 records
-- Source: app/data/ASCII/trantype.txt
-- Parsed from fixed-width COBOL record layout (CVTRA03Y.cpy)

INSERT INTO transaction_types (type_code, description) VALUES ('01', 'Purchase');
INSERT INTO transaction_types (type_code, description) VALUES ('02', 'Payment');
INSERT INTO transaction_types (type_code, description) VALUES ('03', 'Credit');
INSERT INTO transaction_types (type_code, description) VALUES ('04', 'Authorization');
INSERT INTO transaction_types (type_code, description) VALUES ('05', 'Refund');
INSERT INTO transaction_types (type_code, description) VALUES ('06', 'Reversal');
INSERT INTO transaction_types (type_code, description) VALUES ('07', 'Adjustment');
