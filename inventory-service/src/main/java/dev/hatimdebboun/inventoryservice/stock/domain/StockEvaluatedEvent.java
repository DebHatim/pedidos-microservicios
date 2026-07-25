package dev.hatimdebboun.inventoryservice.stock.domain;

public record StockEvaluatedEvent(Long orderId, String status) {}