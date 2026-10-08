package com.fastfood.order;

import com.fastfood.auth.AuthUser;
import com.fastfood.order.dto.CompleteOrderRequest;
import com.fastfood.order.dto.CompleteOrderResponse;
import com.fastfood.order.dto.CreateOrderRequest;
import com.fastfood.order.dto.OrderResponse;
import com.fastfood.order.dto.UpdateStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // --- Client ---

    @PostMapping("/api/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody CreateOrderRequest request) {
        return orderService.place(user, request);
    }

    @GetMapping("/api/orders/mine")
    public List<OrderResponse> mine(@AuthenticationPrincipal AuthUser user) {
        return orderService.mine(user);
    }

    @PostMapping("/api/orders/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return orderService.cancelByCustomer(user, id);
    }

    // --- Restaurateur ---

    @GetMapping("/api/admin/orders")
    public List<OrderResponse> active() {
        return orderService.active();
    }

    @PatchMapping("/api/admin/orders/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }

    @PostMapping("/api/admin/orders/{id}/complete")
    public CompleteOrderResponse complete(@PathVariable Long id, @Valid @RequestBody CompleteOrderRequest request) {
        return orderService.complete(id, request.loyaltyCode());
    }
}
