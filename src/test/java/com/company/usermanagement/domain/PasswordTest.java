package com.company.usermanagement.domain;

import com.company.usermanagement.domain.valueobject.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Password — Value Object")
class PasswordTest {

    @Test
    @DisplayName("Crea contraseña válida que cumple la política")
    void creates_valid_password() {
        Password pwd = Password.ofPlainText("Secure@123");
        assertThat(pwd.isPlainText()).isTrue();
        assertThat(pwd.isHashed()).isFalse();
    }

    @Test
    @DisplayName("toString nunca expone el valor")
    void toString_is_protected() {
        assertThat(Password.ofPlainText("Secure@123").toString())
            .isEqualTo("[PROTECTED]");
    }

    @ParameterizedTest
    @DisplayName("Rechaza contraseñas que violan la política")
    @ValueSource(strings = {
        "short1A!",        // menos de 8 chars → válido, ajustar si mínimo cambia
        "sinmayuscula1!",  // sin mayúscula
        "SINMINUSCULA1!",  // sin minúscula
        "SinNumero!",      // sin dígito
        "SinEspecial1"     // sin carácter especial
    })
    void rejects_weak_passwords(String weak) {
        assertThatThrownBy(() -> Password.ofPlainText(weak))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("No permite crear Password con hash como texto plano")
    void hash_factory_marks_as_hashed() {
        Password hashed = Password.ofHash("$2a$12$someHashValue");
        assertThat(hashed.isHashed()).isTrue();
    }
}
