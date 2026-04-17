-- Add database sequence for atomic transaction ID generation.
-- Replaces the non-atomic SELECT MAX + increment pattern.
-- COBOL Traceability: Replaces COTRN02C's STARTBR HIGH-VALUES / READPREV
-- approach to finding the next transaction ID.
-- Starts at 11 to avoid conflicts with the 10 seed transactions in V2.
CREATE SEQUENCE IF NOT EXISTS transaction_id_seq START WITH 11 INCREMENT BY 1;
