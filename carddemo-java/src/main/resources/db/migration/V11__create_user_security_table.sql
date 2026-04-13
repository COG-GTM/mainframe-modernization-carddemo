-- Flyway migration: Create user_security table
-- Source: COBOL copybook CSUSR01Y.cpy (SEC-USER-DATA, 80 bytes)
-- Seed data: usrsec.txt

CREATE TABLE user_security (
    sec_usr_id           VARCHAR(8)   NOT NULL PRIMARY KEY,   -- PIC X(08)
    sec_usr_fname        VARCHAR(20),                         -- PIC X(20)
    sec_usr_lname        VARCHAR(20),                         -- PIC X(20)
    sec_usr_pwd          VARCHAR(8),                          -- PIC X(08)
    sec_usr_type         VARCHAR(1)                           -- PIC X(01)
);
