package com.fastfood.auth.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    public static final int MIN_LENGTH = 12;

    /** BCrypt ignore tout ce qui dépasse 72 octets : au-delà, deux mots de passe différents seraient égaux. */
    public static final int MAX_BYTES = 72;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        return password.codePointCount(0, password.length()) >= MIN_LENGTH
                && password.getBytes(StandardCharsets.UTF_8).length <= MAX_BYTES
                && !password.isBlank()
                && password.codePoints().anyMatch(StrongPasswordValidator::isSpecial);
    }

    /** Caractère spécial : ni lettre, ni chiffre, ni espace (! ? @ # $ % & * - _ . € …). */
    static boolean isSpecial(int codePoint) {
        return !Character.isLetterOrDigit(codePoint) && !Character.isWhitespace(codePoint);
    }
}
