-- Sample users migrated from COBOL USRSEC VSAM file
-- Default credentials: USER0001/PASSWORD (regular user), ADMIN001/PASSWORD (admin)
INSERT INTO users (user_id, first_name, last_name, password, user_type) VALUES ('USER0001', 'John', 'Doe', 'PASSWORD', 'U');
INSERT INTO users (user_id, first_name, last_name, password, user_type) VALUES ('ADMIN001', 'Admin', 'User', 'PASSWORD', 'A');
