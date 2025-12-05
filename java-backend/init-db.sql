-- CardDemo Database Initialization Script
-- This script creates the initial schema and seed data for the CardDemo application
-- Replaces the USRSEC VSAM file structure from the mainframe

-- Create users table (replaces USRSEC VSAM file)
-- Maps to CSUSR01Y.cpy copybook structure
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(8) PRIMARY KEY,           -- SEC-USR-ID
    first_name VARCHAR(20) NOT NULL,          -- SEC-USR-FNAME
    last_name VARCHAR(20) NOT NULL,           -- SEC-USR-LNAME
    password VARCHAR(255) NOT NULL,           -- SEC-USR-PWD (hashed)
    user_type VARCHAR(1) NOT NULL,            -- SEC-USR-TYPE (A=Admin, U=User)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Create index for user type queries
CREATE INDEX IF NOT EXISTS idx_users_user_type ON users(user_type);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);

-- Insert seed data matching original mainframe test users
-- Note: Passwords are BCrypt hashed versions of 'PASSWORD'
-- Original COBOL stored plain text passwords; this is more secure

-- Admin user (ADMIN001) - routes to COADM01C in original COBOL
INSERT INTO users (user_id, first_name, last_name, password, user_type, is_active)
VALUES ('ADMIN001', 'System', 'Administrator', 
        '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHx3PLTKdHxvAuBRzLXq.Vxa', 
        'A', TRUE)
ON CONFLICT (user_id) DO NOTHING;

-- Regular user (USER0001) - routes to COMEN01C in original COBOL
INSERT INTO users (user_id, first_name, last_name, password, user_type, is_active)
VALUES ('USER0001', 'John', 'Doe', 
        '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHx3PLTKdHxvAuBRzLXq.Vxa', 
        'U', TRUE)
ON CONFLICT (user_id) DO NOTHING;

-- Additional test users
INSERT INTO users (user_id, first_name, last_name, password, user_type, is_active)
VALUES ('USER0002', 'Jane', 'Smith', 
        '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHx3PLTKdHxvAuBRzLXq.Vxa', 
        'U', TRUE)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users (user_id, first_name, last_name, password, user_type, is_active)
VALUES ('USER0003', 'Bob', 'Johnson', 
        '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHx3PLTKdHxvAuBRzLXq.Vxa', 
        'U', TRUE)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO users (user_id, first_name, last_name, password, user_type, is_active)
VALUES ('ADMIN002', 'Mary', 'Wilson', 
        '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHx3PLTKdHxvAuBRzLXq.Vxa', 
        'A', TRUE)
ON CONFLICT (user_id) DO NOTHING;

-- Grant permissions
GRANT ALL PRIVILEGES ON TABLE users TO carddemo;
