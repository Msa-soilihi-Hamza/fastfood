package com.fastfood.auth.dto;

import com.fastfood.auth.Role;
import com.fastfood.auth.User;

public record UserResponse(Long id, String email, String firstName, Role role, String loyaltyCode, int points) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getRole(),
                user.getLoyaltyCode(), user.getPoints());
    }
}
