package com.fastfood.auth;

/** L'utilisateur connecté, reconstruit à partir du jeton JWT à chaque requête. */
public record AuthUser(Long id, String email, Role role) {
}
