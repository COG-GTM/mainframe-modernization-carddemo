-- Flyway migration V2: Seed users table with sample data
-- Modernized from COBOL USRSEC VSAM file initial load (DUSRSECJ.jcl)
-- Passwords are BCrypt-hashed (original plaintext: PASSWORD)
-- User types: 'U' = Regular User, 'A' = Admin (from SEC-USR-TYPE PIC X(01))

INSERT INTO users (usr_id, usr_fname, usr_lname, usr_pwd, usr_type) VALUES
('USER0001', 'User',    'One',   '$2a$10$dXJ3SW6G7P50lGmMQgel6uRXvIC6IwHyb0WUEjBA7oSNy.gLOGPe6', 'U'),
('USER0002', 'User',    'Two',   '$2a$10$dXJ3SW6G7P50lGmMQgel6uRXvIC6IwHyb0WUEjBA7oSNy.gLOGPe6', 'U'),
('USER0003', 'User',    'Three', '$2a$10$dXJ3SW6G7P50lGmMQgel6uRXvIC6IwHyb0WUEjBA7oSNy.gLOGPe6', 'U'),
('ADMIN001', 'Admin',   'One',   '$2a$10$dXJ3SW6G7P50lGmMQgel6uRXvIC6IwHyb0WUEjBA7oSNy.gLOGPe6', 'A'),
('ADMIN002', 'Admin',   'Two',   '$2a$10$dXJ3SW6G7P50lGmMQgel6uRXvIC6IwHyb0WUEjBA7oSNy.gLOGPe6', 'A');
