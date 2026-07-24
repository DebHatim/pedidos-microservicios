package dev.hatimdebboun.inventoryservice.product.api;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        Long stock,
        String imageUrl
) {
}
