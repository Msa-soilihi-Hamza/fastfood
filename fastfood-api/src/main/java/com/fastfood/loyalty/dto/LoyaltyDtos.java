package com.fastfood.loyalty.dto;

import com.fastfood.auth.User;
import com.fastfood.loyalty.PointTransaction;
import com.fastfood.loyalty.Reward;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/** Les objets échangés par l'API fidélité, regroupés car ils sont tous très courts. */
public final class LoyaltyDtos {

    private LoyaltyDtos() {
    }

    public record RewardResponse(Long id, String name, int costPoints, boolean active) {
        public static RewardResponse from(Reward r) {
            return new RewardResponse(r.getId(), r.getName(), r.getCostPoints(), r.isActive());
        }
    }

    public record RewardRequest(@NotBlank @Size(max = 100) String name,
                                @Min(1) @Max(100_000) int costPoints,
                                boolean active) {
    }

    public record TransactionResponse(int delta, String reason, Instant createdAt) {
        public static TransactionResponse from(PointTransaction t) {
            return new TransactionResponse(t.getDelta(), t.getReason(), t.getCreatedAt());
        }
    }

    /** Ce que le client voit : son code à donner au comptoir, son solde et son historique. */
    public record MyLoyaltyResponse(String loyaltyCode, int points, List<TransactionResponse> history) {
    }

    /** Ce que le restaurateur voit après avoir saisi un code client. */
    public record CustomerLookupResponse(String firstName, String loyaltyCode, int points) {
        public static CustomerLookupResponse from(User u) {
            return new CustomerLookupResponse(u.getFirstName(), u.getLoyaltyCode(), u.getPoints());
        }
    }

    public record RedeemRequest(@NotBlank String loyaltyCode, @NotNull Long rewardId) {
    }

    public record RedeemResponse(String rewardName, int pointsSpent, int customerPoints) {
    }
}
