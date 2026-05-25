package com.company.usermanagement.domain;

import com.company.usermanagement.domain.valueobject.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Email — Value Object")
class EmailTest {

    @Test
    @DisplayName("Crea un email válido y normaliza a minúsculas")
    void creates_and_normalizes() {
        Email email = Email.of("User@Example.COM");
        assertThat(email.value()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("Dos emails iguales (diferente case) son iguales por valor")
    void equals_is_case_insensitive() {
        assertThat(Email.of("A@B.com")).isEqualTo(Email.of("a@b.com"));
    }

    @ParameterizedTest
    @DisplayName("Rechaza emails con formato inválido")
    @ValueSource(strings = {"", "  ", "no-arroba", "falta@punto", "@sinlocal.com"})
    void rejects_invalid_emails(String invalid) {
        assertThatThrownBy(() -> Email.of(invalid))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
