package com.fastfood.order.dto;

import com.fastfood.order.Order;
import com.fastfood.order.OrderItem;
import com.fastfood.order.OrderStatus;
import com.fastfood.order.ServiceMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(Long id, String customerFirstName, ServiceMode serviceMode, OrderStatus status,
                            BigDecimal total, Instant createdAt, List<Item> items) {

    public record Item(Long productId, String productName, BigDecimal unitPrice, int quantity) {

        static Item from(OrderItem i) {
            return new Item(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity());
        }
    }

    public static OrderResponse from(Order o) {
        return new OrderResponse(o.getId(), o.getCustomer().getFirstName(), o.getServiceMode(), o.getStatus(),
                o.getTotal(), o.getCreatedAt(), o.getItems().stream().map(Item::from).toList());
    }
}
