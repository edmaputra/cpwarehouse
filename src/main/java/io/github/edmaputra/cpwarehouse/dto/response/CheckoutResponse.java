package io.github.edmaputra.cpwarehouse.dto.response;

import io.github.edmaputra.cpwarehouse.domain.entity.CheckoutItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for checkout response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Checkout response with reservation details")
public class CheckoutResponse {

    @Schema(description = "Checkout ID", example = "6748a1b2c3d4e5f678901237")
    private String id;
    
    @Schema(description = "Item ID", example = "6748a1b2c3d4e5f678901234")
    private String itemId;
    
    @Schema(description = "Variant ID (if applicable)", example = "6748a1b2c3d4e5f678901235", nullable = true)
    private String variantId;
    
    @Schema(description = "Stock ID", example = "6748a1b2c3d4e5f678901238")
    private String stockId;
    
    @Schema(description = "Quantity checked out", example = "3")
    private Integer quantity;
    
    @Schema(description = "Price per unit", example = "55.00")
    private BigDecimal pricePerUnit;
    
    @Schema(description = "Total price for this checkout", example = "165.00")
    private BigDecimal totalPrice;
    
    @Schema(description = "Reservation movement ID", example = "6748a1b2c3d4e5f678901239")
    private String reservationId;
    
    @Schema(description = "Checkout status", example = "RESERVED", allowableValues = {"RESERVED", "PAID", "CANCELLED", "EXPIRED"})
    private CheckoutItem.CheckoutStatus status;
    
    @Schema(description = "Customer ID", example = "CUST-12345")
    private String customerId;
    
    @Schema(description = "Checkout reference", example = "ORDER-2024-001", nullable = true)
    private String checkoutReference;
    
    @Schema(description = "Creation timestamp (epoch millis)", example = "1700000000000")
    private Long createdAt;
    
    @Schema(description = "Last update timestamp (epoch millis)", example = "1700000000000")
    private Long updatedAt;
}
