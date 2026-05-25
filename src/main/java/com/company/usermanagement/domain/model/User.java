package com.company.usermanagement.domain.model;

import com.company.usermanagement.domain.event.DomainEvent;
import com.company.usermanagement.domain.event.RoleAssignedEvent;
import com.company.usermanagement.domain.event.RoleRevokedEvent;
import com.company.usermanagement.domain.event.UserCreatedEvent;
import com.company.usermanagement.domain.event.UserDeletedEvent;
import com.company.usermanagement.domain.exception.RoleAlreadyAssignedException;
import com.company.usermanagement.domain.exception.UserInactiveException;
import com.company.usermanagement.domain.valueobject.Email;
import com.company.usermanagement.domain.valueobject.Password;
import com.company.usermanagement.domain.valueobject.UserId;
import com.company.usermanagement.domain.valueobject.Username;
import com.company.usermanagement.domain.service.PasswordHashingService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Aggregate Root principal del dominio. Representa un usuario del sistema.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Proteger sus invariantes de negocio (el usuario solo puede modificarse
 *       a sí mismo a través de los métodos públicos de este aggregate).</li>
 *   <li>Emitir eventos de dominio en respuesta a acciones de negocio.</li>
 *   <li>No depender de ninguna librería de infraestructura (Spring, JPA, etc.).</li>
 * </ul>
 *
 * <p>Los eventos se acumulan en {@code domainEvents} y son publicados por la
 * capa de aplicación al finalizar la transacción, no dentro del aggregate.
 */
public class User {

    // ─── Identidad y credenciales ────────────────────────────
    private final UserId   id;
    private final Email    email;
    private       Username username;
    private       Password password;

    // ─── Estado de la cuenta ─────────────────────────────────
    private       boolean  active;
    private       boolean  verified;
    private       Instant  lastLoginAt;
    private       Instant  deletedAt;    // null = activo

    // ─── Roles ───────────────────────────────────────────────
    private final Set<UserRole> userRoles;

    // ─── Auditoría ───────────────────────────────────────────
    private final Instant createdAt;
    private       Instant updatedAt;

    // ─── Eventos pendientes de publicar ──────────────────────
    private final List<DomainEvent> domainEvents;

    private User(Builder builder) {
        this.id           = builder.id;
        this.email        = builder.email;
        this.username     = builder.username;
        this.password     = builder.password;
        this.active       = builder.active;
        this.verified     = builder.verified;
        this.lastLoginAt  = builder.lastLoginAt;
        this.deletedAt    = builder.deletedAt;
        this.userRoles    = new HashSet<>(builder.userRoles);
        this.createdAt    = builder.createdAt;
        this.updatedAt    = builder.updatedAt;
        this.domainEvents = new ArrayList<>();
    }

    // ═══════════════════════════════════════════════════════════
    //  Factory Methods
    // ═══════════════════════════════════════════════════════════

    /**
     * Crea un nuevo usuario. Emite {@link UserCreatedEvent}.
     *
     * @param email    email validado (value object)
     * @param password contraseña en texto plano (aún no hasheada)
     * @param username username opcional
     */
    public static User create(Email email, Password password, Username username) {
        if (password.isHashed()) {
            throw new IllegalArgumentException(
                "Al crear un usuario se debe proporcionar la contraseña en texto plano, no el hash");
        }

        User user = User.builder()
            .id(UserId.generate())
            .email(email)
            .username(username)
            .password(password)
            .active(true)
            .verified(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        user.domainEvents.add(new UserCreatedEvent(user.id, user.email));
        return user;
    }

    // ═══════════════════════════════════════════════════════════
    //  Comportamiento de negocio
    // ═══════════════════════════════════════════════════════════

    /**
     * Asigna un rol al usuario. Valida que el usuario esté activo y que
     * no tenga ya el rol asignado (y activo).
     *
     * @param role           el rol a asignar
     * @param assignedBy     quién lo asigna (puede ser null para asignaciones del sistema)
     * @param expiresAt      fecha de expiración opcional (null = permanente)
     */
    public void assignRole(Role role, UserId assignedBy, Instant expiresAt) {
        assertActive();

        boolean alreadyHasRole = userRoles.stream()
            .anyMatch(ur -> ur.roleId().equals(role.getId()) && ur.active());
        if (alreadyHasRole) {
            throw new RoleAlreadyAssignedException(role.getName());
        }

        userRoles.add(new UserRole(role.getId(), assignedBy, expiresAt, true, Instant.now()));
        this.updatedAt = Instant.now();
        domainEvents.add(new RoleAssignedEvent(this.id, role.getId(), role.getName(), assignedBy));
    }

    /**
     * Revoca un rol. No lanza error si el usuario no tenía el rol (idempotente).
     */
    public void revokeRole(Role role) {
        assertActive();
        userRoles.stream()
            .filter(ur -> ur.roleId().equals(role.getId()))
            .findFirst()
            .ifPresent(ur -> {
                userRoles.remove(ur);
                userRoles.add(ur.deactivate());
                this.updatedAt = Instant.now();
                domainEvents.add(new RoleRevokedEvent(this.id, role.getId(), role.getName()));
            });
    }

    /** Registra el último login. */
    public void recordLogin() {
        assertActive();
        this.lastLoginAt = Instant.now();
        this.updatedAt   = Instant.now();
    }

    /** Marca la cuenta como verificada (email confirmado). */
    public void markAsVerified() {
        this.verified  = true;
        this.updatedAt = Instant.now();
    }

    /** Cambia la contraseña. Solo acepta texto plano; el hash lo hace el domain service. */
    public void changePassword(Password newPassword) {
        assertActive();
        if (newPassword.isHashed()) {
            throw new IllegalArgumentException("changePassword requiere texto plano, no un hash");
        }
        this.password  = newPassword;
        this.updatedAt = Instant.now();
    }

    /** Hashea la contraseña del usuario usando el domain service inyectado. */
    public void hashPassword(PasswordHashingService hashingService) {
        if (password.isHashed()) return;
        String hash = hashingService.hash(this.password.value());
        this.password = Password.ofHash(hash);
    }

    /** Verifica si una contraseña en texto plano coincide con el hash almacenado. */
    public boolean matchesPassword(String rawPassword, PasswordHashingService hashingService) {
        if (!password.isHashed()) {
            throw new IllegalStateException("La contraseña del usuario no está hasheada");
        }
        return hashingService.matches(rawPassword, this.password.value());
    }

    /**
     * Borrado lógico. Emite {@link UserDeletedEvent} y revoca todos los roles activos.
     * No se puede deshacer desde el dominio; requiere intervención manual en BD.
     */
    public void delete(UserId deletedByUserId) {
        assertActive();
        this.active    = false;
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
        domainEvents.add(new UserDeletedEvent(this.id, this.email, deletedByUserId));
    }

    // ─── Invariante principal ────────────────────────────────
    private void assertActive() {
        if (!this.active || this.deletedAt != null) {
            throw new UserInactiveException(this.email.value());
        }
    }

    // ─── Gestión de eventos ──────────────────────────────────
    /** Devuelve los eventos y limpia la lista (pattern: pull events). */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    // ─── Getters ─────────────────────────────────────────────
    public UserId            getId()          { return id; }
    public Email             getEmail()       { return email; }
    public Optional<Username> getUsername()   { return Optional.ofNullable(username); }
    public Password          getPassword()    { return password; }
    public boolean           isActive()       { return active; }
    public boolean           isVerified()     { return verified; }
    public Optional<Instant> getLastLoginAt() { return Optional.ofNullable(lastLoginAt); }
    public Optional<Instant> getDeletedAt()   { return Optional.ofNullable(deletedAt); }
    public Set<UserRole>     getUserRoles()   { return Collections.unmodifiableSet(userRoles); }
    public Instant           getCreatedAt()   { return createdAt; }
    public Instant           getUpdatedAt()   { return updatedAt; }
    public boolean           isDeleted()      { return deletedAt != null; }

    // ─── Builder ─────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UserId       id          = UserId.generate();
        private Email        email;
        private Username     username;
        private Password     password;
        private boolean      active      = true;
        private boolean      verified    = false;
        private Instant      lastLoginAt;
        private Instant      deletedAt;
        private Set<UserRole> userRoles  = new HashSet<>();
        private Instant      createdAt   = Instant.now();
        private Instant      updatedAt   = Instant.now();

        public Builder id(UserId id)             { this.id          = id;          return this; }
        public Builder email(Email email)         { this.email       = email;       return this; }
        public Builder username(Username u)       { this.username    = u;           return this; }
        public Builder password(Password p)       { this.password    = p;           return this; }
        public Builder active(boolean active)     { this.active      = active;      return this; }
        public Builder verified(boolean verified)  { this.verified   = verified;    return this; }
        public Builder lastLoginAt(Instant t)     { this.lastLoginAt = t;           return this; }
        public Builder deletedAt(Instant t)       { this.deletedAt   = t;           return this; }
        public Builder userRoles(Set<UserRole> r) { this.userRoles   = r;           return this; }
        public Builder createdAt(Instant t)       { this.createdAt   = t;           return this; }
        public Builder updatedAt(Instant t)       { this.updatedAt   = t;           return this; }

        public User build() {
            if (email == null)    throw new IllegalStateException("User.email es obligatorio");
            if (password == null) throw new IllegalStateException("User.password es obligatorio");
            return new User(this);
        }
    }
}
