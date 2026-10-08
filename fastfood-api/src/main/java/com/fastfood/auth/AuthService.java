package com.fastfood.auth;

import com.fastfood.auth.dto.AuthResponse;
import com.fastfood.auth.dto.ChallengeResponse;
import com.fastfood.auth.dto.LoginRequest;
import com.fastfood.auth.dto.RegisterRequest;
import com.fastfood.auth.dto.UserResponse;
import com.fastfood.common.ApiException;
import com.fastfood.common.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Authentification en deux étapes :
 * 1. inscription ou e-mail + mot de passe → un code à 6 chiffres est envoyé par e-mail ;
 * 2. saisie du code → le jeton JWT est délivré.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_LOYALTY_CODE_ATTEMPTS = 10;

    // Limites anti-abus (par adresse IP ou par compte)
    static final int MAX_FAILED_LOGINS_PER_ACCOUNT = 5;
    static final Duration ACCOUNT_LOCK_PERIOD = Duration.ofMinutes(15);
    private static final int MAX_LOGINS_PER_IP = 20;
    private static final Duration LOGIN_PERIOD = Duration.ofMinutes(15);
    private static final int MAX_REGISTRATIONS_PER_IP = 5;
    private static final Duration REGISTRATION_PERIOD = Duration.ofHours(1);
    private static final int MAX_VERIFICATIONS_PER_IP = 30;
    private static final int MAX_RESENDS_PER_IP = 10;

    private static final String TOO_MANY = "Trop de tentatives depuis votre connexion. Réessayez plus tard.";
    private static final String BAD_CREDENTIALS = "E-mail ou mot de passe incorrect";

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoyaltyCodeGenerator codeGenerator;
    private final ChallengeService challengeService;
    private final RateLimiter rateLimiter;

    /** Empreinte factice : vérifiée quand l'e-mail est inconnu, pour que la réponse prenne le même temps. */
    private String dummyHash;

    @Transactional
    public ChallengeResponse register(RegisterRequest request, String clientIp) {
        rateLimiter.hit("register-ip:" + clientIp, MAX_REGISTRATIONS_PER_IP, REGISTRATION_PERIOD, TOO_MANY);

        String email = normalize(request.email());
        Optional<User> existing = users.findByEmail(email);
        if (existing.isPresent() && existing.get().isEmailVerified()) {
            throw ApiException.conflict("Un compte existe déjà avec cet e-mail. Connectez-vous.");
        }

        // Une inscription jamais confirmée n'a pas prouvé la possession de l'adresse : on la remplace
        User user = existing.orElseGet(() -> newUser(email, Role.CUSTOMER));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        users.save(user);

        return challengeService.start(user, ChallengePurpose.VERIFY_EMAIL);
    }

    @Transactional
    public ChallengeResponse login(LoginRequest request, String clientIp) {
        rateLimiter.hit("login-ip:" + clientIp, MAX_LOGINS_PER_IP, LOGIN_PERIOD, TOO_MANY);

        String email = normalize(request.email());
        String failuresKey = "login-fail:" + email;
        // Compté par e-mail, que le compte existe ou non : le blocage ne révèle pas l'existence du compte
        rateLimiter.check(failuresKey, MAX_FAILED_LOGINS_PER_ACCOUNT, ACCOUNT_LOCK_PERIOD,
                "Trop d'échecs de connexion pour ce compte. Réessayez dans 15 minutes.");

        Optional<User> user = users.findByEmail(email);
        boolean passwordOk = passwordEncoder.matches(request.password(),
                user.map(User::getPasswordHash).orElseGet(this::dummyHash));

        if (user.isEmpty() || !passwordOk) {
            rateLimiter.record(failuresKey, ACCOUNT_LOCK_PERIOD);
            throw new ApiException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }

        rateLimiter.reset(failuresKey);
        // Compte jamais confirmé : on renvoie un code de confirmation plutôt qu'un code de connexion
        ChallengePurpose purpose = user.get().isEmailVerified() ? ChallengePurpose.LOGIN : ChallengePurpose.VERIFY_EMAIL;
        return challengeService.start(user.get(), purpose);
    }

    /** Pas de @Transactional ici : un code faux doit rester compté (voir ChallengeService.verify). */
    public AuthResponse verify(UUID challengeId, String code, String clientIp) {
        rateLimiter.hit("verify-ip:" + clientIp, MAX_VERIFICATIONS_PER_IP, LOGIN_PERIOD, TOO_MANY);
        User user = challengeService.verify(challengeId, code);
        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    public ChallengeResponse resend(UUID challengeId, String clientIp) {
        rateLimiter.hit("resend-ip:" + clientIp, MAX_RESENDS_PER_IP, REGISTRATION_PERIOD, TOO_MANY);
        return challengeService.resend(challengeId);
    }

    @Transactional(readOnly = true)
    public UserResponse me(AuthUser authUser) {
        return users.findById(authUser.id())
                .map(UserResponse::from)
                .orElseThrow(() -> ApiException.notFound("Utilisateur introuvable"));
    }

    /** Création directe d'un compte déjà vérifié (compte restaurateur de démarrage). */
    @Transactional
    public User createVerifiedUser(String email, String rawPassword, String firstName, Role role) {
        String normalizedEmail = normalize(email);
        if (users.existsByEmail(normalizedEmail)) {
            throw ApiException.conflict("Un compte existe déjà avec cet e-mail");
        }
        User user = newUser(normalizedEmail, role);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(firstName.trim());
        user.setEmailVerified(true);
        return users.save(user);
    }

    private User newUser(String email, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        user.setLoyaltyCode(uniqueLoyaltyCode());
        return user;
    }

    private String dummyHash() {
        if (dummyHash == null) {
            dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
        }
        return dummyHash;
    }

    private String uniqueLoyaltyCode() {
        for (int i = 0; i < MAX_LOYALTY_CODE_ATTEMPTS; i++) {
            String code = codeGenerator.generate();
            if (!users.existsByLoyaltyCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de générer un code fidélité unique");
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
