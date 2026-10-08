package com.fastfood.order.dto;

import jakarta.validation.constraints.NotBlank;

/** Le restaurateur saisit le code que le client lui donne au comptoir. */
public record CompleteOrderRequest(@NotBlank String loyaltyCode) {
}
