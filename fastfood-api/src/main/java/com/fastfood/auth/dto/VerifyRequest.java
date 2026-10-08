package com.fastfood.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record VerifyRequest(
        @NotNull UUID challengeId,
        @NotNull @Pattern(regexp = "\\d{6}", message = "Le code contient 6 chiffres") String code) {
}
