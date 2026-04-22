-- Sample users migrated from the COBOL USRSEC VSAM file.
-- Default credentials from the CardDemo application:
--   Regular users: USER0001-USER0003 (type 'U')
--   Admin user:    ADMIN001 (type 'A')

INSERT INTO users (user_id, first_name, last_name, password, user_type) VALUES ('USER0001', 'John', 'Smith', 'PASSWORD', 'U');
INSERT INTO users (user_id, first_name, last_name, password, user_type) VALUES ('USER0002', 'Jane', 'Doe', 'PASSWORD', 'U');
INSERT INTO users (user_id, first_name, last_name, password, user_type) VALUES ('USER0003', 'Robert', 'Johnson', 'PASSWORD', 'U');
INSERT INTO users (user_id, first_name, last_name, password, user_type) VALUES ('ADMIN001', 'Admin', 'User', 'PASSWORD', 'A');
