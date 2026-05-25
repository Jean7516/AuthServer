-- ============================================================
--  V1 — Schemas y función utilitaria
--  Sin extensiones externas:
--  - gen_random_uuid() es nativa en PostgreSQL 13+
--  - citext eliminado: la normalización a minúsculas
--    se hace en el Value Object Email.java
-- ============================================================

CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS audit;

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  NEW.updated_at = NOW();
RETURN NEW;
END;
$$;