-- Flyway migration: Seed test users
-- Migrated from: VSAM USRSEC file sample data
-- Default credentials from CardDemo README:
--   Admin:   ADMIN001 / ADMIN001
--   Regular: USER0001 / USER0001
--
-- NOTE: Passwords are stored in plaintext to match the legacy COBOL behavior
-- where SEC-USR-PWD is PIC X(08). TODO: Migrate to BCrypt hashing.

INSERT INTO user_security (usr_id, usr_fname, usr_lname, usr_pwd, usr_type)
VALUES ('ADMIN001', 'Admin', 'User', 'ADMIN001', 'A');

INSERT INTO user_security (usr_id, usr_fname, usr_lname, usr_pwd, usr_type)
VALUES ('USER0001', 'Regular', 'User', 'USER0001', 'U');
