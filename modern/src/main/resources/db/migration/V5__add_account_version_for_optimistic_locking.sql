-- Add optimistic locking version column to account table.
-- COBOL Traceability: Replaces CICS READ UPDATE record-level locking
-- which prevents concurrent modifications to the same account record.
-- Hibernate @Version will include WHERE version=? on UPDATE statements,
-- throwing OptimisticLockException if another transaction modified the
-- account concurrently (e.g., two bill payments for the same account).
ALTER TABLE account ADD COLUMN version BIGINT DEFAULT 0;
