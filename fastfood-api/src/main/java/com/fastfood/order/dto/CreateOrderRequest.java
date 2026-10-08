package com.fastfood.order.dto;

import com.fastfood.order.ServiceMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotNull ServiceMode serviceMode,
        @NotEmpty(message = "La commande est vide") @Size(max = 30) List<@Valid Line> items) {

    public record Line(@NotNull Long productId, @Min(1) @Max(20) int quantity) {
    }
}
