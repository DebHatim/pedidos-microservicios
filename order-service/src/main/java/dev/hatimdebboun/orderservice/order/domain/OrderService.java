package dev.hatimdebboun.orderservice.order.domain;

import dev.hatimdebboun.orderservice.order.api.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderDTO> kafkaTemplate;

    public void createOrder(OrderDTO dto) {
        Order order = new Order();
        order.setItems(mapToOrderItems(dto.items()));
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(dto.total());

        Order saved = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(saved.getId(), dto.items());
        kafkaTemplate.send("order-created", saved.getId().toString(), event);
    }
}
