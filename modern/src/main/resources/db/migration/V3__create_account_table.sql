-- Flyway migration V3: Create account table
-- Migrated from: ACCTDAT VSAM KSDS file (CVACT01Y.cpy — Account Master, RECLN 300)
-- Primary key: ACCT-ID PIC 9(11)

CREATE TABLE account (
    acct_id         BIGINT         NOT NULL PRIMARY KEY,
    active_status   VARCHAR(1)     NOT NULL DEFAULT 'Y',
    current_balance DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    credit_limit    DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    cash_credit_limit DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    open_date       DATE,
    expiration_date DATE,
    reissue_date    DATE,
    current_cycle_credit DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    current_cycle_debit  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    group_id        VARCHAR(10)
);

-- Seed data: sample accounts matching CardDemo ACCTDAT sample data
INSERT INTO account (acct_id, active_status, current_balance, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, group_id)
VALUES
    (80001000001, 'Y',  1500.00, 10000.00, 5000.00, '2018-03-15', '2028-03-15', '2023-03-15', 200.00,  350.00, 'GROUP001'),
    (80001000002, 'Y',  2750.50, 15000.00, 7500.00, '2019-06-20', '2029-06-20', '2024-06-20', 500.00,  125.75, 'GROUP001'),
    (80001000003, 'Y',   800.25,  8000.00, 4000.00, '2020-01-10', '2030-01-10', '2025-01-10',  75.00,  200.00, 'GROUP002'),
    (80001000004, 'N',  4200.00, 20000.00, 10000.00,'2017-11-05', '2027-11-05', '2022-11-05', 1000.00, 800.00, 'GROUP002'),
    (80001000005, 'Y',     0.00,  5000.00, 2500.00, '2021-08-22', '2031-08-22', '2026-08-22',   0.00,    0.00, 'GROUP003');
