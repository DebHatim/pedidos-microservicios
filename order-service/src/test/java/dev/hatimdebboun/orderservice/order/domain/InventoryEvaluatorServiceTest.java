package dev.hatimdebboun.orderservice.order.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Use Mockito extension for this test
@ExtendWith(MockitoExtension.class)
class InventoryEvaluatorServiceTest {

    // @Mock to create a test double
    @Mock
    private OrderRepository orderRepository;

    // @InjectMocks to create a real instance of the class under test
    @InjectMocks
    private InventoryEvaluatorService inventoryEvaluatorService;

    // Helper method to create a pending order for testing
    private Order pendingOrder(Long id) {
        return new Order(id, new BigDecimal("50.00"), OrderStatus.PENDING, List.of(new OrderItem(1L, 2L)));
    }

    @Test
    void evaluateInventory_updatesOrderToConfirmedWhenEventIsConfirmed() {
        // --- Arrange ---
        Order order = pendingOrder(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StockEvaluatedEvent event = new StockEvaluatedEvent(1L, "CONFIRMED");

        // --- Act ---
        // Call the method under test
        inventoryEvaluatorService.evaluateInventory(event);

        // --- Assert ---
        // Capture the order saved in the repository
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        // Verify that the order status was updated to CONFIRMED
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void evaluateInventory_updatesOrderToRejectedWhenEventIsNotConfirmed() {
        // --- Arrange ---
        Order order = pendingOrder(2L);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StockEvaluatedEvent event = new StockEvaluatedEvent(2L, "REJECTED");

        // --- Act ---
        // Call the method under test
        inventoryEvaluatorService.evaluateInventory(event);

        // --- Assert ---
        // Capture the order saved in the repository
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        // Verify that the order status was updated to REJECTED
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.REJECTED);
    }

    @Test
    void evaluateInventory_treatsAnyStatusOtherThanConfirmedAsRejected() {
        // --- Arrange ---
        Order order = pendingOrder(3L);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StockEvaluatedEvent event = new StockEvaluatedEvent(3L, "UNEXPECTED_STATUS");

        // --- Act ---
        // Call the method under test
        inventoryEvaluatorService.evaluateInventory(event);

        // --- Assert ---
        // Capture the order saved in the repository
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        // Verify that any unexpected status defaults to REJECTED
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.REJECTED);
    }

    @Test
    void evaluateInventory_throwsOrderNotFoundExceptionIfOrderDoesNotExist() {
        // --- Arrange ---
        // Mock the repository to return an empty Optional (simulating not found)
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        StockEvaluatedEvent event = new StockEvaluatedEvent(99L, "CONFIRMED");

        // --- Act & Assert ---
        // Assert that calling the method throws the expected exception
        assertThatThrownBy(() -> inventoryEvaluatorService.evaluateInventory(event))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
    }
}