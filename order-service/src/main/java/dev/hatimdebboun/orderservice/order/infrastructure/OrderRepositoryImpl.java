package dev.hatimdebboun.orderservice.order.infrastructure;

import dev.hatimdebboun.orderservice.order.domain.Order;
import dev.hatimdebboun.orderservice.order.domain.OrderItem;
import dev.hatimdebboun.orderservice.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = OrderJpaEntity.builder()
                .id(order.getId())
                .total(order.getTotal())
                .status(order.getStatus())
                .items(mapToEmbeddable(order.getItems())).build();

        return toDomain(orderJpaRepository.save(entity));
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id).map(this::toDomain);
    }

    private Order toDomain(OrderJpaEntity entity) {
        return new Order(
                entity.getId(),
                entity.getTotal(),
                entity.getStatus(),
                mapToDomain(entity.getItems())
        );
    }

    private List<OrderItemEmbeddable> mapToEmbeddable(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderItemEmbeddable(item.getProductId(), item.getQuantity()))
                .toList();
    }

    private List<OrderItem> mapToDomain(List<OrderItemEmbeddable> items) {
        return items.stream()
                .map(item -> new OrderItem(item.getProductId(), item.getQuantity()))
                .toList();
    }
}
