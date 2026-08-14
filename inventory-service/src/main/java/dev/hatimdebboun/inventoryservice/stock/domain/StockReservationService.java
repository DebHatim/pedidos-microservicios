package dev.hatimdebboun.inventoryservice.stock.domain;

import dev.hatimdebboun.inventoryservice.product.domain.Product;
import dev.hatimdebboun.inventoryservice.product.domain.ProductNotFoundException;
import dev.hatimdebboun.inventoryservice.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
// Evaluates whether an order can be confirmed based on available stock
public class StockReservationService {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Method to verify product existence and availability
    public void evaluateOrder(OrderCreatedEvent event) {
        try {
            boolean hasEnoughStock = event.items().stream().allMatch(this::hasEnoughStock);
            log.info("Order {} - enough stock: {}", event.orderId(), hasEnoughStock);

            if (hasEnoughStock) {
                event.items().forEach(this::reduceStock);
                publish(event.orderId(), "CONFIRMED");
            }
            else {
                publish(event.orderId(), "REJECTED");
            }
        } catch (ProductNotFoundException e) {
            log.error("Order {} rejected: product not found - {}", event.orderId(), e.getMessage());
            publish(event.orderId(), "REJECTED");
        }
    }

    // Method that validates that the product exists and has sufficient stock for the requested quantity
    private boolean hasEnoughStock(OrderCreatedEvent.OrderItemEvent item) {
        Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new ProductNotFoundException(item.productId()));
        return product.getStock() >= item.quantity();
    }

    // Method to deduct the product stock and retain the new value
    private void reduceStock(OrderCreatedEvent.OrderItemEvent item) {
        Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new ProductNotFoundException(item.productId()));
        product.setStock(product.getStock() - item.quantity());
        productRepository.save(product);
    }

    // Send the response to order-service
    private void publish(Long orderId, String status) {
        log.info("Posting order-evaluated: orderId={}, status={}\n", orderId, status);
        kafkaTemplate.send("order-evaluated", orderId.toString(), new StockEvaluatedEvent(orderId, status));
    }
}