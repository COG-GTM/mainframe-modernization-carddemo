-- V3: Add version column for optimistic locking
-- Prevents lost updates on concurrent balance modifications
-- from multiple sources (REST endpoint, RabbitMQ event listener)

ALTER TABLE accounts ADD COLUMN version BIGINT DEFAULT 0;
