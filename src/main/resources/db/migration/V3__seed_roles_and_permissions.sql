-- ============================================================
--  V3 — Datos semilla: módulos, roles y permisos base
-- ============================================================

INSERT INTO auth.modules (name, display_name, description) VALUES
                                                               ('users',    'Usuarios',       'Gestión de usuarios y cuentas'),
                                                               ('roles',    'Roles',          'Gestión de roles y permisos'),
                                                               ('settings', 'Configuración',  'Configuración del sistema'),
                                                               ('audit',    'Auditoría',      'Registros de auditoría')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO auth.roles (name, display_name, description, is_system) VALUES
                                                                        ('superadmin', 'Super Administrador', 'Acceso total. Rol protegido.',     TRUE),
                                                                        ('admin',      'Administrador',       'Administración general sin delete.', TRUE),
                                                                        ('viewer',     'Solo lectura',        'Consulta sin modificación.',        FALSE)
    ON CONFLICT (name) DO NOTHING;

DO $$
DECLARE v_mod TEXT; v_mid UUID; v_action TEXT;
BEGIN
  FOREACH v_mod IN ARRAY ARRAY['users','roles','settings','audit'] LOOP
SELECT id INTO v_mid FROM auth.modules WHERE name = v_mod;
FOREACH v_action IN ARRAY ARRAY['create','read','update','delete','manage','export'] LOOP
      INSERT INTO auth.permissions (module_id, name, action, display_name)
      VALUES (v_mid, v_mod||'.'||v_action, v_action, initcap(v_action)||' '||initcap(v_mod))
      ON CONFLICT (name) DO NOTHING;
END LOOP;
END LOOP;
END;
$$;

INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM auth.roles r CROSS JOIN auth.permissions p
WHERE r.name = 'superadmin'
    ON CONFLICT DO NOTHING;

INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM auth.roles r JOIN auth.permissions p ON p.action = 'read'
WHERE r.name = 'viewer'
    ON CONFLICT DO NOTHING;

INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM auth.roles r
                           JOIN auth.permissions p ON p.action IN ('create','read','update','manage','export')
WHERE r.name = 'admin'
    ON CONFLICT DO NOTHING;