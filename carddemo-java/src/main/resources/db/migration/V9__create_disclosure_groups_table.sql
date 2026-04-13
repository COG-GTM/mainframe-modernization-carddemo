-- Flyway migration: Create disclosure_groups table
-- Source: COBOL copybook CVTRA02Y.cpy (DIS-GROUP-RECORD, 50 bytes)
-- Seed data: discgrp.txt

CREATE TABLE disclosure_groups (
    dis_acct_group_id    VARCHAR(10)  NOT NULL,               -- PIC X(10)
    dis_tran_type_cd     VARCHAR(2)   NOT NULL,               -- PIC X(02)
    dis_tran_cat_cd      INTEGER      NOT NULL,               -- PIC 9(04)
    dis_int_rate         DECIMAL(6,2),                        -- PIC S9(04)V99
    PRIMARY KEY (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd)
);
