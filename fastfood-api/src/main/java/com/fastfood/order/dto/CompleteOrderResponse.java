package com.fastfood.order.dto;

public record CompleteOrderResponse(OrderResponse order, int pointsEarned, int customerPoints) {
}
