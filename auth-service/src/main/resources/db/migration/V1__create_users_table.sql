-- Flyway migration: Create users table matching COBOL SEC-USER-DATA (CSUSR01Y.cpy)
--
-- COBOL layout (80 bytes):
--   SEC-USR-ID      PIC X(08)  -- User ID (primary key)
--   SEC-USR-FNAME   PIC X(20)  -- First name
--   SEC-USR-LNAME   PIC X(20)  -- Last name
--   SEC-USR-PWD     PIC X(08)  -- Password (stored as bcrypt hash, 72 chars)
--   SEC-USR-TYPE    PIC X(01)  -- 'A' = admin, 'U' = regular user
--   SEC-USR-FILLER  PIC X(23)  -- Filler (not migrated)

CREATE TABLE users (
    usr_id    VARCHAR(8)  NOT NULL,
    usr_fname VARCHAR(20) NOT NULL,
    usr_lname VARCHAR(20) NOT NULL,
    usr_pwd   VARCHAR(72) NOT NULL,
    usr_type  VARCHAR(1)  NOT NULL CHECK (usr_type IN ('A', 'U')),
    CONSTRAINT pk_users PRIMARY KEY (usr_id)
);

-- Seed data matching original COBOL VSAM USRSEC file defaults
-- Passwords are BCrypt hashes of the UPPER-CASED original passwords
-- ADMIN001/PASSWORD -> BCrypt('PASSWORD')
-- USER0001/PASSWORD -> BCrypt('PASSWORD')
INSERT INTO users (usr_id, usr_fname, usr_lname, usr_pwd, usr_type) VALUES
    ('ADMIN001', 'ADMIN', 'USER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'A'),
    ('USER0001', 'REGULAR', 'USER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'U');
