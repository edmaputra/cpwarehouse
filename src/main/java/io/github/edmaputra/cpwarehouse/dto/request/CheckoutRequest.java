package io.github.edmaputra.cpwarehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for checkout request.
 * Initiates checkout by checking availability and reserving stock.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotNull(message = "Item ID is required")
    @Size(max = 50, message = "Item ID must not exceed 50 characters")
    private String itemId;

    @Size(max = 50, message = "Variant ID must not exceed 50 characters")
    private String variantId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Customer ID is required")
    @Size(max = 100, message = "Customer ID must not exceed 100 characters")
    private String customerId;

    @Size(max = 100, message = "Checkout reference must not exceed 100 characters")
    private String checkoutReference;
}
