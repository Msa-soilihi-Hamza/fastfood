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

/**
 * Crée le compte restaurateur au premier démarrage.
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

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (users.existsByRole(Role.RESTAURANT)) {
            return;
        }
        authService.createUser(adminEmail, adminPassword, "Restaurateur", Role.RESTAURANT);
        log.info("Compte restaurateur créé : {}", adminEmail);
    }
}
