-- ============================================================
--  V2 — Tablas del schema auth
--  VARCHAR en lugar de CITEXT (normalización en la app)
--  gen_random_uuid() nativa, sin extensiones
-- ============================================================

CREATE TABLE IF NOT EXISTS auth.modules (
                                            id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    description  TEXT,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS auth.roles (
                                          id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    description  TEXT,
    is_system    BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

DROP TRIGGER IF EXISTS trg_roles_updated_at ON auth.roles;
CREATE TRIGGER trg_roles_updated_at BEFORE UPDATE ON auth.roles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS auth.permissions (
                                                id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id    UUID         NOT NULL REFERENCES auth.modules(id) ON DELETE RESTRICT,
    name         VARCHAR(200) UNIQUE NOT NULL,
    action       VARCHAR(50)  NOT NULL,
    display_name VARCHAR(250) NOT NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );
CREATE INDEX IF NOT EXISTS idx_permissions_module_id ON auth.permissions(module_id);

CREATE TABLE IF NOT EXISTS auth.role_permissions (
                                                     id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id       UUID        NOT NULL REFERENCES auth.roles(id)       ON DELETE CASCADE,
    permission_id UUID        NOT NULL REFERENCES auth.permissions(id)  ON DELETE CASCADE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_role_permission UNIQUE (role_id, permission_id)
    );
CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id ON auth.role_permissions(role_id);

-- Email como VARCHAR con índice unique funcional (case-insensitive vía lower())
CREATE TABLE IF NOT EXISTS auth.users (
                                          id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    username      VARCHAR(50),
    password_hash TEXT         NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    is_verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
    );

-- Índices únicos funcionales: garantizan unicidad ignorando mayúsculas
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email    ON auth.users(lower(email)) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_username ON auth.users(lower(username)) WHERE username IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON auth.users(deleted_at) WHERE deleted_at IS NULL;

DROP TRIGGER IF EXISTS trg_users_updated_at ON auth.users;
CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON auth.users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS auth.user_roles (
                                               id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    role_id     UUID        NOT NULL REFERENCES auth.roles(id) ON DELETE RESTRICT,
    assigned_by UUID                 REFERENCES auth.users(id) ON DELETE SET NULL,
    expires_at  TIMESTAMPTZ,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_role UNIQUE (user_id, role_id)
    );
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON auth.user_roles(user_id);

CREATE TABLE IF NOT EXISTS auth.refresh_tokens (
                                                   id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token_hash TEXT        NOT NULL UNIQUE,
    family     UUID        NOT NULL DEFAULT gen_random_uuid(),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    used_at    TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ
    );
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON auth.refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_family  ON auth.refresh_tokens(family);

CREATE TABLE IF NOT EXISTS audit.logs (
                                          id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id      UUID,
    actor_email   TEXT,
    action        VARCHAR(150) NOT NULL,
    resource_type VARCHAR(100),
    resource_id   UUID,
    old_values    JSONB,
    new_values    JSONB,
    metadata      JSONB        NOT NULL DEFAULT '{}',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_id   ON audit.logs(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action     ON audit.logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit.logs(created_at DESC);