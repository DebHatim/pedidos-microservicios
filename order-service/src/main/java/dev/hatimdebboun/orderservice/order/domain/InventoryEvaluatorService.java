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
        try {
            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new OrderNotFoundException(event.orderId()));

            order.setStatus("CONFIRMED".equals(event.status()) ? OrderStatus.CONFIRMED : OrderStatus.REJECTED);
            orderRepository.save(order);
        }
        catch (OrderNotFoundException ex) {
            System.err.println("CRITICAL: Received event for non-existent order: " + event.orderId());
        }

    }
}