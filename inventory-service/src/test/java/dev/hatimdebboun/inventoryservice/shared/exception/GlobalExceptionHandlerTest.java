package dev.hatimdebboun.inventoryservice.shared.exception;

import dev.hatimdebboun.inventoryservice.product.domain.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    // Real instance of the class under test
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleProductNotFound_returns404WithExceptionMessage() {
        // --- Arrange ---
        // Create the specific exception to be handled
        ProductNotFoundException ex = new ProductNotFoundException(7L);

        // --- Act ---
        // Call the method under test
        ResponseEntity<Map<String, String>> response = handler.handleProductNotFound(ex);

        // --- Assert ---
        // Verify the response has the correct 404 NOT_FOUND status code
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        // Verify the response body contains the correct error message
        assertThat(response.getBody()).containsEntry("error", "Product not found with id: 7");
    }

    @Test
    void handleMethodArgumentNotValid_returns400WithFieldErrors() {
        // --- Arrange ---
        // Mock method parameters and setup validation errors
        MethodParameter parameter = mock(MethodParameter.class);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "replenishStockRequest");
        bindingResult.addError(new FieldError("replenishStockRequest", "stock", "must be positive"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        // --- Act ---
        // Call the method under test
        ResponseEntity<Map<String, String>> response = handler.handleMethodArgumentNotValid(ex);

        // --- Assert ---
        // Verify the response has the correct 400 BAD_REQUEST status code
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        // Verify the response body contains the specific field error
        assertThat(response.getBody()).containsEntry("stock", "must be positive");
    }

    @Test
    void handleException_returns500WithGenericMessage() {
        // --- Arrange ---
        Exception ex = new Exception("Unexpected error");

        // --- Act ---
        // Pass the exception object to the handler method
        ResponseEntity<Map<String, String>> response = handler.handleException(ex);

        // --- Assert ---
        // Verify the response has the correct 500 INTERNAL_SERVER_ERROR status code
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        // Verify the response body contains the generic fallback error message
        assertThat(response.getBody()).containsEntry("error", "An unexpected error occurred");
    }
}