package com.fastfood.auth.dto;

import com.fastfood.auth.ChallengePurpose;

import java.util.UUID;

/**
 * Réponse quand un code vient d'être envoyé : le client doit le saisir avec /api/auth/verify.
 * L'adresse est masquée (s***@exemple.fr) pour ne pas exposer l'e-mail complet.
 */
public record ChallengeResponse(UUID challengeId, ChallengePurpose purpose, String maskedEmail,
                                long resendAvailableInSeconds) {
}
