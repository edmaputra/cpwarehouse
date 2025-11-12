package io.github.edmaputra.cpwarehouse.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper for consistent response format.
 *
 * @param <T> the type of data in the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic API response wrapper")
public class ApiResponse<T> {

    @Schema(description = "Success flag", example = "true")
    private Boolean success;
    
    @Schema(description = "Response data")
    private T data;
    
    @Schema(description = "Optional message", example = "Operation completed successfully", nullable = true)
    private String message;
    
    @Schema(description = "Error details (only present on failure)", nullable = true)
    private ErrorDetail error;

    /**
     * Create a successful response with data.
     *
     * @param data the response data
     * @param <T>  the type of data
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().success(true).data(data).build();
    }

    /**
     * Create a successful response with data and message.
     *
     * @param data    the response data
     * @param message the success message
     * @param <T>     the type of data
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder().success(true).data(data).message(message).build();
    }

    /**
     * Create an error response.
     *
     * @param error the error details
     * @param <T>   the type of data
     * @return ApiResponse with error status
     */
    public static <T> ApiResponse<T> error(ErrorDetail error) {
        return ApiResponse.<T>builder().success(false).error(error).build();
    }

    /**
     * Nested class for error details.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Error detail information")
    public static class ErrorDetail {
        @Schema(description = "Error code", example = "RESOURCE_NOT_FOUND")
        private String code;
        
        @Schema(description = "Error message", example = "Item not found with ID: 123")
        private String message;
        
        @Schema(description = "Additional error details", nullable = true)
        private Object details;
        
        @Schema(description = "Error timestamp (epoch millis)", example = "1700000000000")
        private Long timestamp;
        
        @Schema(description = "Request path where error occurred", example = "/api/items/123")
        private String path;
    }
}
