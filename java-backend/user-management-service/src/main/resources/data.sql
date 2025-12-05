-- Initial data for User Management Service
-- This data mirrors the USRSEC VSAM file from the mainframe
-- 
-- Original mainframe users:
-- - USER0001 (Regular user)
-- - ADMIN001 (Admin user)
--
-- Passwords are BCrypt hashed. Original mainframe passwords were plain text.
-- Default password for both users: PASSWORD (hashed below)

-- BCrypt hash for 'PASSWORD': $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS8gPvjWfCyhTMC.y

INSERT INTO users (user_id, first_name, last_name, password, user_type) VALUES
('USER0001', 'John', 'Doe', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS8gPvjWfCyhTMC.y', 'USER'),
('ADMIN001', 'Admin', 'User', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS8gPvjWfCyhTMC.y', 'ADMIN');
