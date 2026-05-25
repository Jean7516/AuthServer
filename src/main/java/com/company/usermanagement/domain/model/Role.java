package com.company.usermanagement.domain.model;

import com.company.usermanagement.domain.exception.PermissionDeniedException;
import com.company.usermanagement.domain.valueobject.PermissionId;
import com.company.usermanagement.domain.valueobject.RoleId;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Aggregate Root que representa un Rol del sistema.
 *
 * <p>Un Rol agrupa un conjunto de {@link Permission}es y se asigna a usuarios.
 * Las reglas de negocio sobre permisos se aplican aquí, no en los services.
 *
 * <p>Los roles marcados como {@code system} no pueden ser eliminados ni desactivados
 * por flujos normales de la aplicación.
 */
public class Role {

    private final RoleId          id;
    private       String          name;
    private       String          displayName;
    private       String          description;
    private final boolean         system;       // true = protegido, no se puede eliminar
    private       boolean         active;
    private final Set<Permission> permissions;
    private final Instant         createdAt;
    private       Instant         updatedAt;

    private Role(Builder builder) {
        this.id          = builder.id;
        this.name        = builder.name;
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.system      = builder.system;
        this.active      = builder.active;
        this.permissions = new HashSet<>(builder.permissions);
        this.createdAt   = builder.createdAt;
        this.updatedAt   = builder.updatedAt;
    }

    // ─── Comportamiento ──────────────────────────────────────

    /** Agrega un permiso al rol. Idempotente: no lanza error si ya existe. */
    public void addPermission(Permission permission) {
        permissions.add(permission);
        this.updatedAt = Instant.now();
    }

    /** Elimina un permiso del rol por su ID. */
    public void removePermission(PermissionId permissionId) {
        permissions.removeIf(p -> p.getId().equals(permissionId));
        this.updatedAt = Instant.now();
    }

    /** ¿El rol tiene este permiso activo? */
    public boolean hasPermission(String permissionName) {
        return permissions.stream()
            .anyMatch(p -> p.getName().equals(permissionName) && p.isActive());
    }

    /**
     * Desactiva el rol. Lanza excepción si es un rol de sistema.
     * Al desactivarse, los usuarios que lo tengan asignado pierden sus permisos
     * derivados de este rol sin que su asignación se elimine.
     */
    public void deactivate() {
        if (this.system) {
            throw new PermissionDeniedException(
                "El rol de sistema '" + name + "' no puede desactivarse");
        }
        this.active    = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active    = true;
        this.updatedAt = Instant.now();
    }

    public void updateDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank())
            throw new IllegalArgumentException("El nombre visible del rol no puede estar vacío");
        this.displayName = displayName;
        this.updatedAt   = Instant.now();
    }

    // ─── Getters ─────────────────────────────────────────────
    public RoleId          getId()          { return id; }
    public String          getName()        { return name; }
    public String          getDisplayName() { return displayName; }
    public String          getDescription() { return description; }
    public boolean         isSystem()       { return system; }
    public boolean         isActive()       { return active; }
    public Instant         getCreatedAt()   { return createdAt; }
    public Instant         getUpdatedAt()   { return updatedAt; }

    /** Vista inmutable del conjunto de permisos. */
    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    // ─── Builder ─────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private RoleId          id          = RoleId.generate();
        private String          name;
        private String          displayName;
        private String          description = "";
        private boolean         system      = false;
        private boolean         active      = true;
        private Set<Permission> permissions = new HashSet<>();
        private Instant         createdAt   = Instant.now();
        private Instant         updatedAt   = Instant.now();

        public Builder id(RoleId id)                    { this.id          = id;          return this; }
        public Builder name(String name)                { this.name        = name;        return this; }
        public Builder displayName(String displayName)  { this.displayName = displayName; return this; }
        public Builder description(String description)  { this.description = description; return this; }
        public Builder system(boolean system)           { this.system      = system;      return this; }
        public Builder active(boolean active)           { this.active      = active;      return this; }
        public Builder permissions(Set<Permission> p)   { this.permissions = p;           return this; }
        public Builder createdAt(Instant createdAt)     { this.createdAt   = createdAt;   return this; }
        public Builder updatedAt(Instant updatedAt)     { this.updatedAt   = updatedAt;   return this; }

        public Role build() {
            if (name == null || name.isBlank())
                throw new IllegalStateException("Role.name es obligatorio");
            if (displayName == null || displayName.isBlank())
                throw new IllegalStateException("Role.displayName es obligatorio");
            return new Role(this);
        }
    }
}
