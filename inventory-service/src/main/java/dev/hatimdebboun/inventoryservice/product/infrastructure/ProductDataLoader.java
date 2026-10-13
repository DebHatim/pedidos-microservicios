package dev.hatimdebboun.inventoryservice.product.infrastructure;

import dev.hatimdebboun.inventoryservice.product.domain.Product;
import dev.hatimdebboun.inventoryservice.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ProductDataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        // Check that this is empty before running
        if (!productRepository.findAll().isEmpty()) {
            return;
        }

        productRepository.save(new Product(null,
                "PlayStation 5 Pro",
                "Next-generation console with ray tracing support and 8K resolution.",
                new BigDecimal("799.99"),
                "Gaming",
                15L,
                "products/producto1.jpg"));

        productRepository.save(new Product(null,
                "iPhone 15 Pro Max",
                "Smartphone with A17 Pro chip, 48MP camera, and 120Hz ProMotion display.",
                new BigDecimal("1219.00"),
                "Smartphones",
                22L,
                "products/producto2.jpg"));

        productRepository.save(new Product(null,
                "Nintendo Switch 2",
                "Hybrid console with an 8-inch OLED display and magnetic Joy-Cons.",
                new BigDecimal("449.50"),
                "Gaming",
                30L,
                "products/producto3.jpg"));

        productRepository.save(new Product(null,
                "MacBook Pro 14\" M4",
                "Professional laptop with M4 chip, 16GB unified memory, and Liquid Retina XDR display.",
                new BigDecimal("2199.00"),
                "Laptops",
                8L,
                "products/producto4.jpg"));

        productRepository.save(new Product(null,
                "Sony WH-1000XM6",
                "Wireless headphones with class-leading noise cancellation.",
                new BigDecimal("399.99"),
                "Audio",
                40L,
                "products/producto5.jpg"));

        productRepository.save(new Product(null,
                "Samsung Galaxy S25 Ultra",
                "Smartphone with built-in S Pen, 200MP camera, and Dynamic AMOLED 2X display.",
                new BigDecimal("1349.00"),
                "Smartphones",
                18L,
                "products/producto6.jpg"));

        productRepository.save(new Product(null,
                "LG OLED C5 55\"",
                "4K OLED TV with AI processor and Dolby Vision support.",
                new BigDecimal("1599.00"),
                "TV & Home",
                6L,
                "products/producto7.jpg"));

        productRepository.save(new Product(null,
                "DJI Mavic 4 Pro",
                "Drone with Hasselblad camera, triple sensor system, and up to 45 minutes of battery life.",
                new BigDecimal("2399.00"),
                "Drones",
                5L,
                "products/producto8.jpg"));

        productRepository.save(new Product(null,
                "Logitech MX Master 4",
                "Ergonomic wireless mouse with magnetic scroll wheel and multi-device support.",
                new BigDecimal("119.99"),
                "Accessories",
                50L,
                "products/producto9.jpg"));

        productRepository.save(new Product(null,
                "Kindle Oasis 2026",
                "E-reader featuring a 7-inch display, adjustable warm light, and water resistance.",
                new BigDecimal("279.99"),
                "Reading",
                25L,
                "products/producto10.jpg"));
    }
}