package com.fastfood.auth;

import com.fastfood.auth.dto.LoginRequest;
import com.fastfood.auth.dto.RegisterRequest;
import com.fastfood.common.ApiException;
import com.fastfood.common.MutableClock;
import com.fastfood.common.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private static final String IP = "203.0.113.4";
    // Générés à chaque exécution : aucun mot de passe n'est écrit en dur dans le code
    private static final String PASSWORD = randomPassword();
    private static final String WRONG_PASSWORD = randomPassword();

    private final UserRepository users = mock(UserRepository.class);
    private final ChallengeService challengeService = mock(ChallengeService.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-08T10:00:00Z"));
    private final AuthService service = new AuthService(users, encoder, mock(JwtService.class),
            new LoyaltyCodeGenerator(), challengeService, new RateLimiter(clock));

    private User sami;

    @BeforeEach
    void setUp() {
        sami = new User();
        sami.setId(1L);
        sami.setEmail("sami@test.fr");
        sami.setFirstName("Sami");
        sami.setPasswordHash(encoder.encode(PASSWORD));
        sami.setEmailVerified(true);
        when(users.findByEmail("sami@test.fr")).thenReturn(Optional.of(sami));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void theRightPasswordSendsALoginCodeInsteadOfAToken() {
        service.login(new LoginRequest(" SAMI@test.fr ", PASSWORD), IP);

        verify(challengeService).start(sami, ChallengePurpose.LOGIN);
    }

    @Test
    void anUnconfirmedAccountReceivesAConfirmationCode() {
        sami.setEmailVerified(false);

        service.login(new LoginRequest("sami@test.fr", PASSWORD), IP);

        verify(challengeService).start(sami, ChallengePurpose.VERIFY_EMAIL);
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveTheSameAnswer() {
        assertThatThrownBy(() -> service.login(new LoginRequest("sami@test.fr", WRONG_PASSWORD), IP))
                .hasMessage("E-mail ou mot de passe incorrect");
        assertThatThrownBy(() -> service.login(new LoginRequest("inconnu@test.fr", PASSWORD), IP))
                .hasMessage("E-mail ou mot de passe incorrect");
        verifyNoInteractions(challengeService);
    }

    @Test
    void theAccountIsLockedAfterFiveWrongPasswordsEvenWithTheRightOne() {
        for (int i = 0; i < AuthService.MAX_FAILED_LOGINS_PER_ACCOUNT; i++) {
            assertThatThrownBy(() -> service.login(new LoginRequest("sami@test.fr", WRONG_PASSWORD), "10.0.0.1"))
                    .isInstanceOf(ApiException.class);
        }

        assertThatThrownBy(() -> service.login(new LoginRequest("sami@test.fr", PASSWORD), "10.0.0.2"))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
        verifyNoInteractions(challengeService);

        // Débloqué une fois la période écoulée
        clock.advance(AuthService.ACCOUNT_LOCK_PERIOD);
        service.login(new LoginRequest("sami@test.fr", PASSWORD), "10.0.0.2");
        verify(challengeService).start(sami, ChallengePurpose.LOGIN);
    }

    @Test
    void registeringAConfirmedEmailIsRefused() {
        assertThatThrownBy(() -> service.register(new RegisterRequest("sami@test.fr", PASSWORD, "Sami"), IP))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void anUnconfirmedRegistrationCanBeRestartedWithANewPassword() {
        sami.setEmailVerified(false);
        String oldHash = sami.getPasswordHash();

        service.register(new RegisterRequest("sami@test.fr", randomPassword(), "Sami"), IP);

        assertThat(sami.getPasswordHash()).isNotEqualTo(oldHash);
        verify(challengeService).start(sami, ChallengePurpose.VERIFY_EMAIL);
    }

    @Test
    void aNewAccountStartsUnconfirmed() {
        when(users.findByEmail("nouveau@test.fr")).thenReturn(Optional.empty());

        service.register(new RegisterRequest("nouveau@test.fr", PASSWORD, "Lina"), IP);

        verify(users).save(argThat(u -> !u.isEmailVerified() && u.getRole() == Role.CUSTOMER));
        verify(challengeService).start(any(User.class), org.mockito.ArgumentMatchers.eq(ChallengePurpose.VERIFY_EMAIL));
    }

    @Test
    void registrationsFromOneConnectionAreLimited() {
        when(users.findByEmail(anyString())).thenReturn(Optional.empty());
        for (int i = 0; i < 5; i++) {
            service.register(new RegisterRequest("client" + i + "@test.fr", PASSWORD, "Client"), IP);
        }

        assertThatThrownBy(() -> service.register(new RegisterRequest("client9@test.fr", PASSWORD, "Client"), IP))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
    }

    /** Respecte la règle du serveur : bien plus de 12 caractères, avec des symboles (! et -). */
    private static String randomPassword() {
        return "Test!" + UUID.randomUUID();
    }
}