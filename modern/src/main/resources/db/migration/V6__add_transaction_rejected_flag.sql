-- V6: Add rejected flag to transaction table.
--
-- COBOL Traceability: Replaces the DALYREJS (daily rejects) output file
-- that CBTRN02C writes when a transaction fails validation during posting.
-- Rejected transactions are excluded from future batch runs to prevent
-- infinite retry loops.

ALTER TABLE transaction ADD COLUMN rejected BOOLEAN NOT NULL DEFAULT FALSE;
