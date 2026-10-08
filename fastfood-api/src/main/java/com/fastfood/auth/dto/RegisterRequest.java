package com.fastfood.auth.dto;

import com.fastfood.auth.password.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "L'e-mail est obligatoire") @Email(message = "Adresse e-mail invalide") @Size(max = 255) String email,
        @StrongPassword String password,
        @NotBlank(message = "Le prénom est obligatoire") @Size(max = 100) String firstName) {
}
