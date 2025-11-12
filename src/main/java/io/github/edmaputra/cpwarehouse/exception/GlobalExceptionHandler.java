package io.github.edmaputra.cpwarehouse.exception;

import io.github.edmaputra.cpwarehouse.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                             HttpServletRequest request) {

        ApiResponse.ErrorDetail errorDetail = ApiResponse.ErrorDetail.builder()
                .code("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(errorDetail));
    }

    /**
     * Handle DuplicateResourceException.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(DuplicateResourceException ex,
                                                                              HttpServletRequest request) {

        ApiResponse.ErrorDetail errorDetail = ApiResponse.ErrorDetail.builder()
                .code("DUPLICATE_RESOURCE")
                .message(ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(errorDetail));
    }

    /**
     * Handle InvalidOperationException.
     */
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidOperationException(InvalidOperationException ex,
                                                                             HttpServletRequest request) {

        ApiResponse.ErrorDetail errorDetail = ApiResponse.ErrorDetail.builder()
                .code("INVALID_OPERATION")
                .message(ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(errorDetail));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStockException(InsufficientStockException ex,
                                                                              HttpServletRequest request) {

        ApiResponse.ErrorDetail errorDetail = ApiResponse.ErrorDetail.builder()
                .code("INSUFFICIENT_STOCK")
                .message(ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(errorDetail));
    }

    /**
     * Handle InvalidPaymentException.
     */
    @ExceptionHandler(InvalidPaymentException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPaymentException(InvalidPaymentException ex,
                                                                           HttpServletRequest request) {

        ApiResponse.ErrorDetail errorDetail = ApiResponse.ErrorDetail.builder()
                .code("INVALID_PAYMENT")
                .message(ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(errorDetail));
    }

    /**
     * Handle validation errors from @Valid annotation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex,
                                                                       HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse.ErrorDetail errorDetail = ApiResponse.ErrorDetail.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .details(errors)
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(errorDetail));
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex, HttpServletRequest request) {

        ApiResponse.ErrorDetail errorDetail = ApiResponse.ErrorDetail.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred: " + ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(errorDetail));
    }
}
