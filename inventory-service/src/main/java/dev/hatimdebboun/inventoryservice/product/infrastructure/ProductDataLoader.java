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
        // Comprobar que este vacio antes de ejecutar
        if (!productRepository.findAll().isEmpty()) {
            return;
        }

        productRepository.save(new Product(null,
                "PlayStation 5 Pro",
                "Consola de nueva generación con soporte para ray tracing y resolución 8K.",
                new BigDecimal("799.99"),
                "Gaming",
                15L,
                "https://picsum.photos/seed/ps5pro/500/500"));

        productRepository.save(new Product(null,
                "iPhone 15 Pro Max",
                "Smartphone con chip A17 Pro, cámara de 48MP y pantalla ProMotion de 120Hz.",
                new BigDecimal("1219.00"),
                "Smartphones",
                22L,
                "https://picsum.photos/seed/iphone15promax/500/500"));

        productRepository.save(new Product(null,
                "Nintendo Switch 2",
                "Consola híbrida con pantalla OLED de 8 pulgadas y Joy-Con magnéticos.",
                new BigDecimal("449.50"),
                "Gaming",
                30L,
                "https://picsum.photos/seed/switch2/500/500"));

        productRepository.save(new Product(null,
                "MacBook Pro 14\" M4",
                "Portátil profesional con chip M4, 16GB de RAM unificada y pantalla Liquid Retina XDR.",
                new BigDecimal("2199.00"),
                "Laptops",
                8L,
                "https://picsum.photos/seed/macbookprom4/500/500"));

        productRepository.save(new Product(null,
                "Sony WH-1000XM6",
                "Auriculares inalámbricos con cancelación de ruido líder en su categoría.",
                new BigDecimal("399.99"),
                "Audio",
                40L,
                "https://picsum.photos/seed/sonywh1000xm6/500/500"));

        productRepository.save(new Product(null,
                "Samsung Galaxy S25 Ultra",
                "Smartphone con S Pen integrado, cámara de 200MP y pantalla Dynamic AMOLED 2X.",
                new BigDecimal("1349.00"),
                "Smartphones",
                18L,
                "https://picsum.photos/seed/galaxys25ultra/500/500"));

        productRepository.save(new Product(null,
                "LG OLED C5 55\"",
                "Televisor OLED 4K con procesador de IA y soporte para Dolby Vision.",
                new BigDecimal("1599.00"),
                "TV & Home",
                6L,
                "https://picsum.photos/seed/lgoledc5/500/500"));

        productRepository.save(new Product(null,
                "DJI Mavic 4 Pro",
                "Dron con cámara Hasselblad, sensor triple y hasta 45 minutos de autonomía.",
                new BigDecimal("2399.00"),
                "Drones",
                5L,
                "https://picsum.photos/seed/mavic4pro/500/500"));

        productRepository.save(new Product(null,
                "Logitech MX Master 4",
                "Ratón inalámbrico ergonómico con scroll magnético y multi-dispositivo.",
                new BigDecimal("119.99"),
                "Accesorios",
                50L,
                "https://picsum.photos/seed/mxmaster4/500/500"));

        productRepository.save(new Product(null,
                "Kindle Oasis 2026",
                "E-reader con pantalla de 7 pulgadas, luz ajustable y resistencia al agua.",
                new BigDecimal("279.99"),
                "Lectura",
                25L,
                "https://picsum.photos/seed/kindleoasis2026/500/500"));
    }
}