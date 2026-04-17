-- Flyway migration V1: Create users table
-- Modernized from COBOL copybook CSUSR01Y (SEC-USER-DATA)
-- Original VSAM KSDS file: USRSEC
CREATE TABLE users (
    usr_id    VARCHAR(8)   NOT NULL PRIMARY KEY,
    usr_fname VARCHAR(20)  NOT NULL,
    usr_lname VARCHAR(20)  NOT NULL,
    usr_pwd   VARCHAR(100) NOT NULL,
    usr_type  CHAR(1)      NOT NULL CHECK (usr_type IN ('A', 'U'))
);
