-- Flyway migration: Create user_security table
-- Migrated from: VSAM KSDS file USRSEC (AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS)
-- Copybook: CSUSR01Y.cpy (SEC-USER-DATA record layout)
--
-- Original VSAM record layout (80 bytes total):
--   SEC-USR-ID      PIC X(08)  -> usr_id    VARCHAR(8)  PRIMARY KEY
--   SEC-USR-FNAME   PIC X(20)  -> usr_fname VARCHAR(20)
--   SEC-USR-LNAME   PIC X(20)  -> usr_lname VARCHAR(20)
--   SEC-USR-PWD     PIC X(08)  -> usr_pwd   VARCHAR(8)
--   SEC-USR-TYPE    PIC X(01)  -> usr_type  CHAR(1) CHECK ('A' or 'U')
--   SEC-USR-FILLER  PIC X(23)  -> (dropped - padding only)

CREATE TABLE user_security (
    usr_id    VARCHAR(8)  NOT NULL,
    usr_fname VARCHAR(20) NOT NULL,
    usr_lname VARCHAR(20) NOT NULL,
    usr_pwd   VARCHAR(8)  NOT NULL,
    usr_type  VARCHAR(1)  NOT NULL,

    CONSTRAINT pk_user_security PRIMARY KEY (usr_id),
    CONSTRAINT chk_usr_type CHECK (usr_type IN ('A', 'U'))
);
