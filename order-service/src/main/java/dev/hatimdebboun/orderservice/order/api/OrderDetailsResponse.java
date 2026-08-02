package dev.hatimdebboun.orderservice.order.api;

import dev.hatimdebboun.orderservice.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.util.List;

public record OrderDetailsResponse(
        Long id,
        OrderStatus status,
        BigDecimal total,
        List items
) {
    public record OrderItemResponse(
            Long productId,
            Long quantity
    ) {}
}