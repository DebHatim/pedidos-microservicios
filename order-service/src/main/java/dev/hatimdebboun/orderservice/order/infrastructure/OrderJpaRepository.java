package dev.hatimdebboun.orderservice.order.infrastructure;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
    @EntityGraph(attributePaths = {"items"})
    List<OrderJpaEntity> findAll();
}
