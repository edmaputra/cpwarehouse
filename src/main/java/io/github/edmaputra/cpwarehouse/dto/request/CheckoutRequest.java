package io.github.edmaputra.cpwarehouse.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request to initiate checkout and reserve stock")
public class CheckoutRequest {

    @NotNull(message = "Item ID is required")
    @Size(max = 50, message = "Item ID must not exceed 50 characters")
    @Schema(description = "Unique identifier of the item", example = "6748a1b2c3d4e5f678901234")
    private String itemId;

    @Size(max = 50, message = "Variant ID must not exceed 50 characters")
    @Schema(description = "Unique identifier of the variant (optional)", example = "6748a1b2c3d4e5f678901235", nullable = true)
    private String variantId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity to checkout", example = "3", minimum = "1")
    private Integer quantity;

    @NotNull(message = "Customer ID is required")
    @Size(max = 100, message = "Customer ID must not exceed 100 characters")
    @Schema(description = "Customer identifier", example = "CUST-12345")
    private String customerId;

    @Size(max = 100, message = "Checkout reference must not exceed 100 characters")
    @Schema(description = "External reference number for this checkout", example = "ORDER-2024-001", nullable = true)
    private String checkoutReference;
}
