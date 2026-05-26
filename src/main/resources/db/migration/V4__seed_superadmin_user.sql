-- ============================================================
--  V4 — Usuario superadmin inicial
--
--  Credenciales por defecto (CAMBIAR en producción):
--    email:    superadmin@empresa.com
--    password: Admin@1234
--
--  Hash BCrypt del password "Admin@1234" con cost=12.
--  Generado con: BCrypt.hashpw("Admin@1234", BCrypt.gensalt(12))
-- ============================================================

DO $$
DECLARE
  v_user_id UUID := gen_random_uuid();
  v_role_id UUID;
BEGIN

  -- Solo insertar si no existe ya un superadmin
  IF NOT EXISTS (
    SELECT 1 FROM auth.users WHERE email = 'superadmin@empresa.com'
  ) THEN

    -- 1. Crear el usuario
    INSERT INTO auth.users (
      id, email, username, password_hash,
      is_active, is_verified,
      created_at, updated_at
    ) VALUES (
      v_user_id,
      'superadmin@empresa.com',
      'superadmin',
      -- BCrypt hash de "Admin@1234" con cost=12
      '$2b$12$u7T0Vxdd1YUFcCE0ku8Rd.NdKNZ5dMKPcnXJ4zbDEyvXL7HMn0rmy',
      TRUE,
      TRUE,
      NOW(),
      NOW()
    );

    -- 2. Obtener el id del rol superadmin
    SELECT id INTO v_role_id FROM auth.roles WHERE name = 'superadmin';

    -- 3. Asignar el rol
    INSERT INTO auth.user_roles (
      id, user_id, role_id,
      is_active, created_at
    ) VALUES (
      gen_random_uuid(),
      v_user_id,
      v_role_id,
      TRUE,
      NOW()
    );

    RAISE NOTICE 'Superadmin creado: superadmin@empresa.com / Admin@1234';
  ELSE
    RAISE NOTICE 'Superadmin ya existe, saltando seed.';
  END IF;

END;
$$;
