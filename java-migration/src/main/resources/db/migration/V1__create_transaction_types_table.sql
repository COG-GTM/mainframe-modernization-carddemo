-- Flyway migration: Create transaction_types table
-- Migrated from COBOL copybook CVTRA03Y.cpy (RECLN = 60)
--
-- COBOL layout:
--   01  TRAN-TYPE-RECORD.
--       05  TRAN-TYPE        PIC X(02).  -> type_code  VARCHAR(2)  PK
--       05  TRAN-TYPE-DESC   PIC X(50).  -> description VARCHAR(50)
--       05  FILLER           PIC X(08).  (not mapped)

CREATE TABLE transaction_types (
    type_code   VARCHAR(2)  NOT NULL,
    description VARCHAR(50) NOT NULL,
    PRIMARY KEY (type_code)
);
