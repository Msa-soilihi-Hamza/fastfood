package com.fastfood.config;

import com.fastfood.auth.AuthService;
import com.fastfood.auth.Role;
import com.fastfood.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

/**
 * Crée le compte restaurateur de l'adresse configurée (ADMIN_EMAIL) s'il n'existe pas encore.
 * Comme la connexion demande un code reçu par e-mail, cette adresse doit être une vraie boîte mail.
 * Le menu et les cadeaux de départ sont insérés par les migrations Flyway.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository users;
    private final AuthService authService;

    @Value("${app.admin.email}")
    private String adminEmail;

    /** Vide par défaut : aucun mot de passe n'est écrit dans le code. */
    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (users.existsByEmail(adminEmail.trim().toLowerCase(Locale.ROOT))) {
            return;
        }
        if (adminPassword.isBlank()) {
            // Comme Spring Security : un mot de passe aléatoire, affiché une seule fois au premier démarrage
            String generated = generatePassword();
            authService.createVerifiedUser(adminEmail, generated, "Restaurateur", Role.RESTAURANT);
            log.warn("Compte restaurateur créé : {} / mot de passe généré : {} (notez-le, il ne sera plus affiché ; "
                    + "définissez ADMIN_PASSWORD dans .env pour le choisir)", adminEmail, generated);
            return;
        }
        authService.createVerifiedUser(adminEmail, adminPassword, "Restaurateur", Role.RESTAURANT);
        log.info("Compte restaurateur créé : {}", adminEmail);
    }

    /** 24 caractères aléatoires précédés de "Resto-" : respecte la règle (12 caractères dont un symbole). */
    private static String generatePassword() {
        byte[] bytes = new byte[18];
        new SecureRandom().nextBytes(bytes);
        return "Resto-" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
