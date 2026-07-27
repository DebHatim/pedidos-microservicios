package dev.hatimdebboun.notificationservice.service;

import dev.hatimdebboun.notificationservice.dto.NotificationMessage;
import dev.hatimdebboun.notificationservice.dto.OrderEvaluatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEvaluatedListener {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "order-evaluated", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void handle(OrderEvaluatedEvent event) {
        String message = "CONFIRMED".equals(event.status())
                ? "Tu pedido se ha confirmado correctamente. (Stock disponible)"
                : "Tu pedido no se ha completado. (Sin stock disponible)";

        messagingTemplate.convertAndSend("/topic/notifications",
                new NotificationMessage(event.orderId(), message)
        );
    }
}
