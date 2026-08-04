package dev.hatimdebboun.orderservice.order.domain;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Order not found with id: " + id);
    }
}