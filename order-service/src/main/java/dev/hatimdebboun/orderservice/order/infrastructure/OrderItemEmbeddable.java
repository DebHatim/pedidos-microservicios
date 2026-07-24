// infrastructure/OrderItemEmbeddable.java
package dev.hatimdebboun.orderservice.order.infrastructure;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEmbeddable {
    private Long productId;
    private Long quantity;
}