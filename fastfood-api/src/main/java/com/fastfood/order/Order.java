package com.fastfood.order;

import com.fastfood.auth.User;
import com.fastfood.common.ApiException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_mode", nullable = false)
    private ServiceMode serviceMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.RECEIVED;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    /** Verrou optimiste : deux employés ne peuvent pas changer le statut en même temps. */
    @Version
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
        total = total.add(item.lineTotal());
    }

    public void moveTo(OrderStatus next) {
        if (!status.canMoveTo(next)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Impossible de passer la commande de " + status + " à " + next);
        }
        status = next;
        updatedAt = Instant.now();
    }

    public boolean belongsTo(Long userId) {
        return customer.getId().equals(userId);
    }
}
