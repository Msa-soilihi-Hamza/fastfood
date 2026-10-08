package com.fastfood.auth;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Génère des codes fidélité de 6 caractères, faciles à lire et à dicter au comptoir. */
@Component
public class LoyaltyCodeGenerator {

    // Sans 0/O, 1/I/L pour éviter les confusions à l'oral
    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    public static final int LENGTH = 6;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
