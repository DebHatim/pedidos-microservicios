package dev.hatimdebboun.orderservice.order.infrastructure;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

    @EntityGraph(attributePaths = {"items"})
    List<OrderJpaEntity> findAll();

    // Debes añadir esta línea para solucionar el error del test
    @EntityGraph(attributePaths = {"items"})
    Optional<OrderJpaEntity> findById(Long id);
}