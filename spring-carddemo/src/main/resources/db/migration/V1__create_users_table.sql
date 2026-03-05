-- Users table derived from CSUSR01Y.cpy (SEC-USER-DATA)
CREATE TABLE users (
    user_id         VARCHAR(8)   NOT NULL,
    first_name      VARCHAR(20),
    last_name       VARCHAR(20),
    password        VARCHAR(8),
    user_type       VARCHAR(1),
    CONSTRAINT pk_users PRIMARY KEY (user_id)
);
