package dev.hatimdebboun.orderservice.shared.exception;

import dev.hatimdebboun.orderservice.order.domain.OrderNotFoundException;
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

    // Instance of the global exception handler under test
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleOrderNotFound_returns404WithExceptionMessage() {
        // --- Arrange ---
        OrderNotFoundException ex = new OrderNotFoundException(15L);

        // --- Act ---
        // Call the handler method under test
        ResponseEntity<Map<String, String>> response = handler.handleOrderNotFound(ex);

        // --- Assert ---
        // Verify status code is 404 NOT_FOUND and body contains the error message
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Order not found with id: 15");
    }

    @Test
    void handleMethodArgumentNotValid_returns400WithFieldErrors() {
        // --- Arrange ---
        MethodParameter parameter = mock(MethodParameter.class);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "orderDTO");
        bindingResult.addError(new FieldError("orderDTO", "total", "must not be null"));
        bindingResult.addError(new FieldError("orderDTO", "items", "must not be empty"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        // --- Act ---
        // Call the handler method under test
        ResponseEntity<Map<String, String>> response = handler.handleMethodArgumentNotValid(ex);

        // --- Assert ---
        // Verify status code is 400 BAD_REQUEST and body contains field validation errors
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("total", "must not be null")
                .containsEntry("items", "must not be empty");
    }

    @Test
    void handleException_returns500WithGenericMessage() {
        // --- Arrange ---
        RuntimeException ex = new RuntimeException("boom");

        // --- Act ---
        // Call the handler method under test
        ResponseEntity<Map<String, String>> response = handler.handleException(ex);

        // --- Assert ---
        // Verify status code is 500 INTERNAL_SERVER_ERROR and body contains generic message
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("error", "An unexpected error occurred");
    }
}