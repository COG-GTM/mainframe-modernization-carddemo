-- CardDemo baseline migration (WAVE 0 scaffolding).
--
-- Intentionally empty: no domain tables exist yet. Later migration waves add
-- versioned migrations (V2__..., V3__...) here under db/migration for the tables
-- backing each COBOL copybook / VSAM file they migrate.
--
-- Schema authored here must be PostgreSQL-compatible (the `prod` profile targets
-- PostgreSQL) while also running on H2 in PostgreSQL compatibility mode for dev/test.
SELECT 1;
