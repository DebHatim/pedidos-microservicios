package dev.hatimdebboun.inventoryservice.product.domain;

import dev.hatimdebboun.inventoryservice.product.api.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Use Mockito extension for this test
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    // @Mock to create a test double
    @Mock
    private ProductRepository productRepository;

    // @InjectMocks to create a real instance of the class under test
    @InjectMocks
    private ProductService productService;

    // Product entity serving as reusable test data
    private Product product;

    // Runs automatically before each test to ensure a clean state
    @BeforeEach
    void setUp() {
        product = new Product(
                1L,
                "PlayStation 5 Pro",
                "Next-generation console",
                new BigDecimal("799.99"),
                "Gaming",
                15L,
                "https://picsum.photos/seed/ps5pro/500/500"
        );
    }

    @Test
    void getAllProducts_returnsAllMappedProducts() {
        // --- Arrange ---
        Product product2 = new Product(2L, "iPhone 15 Pro Max", "description", new BigDecimal("1219.00"), "Smartphones", 22L, "url");

        // Mock the repository to return a list of products
        when(productRepository.findAll()).thenReturn(List.of(product, product2));

        // --- Act ---
        // Call the method under test
        List<ProductResponse> result = productService.getAllProducts();

        // --- Assert ---
        // Verify the list has the correct size
        assertThat(result).hasSize(2);

        // Verify that products were mapped correctly to DTOs
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("PlayStation 5 Pro");
        assertThat(result.get(1).id()).isEqualTo(2L);
    }

    @Test
    void getProductById_returnsProductIfExists() {
        // --- Arrange ---
        // Mock the repository to return an Optional containing the product
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // --- Act ---
        // Call the method under test
        ProductResponse response = productService.getProductById(1L);

        // --- Assert ---
        // Verify the mapped DTO has the correct values
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("PlayStation 5 Pro");
        assertThat(response.stock()).isEqualTo(15L);
    }

    @Test
    void getProductById_throwsProductNotFoundExceptionIfItDoesNotExist() {
        // --- Arrange ---
        // Mock the repository to return an empty Optional (simulating not found)
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // --- Act & Assert ---
        // Assert that calling the method throws the expected exception
        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void replenishStock_addsStockAndPersistsUpdatedProduct() {
        // --- Arrange ---
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When productRepository saves a Product, return the passed argument
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Act ---
        // Call the method under test
        ProductResponse response = productService.replenishStock(1L, 10L);

        // --- Assert ---
        // Verify response stock was updated correctly (15 + 10 = 25)
        assertThat(response.stock()).isEqualTo(25L);

        // Initialize ArgumentCaptor to inspect the exact Product object passed to save()
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

        // Verify the method was called and capture the argument
        verify(productRepository).save(captor.capture());

        // Assert that the persisted product updated its internal stock
        assertThat(captor.getValue().getStock()).isEqualTo(25L);
    }

    @Test
    void replenishStock_throwsProductNotFoundExceptionIfProductDoesNotExist() {
        // --- Arrange ---
        // Mock the repository to return an empty Optional (simulating not found)
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // --- Act & Assert ---
        // Assert that calling the method throws the expected exception
        assertThatThrownBy(() -> productService.replenishStock(99L, 10L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }
}