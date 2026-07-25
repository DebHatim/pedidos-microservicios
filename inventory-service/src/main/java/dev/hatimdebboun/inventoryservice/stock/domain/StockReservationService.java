package dev.hatimdebboun.inventoryservice.stock.domain;

import dev.hatimdebboun.inventoryservice.product.domain.Product;
import dev.hatimdebboun.inventoryservice.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
// Evalua si un pedido puede confirmarse segun el stock disponible
public class StockReservationService {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, StockEvaluatedEvent> kafkaTemplate;

    // Metodo para verificar la existencia del producto y si tiene stock disponible
    public void evaluateOrder(OrderCreatedEvent event) {
        boolean hasEnoughStock = event.items().stream()
                .allMatch(this::hasEnoughStock);

        if (hasEnoughStock) {
            event.items().forEach(this::reduceStock);
            publish(event.orderId(), "CONFIRMED");
        } else {
            publish(event.orderId(), "REJECTED");
        }
    }

    // Metodo que valida que el producto exista y que tenga stock suficiente para la cantidad solicitada
    private boolean hasEnoughStock(OrderCreatedEvent.OrderItemEvent item) {
        Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.productId()));
        return product.getStock() >= item.quantity();
    }

    // Metodo para descontar el stock del producto y persistir el nuevo valor
    private void reduceStock(OrderCreatedEvent.OrderItemEvent item) {
        Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.productId()));
        product.setStock(product.getStock() - item.quantity());
        productRepository.save(product);
    }

    // Enviar la respuesta a order-service
    private void publish(Long orderId, String status) {
        kafkaTemplate.send("order-evaluated", orderId.toString(), new StockEvaluatedEvent(orderId, status));
    }
}