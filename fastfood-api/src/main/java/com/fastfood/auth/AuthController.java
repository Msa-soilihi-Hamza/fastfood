package com.fastfood.auth;

import com.fastfood.auth.dto.AuthResponse;
import com.fastfood.auth.dto.ChallengeResponse;
import com.fastfood.auth.dto.LoginRequest;
import com.fastfood.auth.dto.RegisterRequest;
import com.fastfood.auth.dto.ResendRequest;
import com.fastfood.auth.dto.UserResponse;
import com.fastfood.auth.dto.VerifyRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Étape 1 de l'inscription : un code de confirmation est envoyé par e-mail. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ChallengeResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        return authService.register(request, http.getRemoteAddr());
    }

    /** Étape 1 de la connexion : si le mot de passe est bon, un code est envoyé par e-mail. */
    @PostMapping("/login")
    public ChallengeResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return authService.login(request, http.getRemoteAddr());
    }

    /** Étape 2 : le code reçu par e-mail donne accès au jeton JWT. */
    @PostMapping("/verify")
    public AuthResponse verify(@Valid @RequestBody VerifyRequest request, HttpServletRequest http) {
        return authService.verify(request.challengeId(), request.code(), http.getRemoteAddr());
    }

    @PostMapping("/resend")
    public ChallengeResponse resend(@Valid @RequestBody ResendRequest request, HttpServletRequest http) {
        return authService.resend(request.challengeId(), http.getRemoteAddr());
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser user) {
        return authService.me(user);
    }
}
