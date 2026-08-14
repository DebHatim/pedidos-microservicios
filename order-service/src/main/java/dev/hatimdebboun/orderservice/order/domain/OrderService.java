package dev.hatimdebboun.orderservice.order.domain;

import dev.hatimdebboun.orderservice.order.api.OrderDTO;
import dev.hatimdebboun.orderservice.order.api.OrderDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Long createOrder(OrderDTO dto) {
        Order order = new Order();
        order.setItems(mapToOrderItems(dto.items()));
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(dto.total());

        Order saved = orderRepository.save(order);
        log.info("Order {} persisted in DB with PENDING status", saved.getId());

        OrderCreatedEvent event = new OrderCreatedEvent(saved.getId(), mapToEventItems(saved.getItems()));

        kafkaTemplate.send("order-created", saved.getId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("FAILURE to publish order-created for orderId={}", saved.getId(), ex);
                    } else {
                        log.info("order-created published OK for orderId={} in partition {}",
                                saved.getId(), result.getRecordMetadata().partition());
                    }
                });

        return saved.getId();
    }

    public List<OrderDetailsResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::mapToOrderDetailsResponse).toList();
    }

    public OrderDetailsResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        return mapToOrderDetailsResponse(order);
    }

    private OrderDetailsResponse mapToOrderDetailsResponse(Order order) {
        List<OrderDetailsResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderDetailsResponse.OrderItemResponse(item.getProductId(), item.getQuantity()))
                .toList();

        return new OrderDetailsResponse(
                order.getId(),
                order.getStatus(),
                order.getTotal(),
                items
        );
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
