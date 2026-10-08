package com.fastfood.auth;

import com.fastfood.common.ApiException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Code court que le client donne au comptoir pour recevoir ses points. */
    @Column(name = "loyalty_code", nullable = false, unique = true, length = 6)
    private String loyaltyCode;

    @Column(nullable = false)
    private int points;

    /** Vrai une fois le code reçu par e-mail saisi : avant, le compte ne peut pas se connecter. */
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    /** Verrou optimiste : deux mises à jour simultanées des points ne s'écrasent pas. */
    @Version
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public void addPoints(int amount) {
        this.points += amount;
    }

    public void spendPoints(int amount) {
        if (amount > points) {
            throw ApiException.badRequest("Points insuffisants : " + points + " disponibles, " + amount + " requis");
        }
        this.points -= amount;
    }
}
