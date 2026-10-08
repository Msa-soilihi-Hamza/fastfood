package com.fastfood.auth;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthChallengeRepository extends JpaRepository<AuthChallenge, UUID> {

    /** Charge aussi l'utilisateur : il sert après la fin de la transaction (création du jeton). */
    @EntityGraph(attributePaths = "user")
    Optional<AuthChallenge> findWithUserById(UUID id);

    /** Un seul code valable à la fois : demander un nouveau code annule les précédents. */
    @Modifying
    @Query("""
            update AuthChallenge c set c.consumedAt = :now
            where c.user.id = :userId and c.purpose = :purpose and c.consumedAt is null
            """)
    void closeOpenChallenges(@Param("userId") Long userId, @Param("purpose") ChallengePurpose purpose,
                             @Param("now") Instant now);
}
