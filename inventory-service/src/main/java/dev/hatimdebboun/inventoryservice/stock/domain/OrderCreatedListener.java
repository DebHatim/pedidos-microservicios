package dev.hatimdebboun.inventoryservice.stock.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedListener {

    private final StockReservationService stockReservationService;

    @KafkaListener(topics = "order-created", groupId = "inventory-group")
    public void handle(OrderCreatedEvent event) {
        log.info("Received order-created event: orderId={}, items={}", event.orderId(), event.items().size());
        stockReservationService.evaluateOrder(event);
    }
}