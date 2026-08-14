package dev.hatimdebboun.orderservice.order.domain;

import dev.hatimdebboun.orderservice.order.api.OrderDTO;
import dev.hatimdebboun.orderservice.order.api.OrderDetailsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Use Mockito extension for this test
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    // @Mock to create a test double
    @Mock private OrderRepository orderRepository;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    // @InjectMocks to create a real instance of the class under test
    @InjectMocks
    private OrderService orderService;

    // DTO object serving as reusable test data
    private OrderDTO orderDTO;

    // Runs automatically before each test to ensure a clean state
    @BeforeEach
    void setUp() {
        orderDTO = new OrderDTO(
                new BigDecimal("99.90"),
                List.of(new OrderDTO.OrderItemRequest(1L, 2L))
        );
    }

    // @SuppressWarnings("unchecked") suppresses compiler warnings caused by generic usage with mocks.
    @SuppressWarnings("unchecked")
    private SendResult<String, Object> mockedSendResult() {
        return mock(SendResult.class);
    }

    @Test
    void createOrder_persistsWithPendingStateAndReturnsId() {
        // --- Arrange ---
        Order savedOrder = new Order(
                1L,
                orderDTO.total(),
                OrderStatus.PENDING,
                List.of(new OrderItem(1L, 2L))
        );

        // When kafkaTemplate sends a message, return a completed future with a mocked SendResult
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(mockedSendResult()));

        // When orderRepository saves an Order, return the mocked savedOrder
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // --- Act ---
        // Call the method under test
        Long orderId = orderService.createOrder(orderDTO);

        // --- Assert ---
        // Assert that the returned ID is correct
        assertThat(orderId).isEqualTo(1L);

        // Initialize ArgumentCaptor to inspect the exact Order object passed to the save() method
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        // Verify the method was called and capture the argument at the same time
        verify(orderRepository).save(orderCaptor.capture());

        // Now we can safely extract the value and assert its internal properties to verify that
        // the order was persisted with the correct state and values
        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(capturedOrder.getTotal()).isEqualByComparingTo(orderDTO.total());
        assertThat(capturedOrder.getItems()).hasSize(1);
    }

    @Test
    void createOrder_publishesOrderCreatedEventToKafkaWithCorrectIdAndItems() {
        // --- Arrange ---
        Order savedOrder = new Order(
                42L,
                orderDTO.total(),
                OrderStatus.PENDING,
                List.of(new OrderItem(1L, 2L))
        );

        // When kafkaTemplate sends a message, return a completed future with a mocked SendResult
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(mockedSendResult()));

        // When orderRepository saves an Order, return the mocked savedOrder
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // --- Act ---
        // Call the method under test
        orderService.createOrder(orderDTO);

        // --- Assert ---
        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        // Assert that the kafkaTemplate.send() method was called with the correct topic, key, and captured event
        verify(kafkaTemplate).send(eq("order-created"), eq("42"), eventCaptor.capture());

        // Now we can safely extract the value and assert its internal properties to verify that the
        // event was published with the correct order ID and items
        OrderCreatedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.orderId()).isEqualTo(42L);
        assertThat(publishedEvent.items()).hasSize(1);
        assertThat(publishedEvent.items().getFirst().productId()).isEqualTo(1L);
        assertThat(publishedEvent.items().getFirst().quantity()).isEqualTo(2L);
    }

    @Test
    void createOrder_ifKafkaFailsOrderIsStillPersistedAndIdReturned() {
        // --- Arrange ---
        Order savedOrder = new Order(
                7L,
                orderDTO.total(),
                OrderStatus.PENDING,
                List.of(new OrderItem(1L, 2L))
        );

        // Simulate a failed Kafka operation
        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka not available"));

        // When kafkaTemplate sends a message, return the failed future
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(failedFuture);

        // When orderRepository saves an Order, return the mocked savedOrder
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // --- Act ---
        // Call the method under test
        Long orderId = orderService.createOrder(orderDTO);

        // --- Assert ---
        // Assert that the returned ID is correct even if Kafka fails
        assertThat(orderId).isEqualTo(7L);

        // Verify that the order was still saved in the database
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getAllOrders_returnsAllMappedOrders() {
        // --- Arrange ---
        Order order1 = new Order(1L, new BigDecimal("10.00"), OrderStatus.CONFIRMED, List.of(new OrderItem(1L, 1L)));
        Order order2 = new Order(2L, new BigDecimal("20.00"), OrderStatus.PENDING, List.of(new OrderItem(2L, 3L)));

        // Mock the repository to return a list of orders
        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        // --- Act ---
        // Call the method under test
        List<OrderDetailsResponse> result = orderService.getAllOrders();

        // --- Assert ---
        // Verify the list has the correct size
        assertThat(result).hasSize(2);

        // Verify that the orders were mapped correctly to DTOs
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void getOrderById_returnsOrderIfExists() {
        // --- Arrange ---
        Order order = new Order(5L, new BigDecimal("50.00"), OrderStatus.CONFIRMED, List.of(new OrderItem(1L, 1L)));

        // Mock the repository to return an Optional containing the order
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        // --- Act ---
        // Call the method under test
        OrderDetailsResponse response = orderService.getOrderById(5L);

        // --- Assert ---
        // Verify the mapped DTO has the correct values
        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.items()).hasSize(1);
    }

    @Test
    void getOrderById_throwsOrderNotFoundExceptionIfItDoesNotExist() {
        // --- Arrange ---
        // Mock the repository to return an empty Optional (simulating not found)
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // --- Act & Assert ---
        // Assert that calling the method throws the expected exception
        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
    }
}