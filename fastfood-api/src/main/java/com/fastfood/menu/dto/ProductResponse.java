package com.fastfood.menu.dto;

import com.fastfood.menu.Product;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, String description, BigDecimal price, String category,
                              boolean available) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getCategory(),
                p.isAvailable());
    }
}
