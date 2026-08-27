package dev.hatimdebboun.inventoryservice;

import dev.hatimdebboun.inventoryservice.product.domain.Product;
import dev.hatimdebboun.inventoryservice.product.domain.ProductRepository;
import dev.hatimdebboun.inventoryservice.stock.domain.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryFlowIntegrationTest extends AbstractIntegrationTest {

    // Inject Spring-managed beans backed by real Testcontainers infrastructure
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // ID of the sample product created in database before each test
    private Long productId;

    // Runs automatically before each test to populate database with a clean initial state
    @BeforeEach
    void setUp() {
        Product product = productRepository.save(new Product(
                null,
                "Producto de prueba",
                "descripcion",
                new BigDecimal("50.00"),
                "Categoria",
                20L,
                "url"
        ));
        productId = product.getId();
    }

    @Test
    void alPublicarOrderCreatedElStockSeDescuentaYSePublicaOrderEvaluated() {
        // --- Arrange ---
        // Configure a manual Kafka consumer to capture the published "order-evaluated" event
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer());

        try (var consumer = consumerFactory.createConsumer()) {
            // Subscribe test consumer to the evaluation topic
            consumer.subscribe(List.of("order-evaluated"));
            // Discard initial empty poll to ensure topic partition assignment
            KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(2));

            // Create event payload simulating order-service creation
            OrderCreatedEvent event = new OrderCreatedEvent(
                    999L,
                    List.of(new OrderCreatedEvent.OrderItemEvent(productId, 5L))
            );

            // --- Act ---
            // Publish "order-created" event to Kafka topic
            kafkaTemplate.send("order-created", "999", event);

            // --- Assert ---
            // Verify asynchronously that product stock was decremented in real database
            Awaitility.await()
                    .atMost(Duration.ofSeconds(15))
                    .untilAsserted(() -> {
                        Product updated = productRepository.findById(productId).orElseThrow();
                        assertThat(updated.getStock()).isEqualTo(15L);
                    });

            // Retrieve single published record from "order-evaluated" topic and assert payload content
            ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
                    consumer, "order-evaluated", Duration.ofSeconds(10)
            );
            assertThat(record.value()).contains("CONFIRMED");
            assertThat(record.value()).contains("999");
        }
    }
}