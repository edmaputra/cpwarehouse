package io.github.edmaputra.cpwarehouse.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for stock response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stock information with reservation details")
public class StockResponse {

    @Schema(description = "Stock ID", example = "6748a1b2c3d4e5f678901238")
    private String id;
    
    @Schema(description = "Item ID", example = "6748a1b2c3d4e5f678901234")
    private String itemId;
    
    @Schema(description = "Variant ID (if applicable)", example = "6748a1b2c3d4e5f678901235", nullable = true)
    private String variantId;
    
    @Schema(description = "Total quantity", example = "100")
    private Integer quantity;
    
    @Schema(description = "Reserved quantity", example = "10")
    private Integer reservedQuantity;
    
    @Schema(description = "Available quantity (total - reserved)", example = "90")
    private Integer availableQuantity;
    
    @Schema(description = "Warehouse location", example = "WH-A-01", nullable = true)
    private String warehouseLocation;
    
    @Schema(description = "Last restocked timestamp (epoch millis)", example = "1700000000000", nullable = true)
    private Long lastRestockedAt;
    
    @Schema(description = "Creation timestamp (epoch millis)", example = "1700000000000")
    private Long createdAt;
    
    @Schema(description = "Last update timestamp (epoch millis)", example = "1700000000000")
    private Long updatedAt;
    
    @Schema(description = "Version for optimistic locking", example = "5")
    private Long version;
    
    @Schema(description = "Reservation movement ID (for reserve operations)", example = "6748a1b2c3d4e5f678901239", nullable = true)
    private String reservationId;
}
