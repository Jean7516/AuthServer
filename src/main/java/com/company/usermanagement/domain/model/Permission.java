package com.company.usermanagement.domain.model;

import com.company.usermanagement.domain.valueobject.PermissionId;

/**
 * Entidad de dominio que representa un permiso granular del sistema.
 *
 * <p>Permission no es un Aggregate Root: vive dentro del Aggregate {@link Role}.
 * Se accede siempre a través del Role que lo contiene.
 *
 * <p>Convención de nombre: {@code <módulo>.<acción>} — ej: {@code users.delete}.
 */
public class Permission {

    private final PermissionId id;
    private final String       moduleName;
    private final String       name;       // ej: "users.delete"
    private final String       action;     // ej: "delete"
    private final String       displayName;
    private       boolean      active;

    private Permission(Builder builder) {
        this.id          = builder.id;
        this.moduleName  = builder.moduleName;
        this.name        = builder.name;
        this.action      = builder.action;
        this.displayName = builder.displayName;
        this.active      = builder.active;
    }

    // ─── Comportamiento ──────────────────────────────────────
    public void activate()   { this.active = true; }
    public void deactivate() { this.active = false; }

    // ─── Getters ─────────────────────────────────────────────
    public PermissionId getId()          { return id; }
    public String       getModuleName()  { return moduleName; }
    public String       getName()        { return name; }
    public String       getAction()      { return action; }
    public String       getDisplayName() { return displayName; }
    public boolean      isActive()       { return active; }

    // ─── Builder ─────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private PermissionId id          = PermissionId.generate();
        private String       moduleName;
        private String       name;
        private String       action;
        private String       displayName;
        private boolean      active      = true;

        public Builder id(PermissionId id)          { this.id          = id;          return this; }
        public Builder moduleName(String moduleName) { this.moduleName  = moduleName;  return this; }
        public Builder name(String name)             { this.name        = name;        return this; }
        public Builder action(String action)         { this.action      = action;      return this; }
        public Builder displayName(String display)   { this.displayName = display;     return this; }
        public Builder active(boolean active)        { this.active      = active;      return this; }

        public Permission build() {
            if (name == null || name.isBlank())
                throw new IllegalStateException("Permission.name es obligatorio");
            if (!name.contains("."))
                throw new IllegalStateException("Nombre de permiso inválido. Formato: modulo.accion");
            return new Permission(this);
        }
    }
}
