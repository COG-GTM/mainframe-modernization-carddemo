-- Flyway migration: Create users table
-- Mapped from COBOL copybook CSUSR01Y.cpy (SEC-USER-DATA / USRSEC VSAM file)
--
-- Original COBOL layout:
--   05 SEC-USR-ID      PIC X(08)  -> usr_id    VARCHAR(8)  PRIMARY KEY
--   05 SEC-USR-FNAME   PIC X(20)  -> usr_fname VARCHAR(20)
--   05 SEC-USR-LNAME   PIC X(20)  -> usr_lname VARCHAR(20)
--   05 SEC-USR-PWD     PIC X(08)  -> usr_pwd   VARCHAR(100) (BCrypt hashed)
--   05 SEC-USR-TYPE    PIC X(01)  -> usr_type  CHAR(1) ('A' = admin, 'U' = regular)
--   05 SEC-USR-FILLER  PIC X(23)  -> dropped (padding not needed in RDBMS)

CREATE TABLE users (
    usr_id    VARCHAR(8)   NOT NULL PRIMARY KEY,
    usr_fname VARCHAR(20),
    usr_lname VARCHAR(20),
    usr_pwd   VARCHAR(100) NOT NULL,
    usr_type  CHAR(1)      NOT NULL CHECK (usr_type IN ('A', 'U'))
);
