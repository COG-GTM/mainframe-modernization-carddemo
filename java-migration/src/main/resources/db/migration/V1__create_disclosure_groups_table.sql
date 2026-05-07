-- Flyway migration: Create disclosure_groups table
-- Migrated from COBOL copybook CVTRA02Y.cpy (DIS-GROUP-RECORD, RECLN = 50)
-- Original VSAM file: MFE.CARDDEMO.DISCGRP.PS

CREATE TABLE disclosure_groups (
    account_group_id         VARCHAR(10)    NOT NULL,
    transaction_type_code    VARCHAR(2)     NOT NULL,
    transaction_category_code INTEGER       NOT NULL,
    interest_rate            DECIMAL(6, 2)  NOT NULL,

    PRIMARY KEY (account_group_id, transaction_type_code, transaction_category_code)
);
