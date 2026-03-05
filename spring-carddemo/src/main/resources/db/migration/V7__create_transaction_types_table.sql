-- Transaction types reference table
CREATE TABLE transaction_types (
    type_code        VARCHAR(2)   NOT NULL,
    type_description VARCHAR(100),
    CONSTRAINT pk_transaction_types PRIMARY KEY (type_code)
);
