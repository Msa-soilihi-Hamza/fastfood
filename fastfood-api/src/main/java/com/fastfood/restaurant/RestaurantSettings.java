package com.fastfood.restaurant;

import com.fastfood.order.ServiceMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Réglages du fast-food. Une seule ligne en base (id = 1), créée par la migration V2. */
@Entity
@Table(name = "restaurant_settings")
@Getter
@Setter
@NoArgsConstructor
public class RestaurantSettings {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id;

    @Column(name = "takeaway_enabled", nullable = false)
    private boolean takeawayEnabled;

    @Column(name = "dine_in_enabled", nullable = false)
    private boolean dineInEnabled;

    @Column(name = "points_per_euro", nullable = false)
    private int pointsPerEuro;

    public boolean accepts(ServiceMode mode) {
        return switch (mode) {
            case TAKEAWAY -> takeawayEnabled;
            case DINE_IN -> dineInEnabled;
        };
    }
}
