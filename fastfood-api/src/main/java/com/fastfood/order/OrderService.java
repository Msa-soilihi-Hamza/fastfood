package com.fastfood.order;

import com.fastfood.auth.AuthUser;
import com.fastfood.auth.UserRepository;
import com.fastfood.common.ApiException;
import com.fastfood.loyalty.LoyaltyService;
import com.fastfood.menu.Product;
import com.fastfood.menu.ProductRepository;
import com.fastfood.order.dto.CompleteOrderResponse;
import com.fastfood.order.dto.CreateOrderRequest;
import com.fastfood.order.dto.OrderResponse;
import com.fastfood.restaurant.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;
    private final SettingsService settingsService;
    private final LoyaltyService loyaltyService;

    // --- Client ---

    @Transactional
    public OrderResponse place(AuthUser authUser, CreateOrderRequest request) {
        if (!settingsService.get().accepts(request.serviceMode())) {
            throw ApiException.badRequest("Ce mode n'est pas proposé par le restaurant : " + request.serviceMode());
        }

        // Regroupe les lignes en double (même plat ajouté deux fois)
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        request.items().forEach(line -> quantities.merge(line.productId(), line.quantity(), Integer::sum));

        Map<Long, Product> found = products.findAllById(quantities.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Order order = new Order();
        order.setCustomer(users.getReferenceById(authUser.id()));
        order.setServiceMode(request.serviceMode());

        quantities.forEach((productId, quantity) -> {
            Product product = found.get(productId);
            if (product == null) {
                throw ApiException.badRequest("Plat introuvable : " + productId);
            }
            if (!product.isAvailable()) {
                throw ApiException.badRequest("Plat épuisé : " + product.getName());
            }
            order.addItem(OrderItem.of(product, quantity));
        });

        return OrderResponse.from(orders.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> mine(AuthUser authUser) {
        return orders.findByCustomerIdOrderByCreatedAtDesc(authUser.id()).stream()
                .map(OrderResponse::from).toList();
    }

    /** Le client peut annuler tant que la cuisine n'a pas commencé. */
    @Transactional
    public OrderResponse cancelByCustomer(AuthUser authUser, Long orderId) {
        Order order = get(orderId);
        if (!order.belongsTo(authUser.id())) {
            // 404 plutôt que 403 : on ne révèle pas l'existence des commandes des autres
            throw ApiException.notFound("Commande introuvable : " + orderId);
        }
        if (order.getStatus() != OrderStatus.RECEIVED) {
            throw ApiException.conflict("La commande est déjà en préparation, adressez-vous au comptoir");
        }
        order.moveTo(OrderStatus.CANCELLED);
        return OrderResponse.from(order);
    }

    // --- Restaurateur ---

    @Transactional(readOnly = true)
    public List<OrderResponse> active() {
        return orders.findByStatusInOrderByCreatedAtAsc(
                        EnumSet.of(OrderStatus.RECEIVED, OrderStatus.PREPARING, OrderStatus.READY))
                .stream().map(OrderResponse::from).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus next) {
        if (next == OrderStatus.COMPLETED) {
            throw ApiException.badRequest("Pour remettre une commande, saisissez le code du client");
        }
        Order order = get(orderId);
        order.moveTo(next);
        return OrderResponse.from(order);
    }

    /**
     * Remise au comptoir : le client a payé et donne son code.
     * Le code doit être celui du client qui a passé la commande, puis les points sont crédités.
     */
    @Transactional
    public CompleteOrderResponse complete(Long orderId, String loyaltyCode) {
        Order order = get(orderId);
        String expected = order.getCustomer().getLoyaltyCode();
        if (!expected.equals(LoyaltyService.normalizeCode(loyaltyCode))) {
            throw ApiException.badRequest("Ce code ne correspond pas au client de la commande");
        }

        order.moveTo(OrderStatus.COMPLETED);
        int points = loyaltyService.creditForOrder(order);
        return new CompleteOrderResponse(OrderResponse.from(order), points, order.getCustomer().getPoints());
    }

    private Order get(Long id) {
        return orders.findById(id).orElseThrow(() -> ApiException.notFound("Commande introuvable : " + id));
    }
}
