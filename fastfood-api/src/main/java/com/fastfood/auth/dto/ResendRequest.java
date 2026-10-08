package com.fastfood.auth.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResendRequest(@NotNull UUID challengeId) {
}
