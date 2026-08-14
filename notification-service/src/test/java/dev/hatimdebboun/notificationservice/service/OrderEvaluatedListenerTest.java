package dev.hatimdebboun.notificationservice.service;

import dev.hatimdebboun.notificationservice.dto.NotificationMessage;
import dev.hatimdebboun.notificationservice.dto.OrderEvaluatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

// Use Mockito extension for this test
@ExtendWith(MockitoExtension.class)
class OrderEvaluatedListenerTest {

    // @Mock to create a test double
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    // @InjectMocks to create a real instance of the class under test
    @InjectMocks
    private OrderEvaluatedListener orderEvaluatedListener;

    @Test
    void handle_sendsConfirmationMessageWhenStatusIsConfirmed() {
        // --- Arrange ---
        OrderEvaluatedEvent event = new OrderEvaluatedEvent(1L, "CONFIRMED");

        // --- Act ---
        // Call the method under test
        orderEvaluatedListener.handle(event);

        // --- Assert ---
        // Initialize ArgumentCaptor to inspect the exact NotificationMessage passed to convertAndSend
        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);

        // Verify that messagingTemplate.convertAndSend was called with the correct destination and captured message
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications"), captor.capture());

        // Extract the value and assert internal properties for a confirmed order
        NotificationMessage notification = captor.getValue();
        assertThat(notification.orderId()).isEqualTo(1L);
        assertThat(notification.message()).contains("successfully confirmed");
    }

    @Test
    void handle_sendsRejectionMessageWhenStatusIsRejected() {
        // --- Arrange ---
        OrderEvaluatedEvent event = new OrderEvaluatedEvent(2L, "REJECTED");

        // --- Act ---
        // Call the method under test
        orderEvaluatedListener.handle(event);

        // --- Assert ---
        // Initialize ArgumentCaptor to inspect the message payload
        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);

        // Verify that messagingTemplate.convertAndSend was called
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications"), captor.capture());

        // Extract the value and assert internal properties for a rejected order
        NotificationMessage notification = captor.getValue();
        assertThat(notification.orderId()).isEqualTo(2L);
        assertThat(notification.message()).contains("has not been completed");
    }

    @Test
    void handle_treatsAnyStatusOtherThanConfirmedAsRejection() {
        // --- Arrange ---
        OrderEvaluatedEvent event = new OrderEvaluatedEvent(3L, "UNKNOWN_STATUS");

        // --- Act ---
        // Call the method under test
        orderEvaluatedListener.handle(event);

        // --- Assert ---
        // Initialize ArgumentCaptor to inspect the message payload
        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);

        // Verify message was sent
        verify(messagingTemplate).convertAndSend(eq("/topic/notifications"), captor.capture());

        // Verify that non-confirmed status falls back to rejection message
        assertThat(captor.getValue().message()).contains("has not been completed");
    }
}