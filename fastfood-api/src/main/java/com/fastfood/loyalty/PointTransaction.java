package com.fastfood.loyalty;

import com.fastfood.auth.User;
import com.fastfood.order.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Une ligne de l'historique des points : gain après une commande ou dépense pour un cadeau. */
@Entity
@Table(name = "point_transactions")
@Getter
@Setter
@NoArgsConstructor
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /** Positif pour un gain, négatif pour un cadeau. */
    @Column(nullable = false)
    private int delta;

    @Column(nullable = false)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public static PointTransaction earned(User user, int points, Order order) {
        PointTransaction tx = new PointTransaction();
        tx.setUser(user);
        tx.setDelta(points);
        tx.setReason("Commande n°" + order.getId());
        tx.setOrder(order);
        return tx;
    }

    public static PointTransaction redeemed(User user, Reward reward) {
        PointTransaction tx = new PointTransaction();
        tx.setUser(user);
        tx.setDelta(-reward.getCostPoints());
        tx.setReason("Cadeau : " + reward.getName());
        tx.setReward(reward);
        return tx;
    }
}
