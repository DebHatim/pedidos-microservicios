package dev.hatimdebboun.orderservice.order.domain;

import java.util.List;

public record OrderCreatedEvent(Long orderId, List<OrderItemEvent> items) {
    public record OrderItemEvent(Long productId, Long quantity) {}
}