-- Add 'posted' flag to transaction table to track which transactions
-- have been processed by the daily batch job.
-- COBOL Traceability: In the COBOL system, CBTRN02C reads from the
-- DALYTRAN daily input file (separate from the TRANSACT master).
-- This flag replaces that separation: unposted transactions are the
-- equivalent of the DALYTRAN input, and posted ones are in the master.
-- Seed transactions and system-generated ones (interest, fees) are
-- marked as already posted since they don't need batch processing.
ALTER TABLE transaction ADD COLUMN posted BOOLEAN NOT NULL DEFAULT TRUE;
