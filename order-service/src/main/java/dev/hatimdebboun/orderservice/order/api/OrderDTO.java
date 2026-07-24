package dev.hatimdebboun.orderservice.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record OrderDTO(
        @NotNull @Positive BigDecimal total,
        @NotEmpty @Valid List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull Long productId,
            @NotNull @Positive Long quantity
    ) {}
}