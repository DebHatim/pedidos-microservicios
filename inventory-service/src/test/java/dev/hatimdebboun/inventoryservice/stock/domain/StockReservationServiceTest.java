package dev.hatimdebboun.inventoryservice.stock.domain;

import dev.hatimdebboun.inventoryservice.product.domain.Product;
import dev.hatimdebboun.inventoryservice.product.domain.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Use Mockito extension for this test
@ExtendWith(MockitoExtension.class)
class StockReservationServiceTest {

    // @Mock to create a test double
    @Mock
    private ProductRepository productRepository;

    // @Mock to create a test double for Kafka messaging
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    // @InjectMocks to create a real instance of the class under test
    @InjectMocks
    private StockReservationService stockReservationService;

    // Helper method serving to generate reusable test data
    private Product productWithStock(Long id, Long stock) {
        return new Product(id, "Product " + id, "description", new BigDecimal("10.00"), "Category", stock, "url");
    }

    @Test
    void evaluateOrder_confirmsAndDeductsStockWhenSufficient() {
        // --- Arrange ---
        Product product = productWithStock(1L, 10L);

        // Mock the repository to return an Optional containing the product
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When productRepository saves a Product, return the passed argument
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderCreatedEvent event = new OrderCreatedEvent(
                100L,
                List.of(new OrderCreatedEvent.OrderItemEvent(1L, 4L))
        );

        // --- Act ---
        // Call the method under test
        stockReservationService.evaluateOrder(event);

        // --- Assert ---
        // Verify the product was saved with the updated stock (10 - 4 = 6)
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getStock()).isEqualTo(6L);

        // Verify that a CONFIRMED message was sent to Kafka
        ArgumentCaptor<StockEvaluatedEvent> eventCaptor = ArgumentCaptor.forClass(StockEvaluatedEvent.class);
        verify(kafkaTemplate).send(eq("order-evaluated"), eq("100"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().status()).isEqualTo("CONFIRMED");
        assertThat(eventCaptor.getValue().orderId()).isEqualTo(100L);
    }

    @Test
    void evaluateOrder_rejectsAndDoesNotDeductStockWhenInsufficient() {
        // --- Arrange ---
        Product product = productWithStock(1L, 2L); // Only 2 in stock

        // Mock the repository to return the product
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        OrderCreatedEvent event = new OrderCreatedEvent(
                101L,
                List.of(new OrderCreatedEvent.OrderItemEvent(1L, 5L)) // Requesting 5
        );

        // --- Act ---
        // Call the method under test
        stockReservationService.evaluateOrder(event);

        // --- Assert ---
        // Verify the repository save method was never called
        verify(productRepository, never()).save(any(Product.class));

        // Verify that a REJECTED message was sent to Kafka
        ArgumentCaptor<StockEvaluatedEvent> eventCaptor = ArgumentCaptor.forClass(StockEvaluatedEvent.class);
        verify(kafkaTemplate).send(eq("order-evaluated"), eq("101"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().status()).isEqualTo("REJECTED");
    }

    @Test
    void evaluateOrder_rejectsWithoutDeductingIfSingleItemLacksStock() {
        // --- Arrange ---
        Product product1 = productWithStock(1L, 10L); // Sufficient stock
        Product product2 = productWithStock(2L, 1L);  // Insufficient stock

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));

        OrderCreatedEvent event = new OrderCreatedEvent(
                102L,
                List.of(
                        new OrderCreatedEvent.OrderItemEvent(1L, 3L), // Requesting 3
                        new OrderCreatedEvent.OrderItemEvent(2L, 5L)  // Requesting 5 (Fails here)
                )
        );

        // --- Act ---
        // Call the method under test
        stockReservationService.evaluateOrder(event);

        // --- Assert ---
        // Verify the repository save method was never called because the whole order fails
        verify(productRepository, never()).save(any(Product.class));

        // Verify that a REJECTED message was sent to Kafka
        ArgumentCaptor<StockEvaluatedEvent> eventCaptor = ArgumentCaptor.forClass(StockEvaluatedEvent.class);
        verify(kafkaTemplate).send(eq("order-evaluated"), eq("102"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().status()).isEqualTo("REJECTED");
    }

    @Test
    void evaluateOrder_rejectsWhenProductDoesNotExist() {
        // --- Arrange ---
        // Mock the repository to return an empty Optional (simulating not found)
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        OrderCreatedEvent event = new OrderCreatedEvent(
                103L,
                List.of(new OrderCreatedEvent.OrderItemEvent(99L, 1L))
        );

        // --- Act ---
        // Call the method under test
        stockReservationService.evaluateOrder(event);

        // --- Assert ---
        // Verify the repository save method was never called
        verify(productRepository, never()).save(any(Product.class));

        // Verify that a REJECTED message was sent to Kafka
        ArgumentCaptor<StockEvaluatedEvent> eventCaptor = ArgumentCaptor.forClass(StockEvaluatedEvent.class);
        verify(kafkaTemplate).send(eq("order-evaluated"), eq("103"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().status()).isEqualTo("REJECTED");
    }

    @Test
    void evaluateOrder_confirmsAndDeductsStockForMultipleItemsCorrectly() {
        // --- Arrange ---
        Product product1 = productWithStock(1L, 10L);
        Product product2 = productWithStock(2L, 8L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderCreatedEvent event = new OrderCreatedEvent(
                104L,
                List.of(
                        new OrderCreatedEvent.OrderItemEvent(1L, 3L),
                        new OrderCreatedEvent.OrderItemEvent(2L, 2L)
                )
        );

        // --- Act ---
        // Call the method under test
        stockReservationService.evaluateOrder(event);

        // --- Assert ---
        // Verify the save method was called exactly twice (once for each product)
        verify(productRepository, times(2)).save(any(Product.class));

        // Verify that a CONFIRMED message was sent to Kafka
        ArgumentCaptor<StockEvaluatedEvent> eventCaptor = ArgumentCaptor.forClass(StockEvaluatedEvent.class);
        verify(kafkaTemplate).send(eq("order-evaluated"), eq("104"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().status()).isEqualTo("CONFIRMED");
    }
}