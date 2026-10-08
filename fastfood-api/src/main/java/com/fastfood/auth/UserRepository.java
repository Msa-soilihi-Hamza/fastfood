package com.fastfood.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByLoyaltyCode(String loyaltyCode);

    boolean existsByEmail(String email);

    boolean existsByLoyaltyCode(String loyaltyCode);

    boolean existsByRole(Role role);
}
