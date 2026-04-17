-- Flyway migration: Create user_security table
-- Migrated from: USRSEC VSAM KSDS file
-- Copybook: CSUSR01Y.cpy (SEC-USER-DATA layout)
-- Original record length: 80 bytes (8+20+20+8+1+23 filler)

CREATE TABLE user_security (
    usr_id    VARCHAR(8)  NOT NULL,
    usr_fname VARCHAR(20) NOT NULL,
    usr_lname VARCHAR(20) NOT NULL,
    usr_pwd   VARCHAR(8)  NOT NULL,
    usr_type  VARCHAR(1)  NOT NULL,
    CONSTRAINT pk_user_security PRIMARY KEY (usr_id),
    CONSTRAINT chk_usr_type CHECK (usr_type IN ('A', 'U'))
);
