package com.fastfood.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Un code à 6 chiffres envoyé par e-mail et en attente de saisie.
 * Seule l'empreinte du code est stockée (BCrypt), jamais le code lui-même.
 * L'identifiant est un UUID aléatoire : impossible de deviner celui d'un autre utilisateur.
 */
@Entity
@Table(name = "auth_challenges")
@Getter
@Setter
@NoArgsConstructor
public class AuthChallenge {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengePurpose purpose;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "send_count", nullable = false)
    private int sendCount;

    @Column(name = "last_sent_at", nullable = false)
    private Instant lastSentAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    /** Verrou optimiste : deux saisies simultanées ne peuvent pas contourner la limite d'essais. */
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static AuthChallenge create(User user, ChallengePurpose purpose, String codeHash, Instant now, Instant expiresAt) {
        AuthChallenge challenge = new AuthChallenge();
        challenge.setId(UUID.randomUUID());
        challenge.setUser(user);
        challenge.setPurpose(purpose);
        challenge.setCodeHash(codeHash);
        challenge.setSendCount(1);
        challenge.setLastSentAt(now);
        challenge.setExpiresAt(expiresAt);
        challenge.setCreatedAt(now);
        return challenge;
    }

    /** Ni utilisé, ni expiré. */
    public boolean isOpen(Instant now) {
        return consumedAt == null && now.isBefore(expiresAt);
    }
}
