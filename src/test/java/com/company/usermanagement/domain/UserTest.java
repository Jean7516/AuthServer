package com.company.usermanagement.domain;

import com.company.usermanagement.domain.event.RoleAssignedEvent;
import com.company.usermanagement.domain.event.UserCreatedEvent;
import com.company.usermanagement.domain.exception.RoleAlreadyAssignedException;
import com.company.usermanagement.domain.exception.UserInactiveException;
import com.company.usermanagement.domain.model.Role;
import com.company.usermanagement.domain.model.User;
import com.company.usermanagement.domain.valueobject.Email;
import com.company.usermanagement.domain.valueobject.Password;
import com.company.usermanagement.domain.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User — Aggregate Root")
class UserTest {

    private User user;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        user = User.create(
            Email.of("juan@empresa.com"),
            Password.ofPlainText("Secure@123"),
            null
        );
        adminRole = Role.builder()
            .name("admin")
            .displayName("Administrador")
            .build();
    }

    @Nested
    @DisplayName("Creación")
    class Creation {

        @Test
        @DisplayName("Emite UserCreatedEvent al crearse")
        void emits_created_event() {
            var events = user.pullDomainEvents();
            assertThat(events)
                .hasSize(1)
                .first()
                .isInstanceOf(UserCreatedEvent.class);
        }

        @Test
        @DisplayName("El usuario recién creado no está verificado")
        void is_not_verified_by_default() {
            assertThat(user.isVerified()).isFalse();
        }

        @Test
        @DisplayName("El usuario recién creado está activo")
        void is_active_by_default() {
            assertThat(user.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Roles")
    class Roles {

        @Test
        @DisplayName("Asigna un rol correctamente y emite evento")
        void assigns_role_and_emits_event() {
            user.pullDomainEvents(); // limpiar evento de creación

            user.assignRole(adminRole, null, null);

            assertThat(user.getUserRoles()).hasSize(1);
            assertThat(user.pullDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(RoleAssignedEvent.class);
        }

        @Test
        @DisplayName("Lanza excepción al asignar el mismo rol dos veces")
        void throws_on_duplicate_role() {
            user.assignRole(adminRole, null, null);
            assertThatThrownBy(() -> user.assignRole(adminRole, null, null))
                .isInstanceOf(RoleAlreadyAssignedException.class);
        }
    }

    @Nested
    @DisplayName("Borrado lógico")
    class SoftDelete {

        @Test
        @DisplayName("Un usuario eliminado no puede modificarse")
        void deleted_user_cannot_be_modified() {
            UserId actor = UserId.generate();
            user.delete(actor);

            assertThatThrownBy(() -> user.assignRole(adminRole, actor, null))
                .isInstanceOf(UserInactiveException.class);
        }

        @Test
        @DisplayName("El borrado establece deletedAt y desactiva la cuenta")
        void sets_deleted_at_and_deactivates() {
            user.delete(UserId.generate());

            assertThat(user.isDeleted()).isTrue();
            assertThat(user.isActive()).isFalse();
            assertThat(user.getDeletedAt()).isPresent();
        }
    }
}
