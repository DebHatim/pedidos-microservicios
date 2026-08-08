package dev.hatimdebboun.orderservice.order.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEvaluatorService {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "order-evaluated", groupId = "orders-group", containerFactory = "kafkaListenerContainerFactory")
    public void evaluateInventory(StockEvaluatedEvent event) {
        log.info("Evento order-evaluated recibido: orderId={}, status={}", event.orderId(), event.status());

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException(event.orderId()));

        order.setStatus("CONFIRMED".equals(event.status()) ? OrderStatus.CONFIRMED : OrderStatus.REJECTED);
        orderRepository.save(order);

        log.info("Orden {} actualizada a estado {}", order.getId(), order.getStatus());
    }
}