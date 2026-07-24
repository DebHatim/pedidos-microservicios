package dev.hatimdebboun.orderservice.order.domain;

import dev.hatimdebboun.orderservice.order.api.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void createOrder(OrderDTO dto) {
        Order order = new Order();
        order.setItems(mapToOrderItems(dto.items()));
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(dto.total());

        Order saved = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(saved.getId(), mapToEventItems(saved.getItems()));
        kafkaTemplate.send("order-created", saved.getId().toString(), event);
    }

    private List<OrderItem> mapToOrderItems(List<OrderDTO.OrderItemRequest> items) {
        return items.stream()
                .map(item -> new OrderItem(item.productId(), item.quantity()))
                .toList();
    }

    private List<OrderCreatedEvent.OrderItemEvent> mapToEventItems(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderCreatedEvent.OrderItemEvent(item.getProductId(), item.getQuantity()))
                .toList();
    }
}
