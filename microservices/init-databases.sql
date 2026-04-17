-- Initialize separate databases for each microservice
-- This script runs automatically when the PostgreSQL container starts

CREATE DATABASE carddemo_auth;
CREATE DATABASE carddemo_accounts;
CREATE DATABASE carddemo_cards;
CREATE DATABASE carddemo_transactions;
CREATE DATABASE carddemo_users;

-- Grant privileges to the carddemo user
GRANT ALL PRIVILEGES ON DATABASE carddemo_auth TO carddemo;
GRANT ALL PRIVILEGES ON DATABASE carddemo_accounts TO carddemo;
GRANT ALL PRIVILEGES ON DATABASE carddemo_cards TO carddemo;
GRANT ALL PRIVILEGES ON DATABASE carddemo_transactions TO carddemo;
GRANT ALL PRIVILEGES ON DATABASE carddemo_users TO carddemo;
