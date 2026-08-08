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
// Evalua si un pedido puede confirmarse segun el stock disponible
public class StockReservationService {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Metodo para verificar la existencia del producto y si tiene stock disponible
    public void evaluateOrder(OrderCreatedEvent event) {
        try {
            boolean hasEnoughStock = event.items().stream().allMatch(this::hasEnoughStock);
            log.info("Orden {} - stock suficiente: {}", event.orderId(), hasEnoughStock);

            if (hasEnoughStock) {
                event.items().forEach(this::reduceStock);
                publish(event.orderId(), "CONFIRMED");
            }
            else {
                publish(event.orderId(), "REJECTED");
            }
        } catch (ProductNotFoundException e) {
            log.error("Orden {} rechazada: producto no encontrado - {}", event.orderId(), e.getMessage());
            publish(event.orderId(), "REJECTED");
        }
    }

    // Metodo que valida que el producto exista y que tenga stock suficiente para la cantidad solicitada
    private boolean hasEnoughStock(OrderCreatedEvent.OrderItemEvent item) {
        Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new ProductNotFoundException(item.productId()));
        return product.getStock() >= item.quantity();
    }

    // Metodo para descontar el stock del producto y persistir el nuevo valor
    private void reduceStock(OrderCreatedEvent.OrderItemEvent item) {
        Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new ProductNotFoundException(item.productId()));
        product.setStock(product.getStock() - item.quantity());
        productRepository.save(product);
    }

    // Enviar la respuesta a order-service
    private void publish(Long orderId, String status) {
        log.info("Publicando order-evaluated: orderId={}, status={}", orderId, status);
        kafkaTemplate.send("order-evaluated", orderId.toString(), new StockEvaluatedEvent(orderId, status));
    }
}