package com.fastfood.loyalty;

import com.fastfood.auth.User;
import com.fastfood.auth.UserRepository;
import com.fastfood.common.ApiException;
import com.fastfood.loyalty.dto.LoyaltyDtos.RedeemRequest;
import com.fastfood.loyalty.dto.LoyaltyDtos.RedeemResponse;
import com.fastfood.order.Order;
import com.fastfood.restaurant.RestaurantSettings;
import com.fastfood.restaurant.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {

    @Mock UserRepository users;
    @Mock RewardRepository rewards;
    @Mock PointTransactionRepository transactions;
    @Mock SettingsService settingsService;

    @InjectMocks LoyaltyService service;

    private User customer;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(1L);
        customer.setFirstName("Sami");
        customer.setLoyaltyCode("K7P2QX");
    }

    @Test
    void pointsAreRoundedDown() {
        assertThat(LoyaltyService.pointsFor(new BigDecimal("9.90"), 1)).isEqualTo(9);
        assertThat(LoyaltyService.pointsFor(new BigDecimal("9.90"), 2)).isEqualTo(19);
        assertThat(LoyaltyService.pointsFor(new BigDecimal("0.50"), 1)).isZero();
    }

    @Test
    void creditForOrderAddsPointsAndRecordsTheHistory() {
        Order order = order(new BigDecimal("23.00"));
        RestaurantSettings settings = new RestaurantSettings();
        settings.setPointsPerEuro(1);
        when(settingsService.get()).thenReturn(settings);

        int earned = service.creditForOrder(order);

        assertThat(earned).isEqualTo(23);
        assertThat(customer.getPoints()).isEqualTo(23);
        verify(transactions).save(any(PointTransaction.class));
    }

    @Test
    void anOrderCannotBeCreditedTwice() {
        Order order = order(new BigDecimal("23.00"));
        when(transactions.existsByOrderId(order.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.creditForOrder(order))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("déjà");
        assertThat(customer.getPoints()).isZero();
    }

    @Test
    void redeemSpendsPoints() {
        customer.setPoints(120);
        when(users.findByLoyaltyCode("K7P2QX")).thenReturn(Optional.of(customer));
        when(rewards.findById(4L)).thenReturn(Optional.of(reward(4L, "Burger offert", 100)));

        RedeemResponse response = service.redeem(new RedeemRequest("k7p2qx", 4L));

        assertThat(response.customerPoints()).isEqualTo(20);
        assertThat(customer.getPoints()).isEqualTo(20);
    }

    @Test
    void redeemFailsWithoutEnoughPoints() {
        customer.setPoints(30);
        when(users.findByLoyaltyCode("K7P2QX")).thenReturn(Optional.of(customer));
        when(rewards.findById(4L)).thenReturn(Optional.of(reward(4L, "Burger offert", 100)));

        assertThatThrownBy(() -> service.redeem(new RedeemRequest("K7P2QX", 4L)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("insuffisants");
        assertThat(customer.getPoints()).isEqualTo(30);
        verify(transactions, never()).save(any());
    }

    @Test
    void unknownCodeIsRejected() {
        when(users.findByLoyaltyCode("ZZZZZZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.lookup("zzzzzz"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("inconnu");
    }

    private Order order(BigDecimal total) {
        Order order = new Order();
        order.setId(5L);
        order.setCustomer(customer);
        order.setTotal(total);
        return order;
    }

    private static Reward reward(Long id, String name, int cost) {
        Reward r = new Reward();
        r.setId(id);
        r.setName(name);
        r.setCostPoints(cost);
        return r;
    }
}
