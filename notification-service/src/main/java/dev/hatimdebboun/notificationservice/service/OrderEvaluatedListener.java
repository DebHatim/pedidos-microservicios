package dev.hatimdebboun.notificationservice.service;

import dev.hatimdebboun.notificationservice.dto.NotificationMessage;
import dev.hatimdebboun.notificationservice.dto.OrderEvaluatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEvaluatedListener {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "order-evaluated", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void handle(OrderEvaluatedEvent event) {
        log.info("Notification-service received order-evaluated: orderId={}, status={}", event.orderId(), event.status());
        String message = "CONFIRMED".equals(event.status())
                ? "Your order has been successfully confirmed. (Stock available)"
                : "Your order has not been completed. (Out of stock)";

        messagingTemplate.convertAndSend("/topic/notifications",
                new NotificationMessage(event.orderId(), message)
        );
        log.info("WebSocket message sent to /topic/notifications for orderId={}", event.orderId());
    }
}
