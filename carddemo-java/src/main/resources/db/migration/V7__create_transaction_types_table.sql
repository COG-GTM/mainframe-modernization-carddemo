-- Flyway migration: Create transaction_types table
-- Source: COBOL copybook CVTRA03Y.cpy (TRAN-TYPE-RECORD, 60 bytes)
-- Seed data: trantype.txt

CREATE TABLE transaction_types (
    tran_type            VARCHAR(2)   NOT NULL PRIMARY KEY,   -- PIC X(02)
    tran_type_desc       VARCHAR(50)                          -- PIC X(50)
);
