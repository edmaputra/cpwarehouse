package io.github.edmaputra.cpwarehouse.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class ApiResponse<T> {

    private Boolean success;
    private T data;
    private String message;
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
    public static class ErrorDetail {
        private String code;
        private String message;
        private Object details;
        private Long timestamp;
        private String path;
    }
}
