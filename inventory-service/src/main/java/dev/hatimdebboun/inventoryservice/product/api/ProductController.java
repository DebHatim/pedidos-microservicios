package dev.hatimdebboun.inventoryservice.product.api;

import dev.hatimdebboun.inventoryservice.product.domain.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> replenishStock(@PathVariable Long id,
                                                          @Valid @RequestBody ReplenishStockRequest request) {
        return ResponseEntity.ok(productService.replenishStock(id, request.stock()));
    }
}
