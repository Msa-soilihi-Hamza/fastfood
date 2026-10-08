package com.fastfood.auth;

import com.fastfood.auth.dto.AuthResponse;
import com.fastfood.auth.dto.LoginRequest;
import com.fastfood.auth.dto.RegisterRequest;
import com.fastfood.auth.dto.UserResponse;
import com.fastfood.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_CODE_ATTEMPTS = 10;

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoyaltyCodeGenerator codeGenerator;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = createUser(request.email(), request.password(), request.firstName(), Role.CUSTOMER);
        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmail(normalize(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                // Même message dans les deux cas : on ne révèle pas si l'e-mail existe
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "E-mail ou mot de passe incorrect"));
        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me(AuthUser authUser) {
        return users.findById(authUser.id())
                .map(UserResponse::from)
                .orElseThrow(() -> ApiException.notFound("Utilisateur introuvable"));
    }

    @Transactional
    public User createUser(String email, String rawPassword, String firstName, Role role) {
        String normalizedEmail = normalize(email);
        if (users.existsByEmail(normalizedEmail)) {
            throw ApiException.conflict("Un compte existe déjà avec cet e-mail");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(firstName.trim());
        user.setRole(role);
        user.setLoyaltyCode(uniqueLoyaltyCode());
        return users.save(user);
    }

    private String uniqueLoyaltyCode() {
        for (int i = 0; i < MAX_CODE_ATTEMPTS; i++) {
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
