package com.fastfood.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Pas de règle de complexité ici : un ancien mot de passe doit rester utilisable pour se connecter
public record LoginRequest(@NotBlank @Size(max = 255) String email, @NotBlank @Size(max = 200) String password) {
}
