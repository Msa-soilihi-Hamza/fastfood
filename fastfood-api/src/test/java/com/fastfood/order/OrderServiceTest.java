package com.fastfood.order;

import com.fastfood.auth.AuthUser;
import com.fastfood.auth.Role;
import com.fastfood.auth.User;
import com.fastfood.auth.UserRepository;
import com.fastfood.common.ApiException;
import com.fastfood.loyalty.LoyaltyService;
import com.fastfood.menu.Product;
import com.fastfood.menu.ProductRepository;
import com.fastfood.order.dto.CompleteOrderResponse;
import com.fastfood.order.dto.CreateOrderRequest;
import com.fastfood.order.dto.CreateOrderRequest.Line;
import com.fastfood.order.dto.OrderResponse;
import com.fastfood.restaurant.RestaurantSettings;
import com.fastfood.restaurant.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orders;
    @Mock ProductRepository products;
    @Mock UserRepository users;
    @Mock SettingsService settingsService;
    @Mock LoyaltyService loyaltyService;

    @InjectMocks OrderService service;

    private final AuthUser authUser = new AuthUser(1L, "client@test.fr", Role.CUSTOMER);
    private User customer;
    private RestaurantSettings settings;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(1L);
        customer.setFirstName("Sami");
        customer.setLoyaltyCode("K7P2QX");

        settings = new RestaurantSettings();
        settings.setTakeawayEnabled(true);
        settings.setDineInEnabled(false);
        settings.setPointsPerEuro(1);
    }

    @Test
    void placeComputesTotalAndMergesDuplicateLines() {
        when(settingsService.get()).thenReturn(settings);
        when(products.findAllById(any())).thenReturn(List.of(
                product(10L, "Burger", "8.50", true),
                product(20L, "Frites", "3.00", true)));
        when(users.getReferenceById(1L)).thenReturn(customer);
        when(orders.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = service.place(authUser, new CreateOrderRequest(ServiceMode.TAKEAWAY, List.of(
                new Line(10L, 1), new Line(20L, 2), new Line(10L, 1))));

        // 2 burgers (8,50) + 2 frites (3,00) = 23,00
        assertThat(response.total()).isEqualByComparingTo("23.00");
        assertThat(response.items()).hasSize(2);
        assertThat(response.items().getFirst().quantity()).isEqualTo(2);
        assertThat(response.status()).isEqualTo(OrderStatus.RECEIVED);
    }

    @Test
    void placeRejectsAModeTheRestaurantDoesNotOffer() {
        when(settingsService.get()).thenReturn(settings);

        assertThatThrownBy(() -> service.place(authUser,
                new CreateOrderRequest(ServiceMode.DINE_IN, List.of(new Line(10L, 1)))))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("mode");
        verify(orders, never()).save(any());
    }

    @Test
    void placeRejectsASoldOutProduct() {
        when(settingsService.get()).thenReturn(settings);
        when(products.findAllById(any())).thenReturn(List.of(product(10L, "Burger", "8.50", false)));
        when(users.getReferenceById(1L)).thenReturn(customer);

        assertThatThrownBy(() -> service.place(authUser,
                new CreateOrderRequest(ServiceMode.TAKEAWAY, List.of(new Line(10L, 1)))))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("épuisé");
    }

    @Test
    void completeRejectsTheCodeOfAnotherCustomer() {
        Order order = readyOrder();
        when(orders.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.complete(5L, "AAAAAA"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("ne correspond pas");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.READY);
        verifyNoInteractions(loyaltyService);
    }

    @Test
    void completeWithTheRightCodeCreditsPoints() {
        Order order = readyOrder();
        when(orders.findById(5L)).thenReturn(Optional.of(order));
        when(loyaltyService.creditForOrder(order)).thenReturn(8);

        // Le code est accepté même saisi en minuscules avec des espaces
        CompleteOrderResponse response = service.complete(5L, " k7p2qx ");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(response.pointsEarned()).isEqualTo(8);
    }

    @Test
    void completeIsImpossibleIfTheOrderIsNotReady() {
        Order order = readyOrder();
        order.setStatus(OrderStatus.PREPARING);
        when(orders.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.complete(5L, "K7P2QX")).isInstanceOf(ApiException.class);
        verifyNoInteractions(loyaltyService);
    }

    @Test
    void customerCannotCancelSomeoneElsesOrder() {
        Order order = readyOrder();
        order.setStatus(OrderStatus.RECEIVED);
        when(orders.findById(5L)).thenReturn(Optional.of(order));

        AuthUser intruder = new AuthUser(99L, "autre@test.fr", Role.CUSTOMER);
        assertThatThrownBy(() -> service.cancelByCustomer(intruder, 5L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("introuvable");
    }

    private Order readyOrder() {
        Order order = new Order();
        order.setId(5L);
        order.setCustomer(customer);
        order.setServiceMode(ServiceMode.TAKEAWAY);
        order.addItem(OrderItem.of(product(10L, "Burger", "8.50", true), 1));
        order.setStatus(OrderStatus.READY);
        return order;
    }

    private static Product product(Long id, String name, String price, boolean available) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(new BigDecimal(price));
        p.setCategory("Test");
        p.setAvailable(available);
        return p;
    }
}
