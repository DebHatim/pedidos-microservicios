package dev.hatimdebboun.orderservice.order.domain;

// Clase del evento que publica inventory-service como respuesta
public record StockEvaluatedEvent(Long orderId, String status) {}