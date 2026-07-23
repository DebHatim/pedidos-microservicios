package dev.hatimdebboun.orderservice.order.domain;

import java.math.BigDecimal;
import java.util.List;

public class Order {
    private Long id;
    private List<OrderItem> items;
    private BigDecimal total;
    private OrderStatus status;
}