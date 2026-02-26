-- ============================================================================
-- Add database sequence for thread-safe transaction ID generation.
-- Fixes the race condition in the original read-then-increment pattern
-- (migrated from COBOL STARTBR HIGH-VALUES / READPREV / ADD 1).
--
-- Start with 4 since V2 seed data inserts IDs up to 3.
-- ============================================================================

CREATE SEQUENCE tran_id_seq START WITH 4 INCREMENT BY 1;
