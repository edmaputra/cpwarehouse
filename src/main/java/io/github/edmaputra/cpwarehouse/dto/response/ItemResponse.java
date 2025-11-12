package io.github.edmaputra.cpwarehouse.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for item response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item information")
public class ItemResponse {

    @Schema(description = "Item ID", example = "6748a1b2c3d4e5f678901234")
    private String id;
    
    @Schema(description = "Stock Keeping Unit", example = "LAPTOP-001")
    private String sku;
    
    @Schema(description = "Item name", example = "Premium Laptop")
    private String name;
    
    @Schema(description = "Item description", example = "High-performance laptop for professionals", nullable = true)
    private String description;
    
    @Schema(description = "Base price", example = "999.99")
    private BigDecimal basePrice;
    
    @Schema(description = "Active status", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Creation timestamp (epoch millis)", example = "1700000000000")
    private Long createdAt;
    
    @Schema(description = "Last update timestamp (epoch millis)", example = "1700000000000")
    private Long updatedAt;
}
