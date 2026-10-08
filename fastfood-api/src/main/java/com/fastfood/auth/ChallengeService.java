package com.fastfood.auth;

import com.fastfood.auth.dto.ChallengeResponse;
import com.fastfood.common.ApiException;
import com.fastfood.mail.CodeMailer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** Envoi et vérification des codes à 6 chiffres reçus par e-mail. */
@Service
@RequiredArgsConstructor
public class ChallengeService {

    static final Duration CODE_VALIDITY = Duration.ofMinutes(10);
    static final int MAX_ATTEMPTS = 5;
    static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    static final int MAX_SENDS = 5;

    private static final String EXPIRED = "Ce code n'est plus valable. Recommencez pour en recevoir un nouveau.";

    private final AuthChallengeRepository challenges;
    private final PasswordEncoder passwordEncoder;
    private final CodeMailer codeMailer;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    /** Crée un code, annule les précédents du même type, et l'envoie par e-mail. */
    @Transactional
    public ChallengeResponse start(User user, ChallengePurpose purpose) {
        Instant now = clock.instant();
        challenges.closeOpenChallenges(user.getId(), purpose, now);

        String code = newCode();
        AuthChallenge challenge = challenges.save(
                AuthChallenge.create(user, purpose, passwordEncoder.encode(code), now, now.plus(CODE_VALIDITY)));
        codeMailer.sendCode(user.getEmail(), user.getFirstName(), purpose, code, CODE_VALIDITY.toMinutes());
        return response(challenge, now);
    }

    @Transactional
    public ChallengeResponse resend(UUID challengeId) {
        Instant now = clock.instant();
        AuthChallenge challenge = challenges.findWithUserById(challengeId)
                .filter(c -> c.isOpen(now))
                .orElseThrow(() -> ApiException.badRequest(EXPIRED));

        Duration wait = Duration.between(now, challenge.getLastSentAt().plus(RESEND_COOLDOWN));
        if (wait.isPositive()) {
            throw ApiException.tooManyRequests(
                    "Patientez " + Math.max(1, wait.toSeconds()) + " secondes avant de redemander un code.", wait);
        }
        if (challenge.getSendCount() >= MAX_SENDS) {
            throw ApiException.tooManyRequests("Trop de codes envoyés. Recommencez depuis le début.", null);
        }

        String code = newCode();
        challenge.setCodeHash(passwordEncoder.encode(code));
        challenge.setAttempts(0);
        challenge.setSendCount(challenge.getSendCount() + 1);
        challenge.setLastSentAt(now);
        challenge.setExpiresAt(now.plus(CODE_VALIDITY));

        User user = challenge.getUser();
        codeMailer.sendCode(user.getEmail(), user.getFirstName(), challenge.getPurpose(), code, CODE_VALIDITY.toMinutes());
        return response(challenge, now);
    }

    /**
     * Vérifie le code saisi. En cas d'erreur, le nombre d'essais doit quand même être enregistré :
     * d'où noRollbackFor, sinon l'exception annulerait l'incrément et les essais seraient illimités.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public User verify(UUID challengeId, String code) {
        Instant now = clock.instant();
        AuthChallenge challenge = challenges.findWithUserById(challengeId)
                .filter(c -> c.isOpen(now))
                .orElseThrow(() -> ApiException.badRequest(EXPIRED));

        if (challenge.getAttempts() >= MAX_ATTEMPTS) {
            throw ApiException.tooManyRequests("Trop d'essais. Demandez un nouveau code.", null);
        }
        challenge.setAttempts(challenge.getAttempts() + 1);

        if (!passwordEncoder.matches(code, challenge.getCodeHash())) {
            int remaining = MAX_ATTEMPTS - challenge.getAttempts();
            throw ApiException.badRequest(remaining > 0
                    ? "Code incorrect. Il vous reste " + remaining + " essai" + (remaining > 1 ? "s" : "") + "."
                    : "Code incorrect. Demandez un nouveau code.");
        }

        challenge.setConsumedAt(now);
        User user = challenge.getUser();
        if (challenge.getPurpose() == ChallengePurpose.VERIFY_EMAIL) {
            user.setEmailVerified(true);
        }
        return user;
    }

    private String newCode() {
        return "%06d".formatted(random.nextInt(1_000_000));
    }

    private static ChallengeResponse response(AuthChallenge challenge, Instant now) {
        long cooldown = Math.max(0, Duration.between(now, challenge.getLastSentAt().plus(RESEND_COOLDOWN)).toSeconds());
        return new ChallengeResponse(challenge.getId(), challenge.getPurpose(),
                maskEmail(challenge.getUser().getEmail()), cooldown);
    }

    /** sami@test.fr → s***@test.fr */
    static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
