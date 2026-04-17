-- Seed data matching original CardDemo default credentials
-- USER0001/PASSWORD (type U - regular user)
-- ADMIN001/PASSWORD (type A - admin user)
--
-- BCrypt hashes generated for 'PASSWORD'
-- These match the original COBOL USRSEC file entries

INSERT INTO users (usr_id, usr_fname, usr_lname, usr_pwd, usr_type) VALUES
('USER0001', 'Regular', 'User', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'U');

INSERT INTO users (usr_id, usr_fname, usr_lname, usr_pwd, usr_type) VALUES
('ADMIN001', 'Admin', 'User', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'A');
