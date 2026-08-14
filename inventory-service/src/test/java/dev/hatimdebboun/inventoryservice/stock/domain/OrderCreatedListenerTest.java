package dev.hatimdebboun.inventoryservice.stock.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

// Use Mockito extension for this test
@ExtendWith(MockitoExtension.class)
class OrderCreatedListenerTest {

    // @Mock to create a test double
    @Mock
    private StockReservationService stockReservationService;

    // @InjectMocks to create a real instance of the class under test
    @InjectMocks
    private OrderCreatedListener orderCreatedListener;

    @Test
    void handle_delegatesEventToStockReservationService() {
        // --- Arrange ---
        // Create a test event representing a new order
        OrderCreatedEvent event = new OrderCreatedEvent(
                1L,
                List.of(new OrderCreatedEvent.OrderItemEvent(10L, 3L))
        );

        // --- Act ---
        // Call the method under test
        orderCreatedListener.handle(event);

        // --- Assert ---
        // Initialize ArgumentCaptor to inspect the exact OrderCreatedEvent passed to the service
        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);

        // Verify the service method was called and capture the argument
        verify(stockReservationService).evaluateOrder(captor.capture());

        // Extract the value and assert internal properties
        assertThat(captor.getValue().orderId()).isEqualTo(1L);
        assertThat(captor.getValue().items()).hasSize(1);
        assertThat(captor.getValue().items().get(0).productId()).isEqualTo(10L);
    }
}