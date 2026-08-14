package dev.hatimdebboun.orderservice.order.domain;

// Class of the event that inventory-service publishes as a response
public record StockEvaluatedEvent(Long orderId, String status) {}