package dev.hatimdebboun.orderservice.order.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryEvaluatorService {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "order-evaluated", groupId = "orders-group")
    public void evaluateInventory(StockEvaluatedEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalStateException("Order not found: " + event.orderId()));

        order.setStatus("CONFIRMED".equals(event.status()) ? OrderStatus.CONFIRMED : OrderStatus.REJECTED);
        orderRepository.save(order);
    }
}