package com.fastfood.auth;

import com.fastfood.auth.dto.ChallengeResponse;
import com.fastfood.common.ApiException;
import com.fastfood.common.MutableClock;
import com.fastfood.mail.CodeMailer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChallengeServiceTest {

    private final AuthChallengeRepository challenges = mock(AuthChallengeRepository.class);
    private final CodeMailer mailer = mock(CodeMailer.class);
    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-08T10:00:00Z"));
    private final ChallengeService service =
            new ChallengeService(challenges, new BCryptPasswordEncoder(4), mailer, clock);

    private User user;
    private AuthChallenge stored;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("sami@test.fr");
        user.setFirstName("Sami");

        when(challenges.save(any(AuthChallenge.class))).thenAnswer(inv -> {
            stored = inv.getArgument(0);
            return stored;
        });
        when(challenges.findWithUserById(any())).thenAnswer(inv -> Optional.ofNullable(stored));
    }

    @Test
    void startSendsASixDigitCodeAndMasksTheEmail() {
        ChallengeResponse response = service.start(user, ChallengePurpose.LOGIN);

        String code = sentCode();
        assertThat(code).matches("\\d{6}");
        assertThat(stored.getCodeHash()).isNotEqualTo(code); // seule l'empreinte est stockée
        assertThat(response.maskedEmail()).isEqualTo("s***@test.fr");
        verify(challenges).closeOpenChallenges(eq(1L), eq(ChallengePurpose.LOGIN), any());
    }

    @Test
    void theRightCodeVerifiesTheEmail() {
        service.start(user, ChallengePurpose.VERIFY_EMAIL);

        User verified = service.verify(stored.getId(), sentCode());

        assertThat(verified.isEmailVerified()).isTrue();
        assertThat(stored.getConsumedAt()).isNotNull();
    }

    @Test
    void aCodeCanOnlyBeUsedOnce() {
        service.start(user, ChallengePurpose.LOGIN);
        String code = sentCode();
        service.verify(stored.getId(), code);

        assertThatThrownBy(() -> service.verify(stored.getId(), code)).isInstanceOf(ApiException.class);
    }

    @Test
    void aWrongCodeIsCountedAndTheSixthTryIsRefused() {
        service.start(user, ChallengePurpose.LOGIN);
        String good = sentCode();
        String wrong = good.equals("000000") ? "111111" : "000000";

        for (int i = 1; i <= ChallengeService.MAX_ATTEMPTS; i++) {
            assertThatThrownBy(() -> service.verify(stored.getId(), wrong)).isInstanceOf(ApiException.class);
            assertThat(stored.getAttempts()).isEqualTo(i);
        }

        // Même le bon code est refusé une fois les essais épuisés
        assertThatThrownBy(() -> service.verify(stored.getId(), good))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
    }

    @Test
    void aCodeExpiresAfterTenMinutes() {
        service.start(user, ChallengePurpose.LOGIN);
        String code = sentCode();
        clock.advance(Duration.ofMinutes(10));

        assertThatThrownBy(() -> service.verify(stored.getId(), code))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("plus valable");
    }

    @Test
    void resendIsRefusedDuringTheCooldownThenSendsANewCode() {
        service.start(user, ChallengePurpose.LOGIN);
        String first = sentCode();

        assertThatThrownBy(() -> service.resend(stored.getId()))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));

        clock.advance(ChallengeService.RESEND_COOLDOWN);
        service.resend(stored.getId());

        verify(mailer, times(2)).sendCode(any(), any(), any(), any(), anyLong());
        String second = sentCode();
        if (!second.equals(first)) {
            // L'ancien code ne fonctionne plus
            assertThatThrownBy(() -> service.verify(stored.getId(), first)).isInstanceOf(ApiException.class);
        }
        assertThat(service.verify(stored.getId(), second)).isSameAs(user);
    }

    private String sentCode() {
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(mailer, atLeastOnce()).sendCode(any(), any(), any(), code.capture(), anyLong());
        return code.getValue();
    }
}
