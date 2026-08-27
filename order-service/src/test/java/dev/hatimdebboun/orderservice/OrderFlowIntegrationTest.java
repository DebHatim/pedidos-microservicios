package dev.hatimdebboun.orderservice;

import dev.hatimdebboun.orderservice.order.api.OrderDTO;
import dev.hatimdebboun.orderservice.order.domain.OrderRepository;
import dev.hatimdebboun.orderservice.order.domain.OrderService;
import dev.hatimdebboun.orderservice.order.domain.OrderStatus;
import dev.hatimdebboun.orderservice.order.domain.StockEvaluatedEvent;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderFlowIntegrationTest extends AbstractIntegrationTest {

    // Inject Spring-managed beans backed by real Testcontainers infrastructure
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void alPublicarOrderEvaluatedConfirmedLaOrdenPasaAConfirmed() {
        // --- Arrange ---
        // Create an order via service layer (persists in MySQL and triggers order-created)
        OrderDTO dto = new OrderDTO(
                new BigDecimal("99.90"),
                List.of(new OrderDTO.OrderItemRequest(1L, 2L))
        );
        Long orderId = orderService.createOrder(dto);

        // --- Act ---
        // Simulate inventory-service response by publishing "CONFIRMED" evaluation event
        StockEvaluatedEvent event = new StockEvaluatedEvent(orderId, "CONFIRMED");
        kafkaTemplate.send("order-evaluated", orderId.toString(), event);

        // --- Assert ---
        // Wait asynchronously for actual listener to consume event and update status in database
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    var order = orderRepository.findById(orderId).orElseThrow();
                    assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
                });
    }

    @Test
    void alPublicarOrderEvaluatedRejectedLaOrdenPasaARejected() {
        // --- Arrange ---
        // Create an order via service layer
        OrderDTO dto = new OrderDTO(
                new BigDecimal("50.00"),
                List.of(new OrderDTO.OrderItemRequest(2L, 1L))
        );
        Long orderId = orderService.createOrder(dto);

        // --- Act ---
        // Simulate inventory-service response by publishing "REJECTED" evaluation event
        StockEvaluatedEvent event = new StockEvaluatedEvent(orderId, "REJECTED");
        kafkaTemplate.send("order-evaluated", orderId.toString(), event);

        // --- Assert ---
        // Wait asynchronously for actual listener to consume event and update status in database
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    var order = orderRepository.findById(orderId).orElseThrow();
                    assertThat(order.getStatus()).isEqualTo(OrderStatus.REJECTED);
                });
    }
}