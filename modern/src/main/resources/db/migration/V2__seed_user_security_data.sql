-- Flyway migration: Seed user_security table with test data
-- Migrated from: DUSRSECJ.jcl (Initial load of USRSEC VSAM file)
-- Default credentials from README.md:
--   ADMIN001/PASSWORD (admin user)
--   USER0001/PASSWORD (regular user)

INSERT INTO user_security (usr_id, usr_fname, usr_lname, usr_pwd, usr_type) VALUES
    ('ADMIN001', 'ADMIN',    'USER',     'PASSWORD', 'A'),
    ('USER0001', 'FIRST',    'USER',     'PASSWORD', 'U'),
    ('USER0002', 'SECOND',   'USER',     'PASSWORD', 'U'),
    ('USER0003', 'THIRD',    'USER',     'PASSWORD', 'U'),
    ('USER0004', 'FOURTH',   'USER',     'PASSWORD', 'U'),
    ('ADMIN002', 'SECOND',   'ADMIN',    'PASSWORD', 'A');
