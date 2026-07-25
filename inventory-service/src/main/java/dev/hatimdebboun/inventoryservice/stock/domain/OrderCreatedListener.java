package dev.hatimdebboun.inventoryservice.stock.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final StockReservationService stockReservationService;

    @KafkaListener(topics = "order-created", groupId = "inventory-group")
    public void handle(OrderCreatedEvent event) {
        stockReservationService.evaluateOrder(event);
    }
}