package com.fastfood.auth.password;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @ParameterizedTest
    @ValueSource(strings = {"Burger-Frites2026", "motdepasselong!", "123456789012?", "café crème €uro"})
    void acceptsTwelveCharactersWithASpecialOne(String password) {
        assertThat(validator.isValid(password, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "court!",                 // trop court
            "Burger!2026",            // 11 caractères
            "motdepasselong",         // aucun caractère spécial
            "motdepasse   long",      // l'espace ne compte pas comme caractère spécial
            "            "            // que des espaces
    })
    void rejectsWeakPasswords(String password) {
        assertThat(validator.isValid(password, null)).isFalse();
    }

    @org.junit.jupiter.api.Test
    void rejectsNullAndPasswordsTooLongForBCrypt() {
        assertThat(validator.isValid(null, null)).isFalse();
        assertThat(validator.isValid("!" + "a".repeat(72), null)).isFalse();
    }
}
