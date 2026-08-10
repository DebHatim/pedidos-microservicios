package dev.hatimdebboun.inventoryservice.product.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReplenishStockRequest(
        @NotNull @Positive Long stock
) {}