package dev.hatimdebboun.orderservice.order.domain;

import dev.hatimdebboun.orderservice.order.api.OrderDTO;

import java.util.List;

public record OrderCreatedEvent(Long orderId, List<OrderDTO.OrderItemRequest> items) {}