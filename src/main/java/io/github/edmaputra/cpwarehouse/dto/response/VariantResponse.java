package io.github.edmaputra.cpwarehouse.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for variant response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item variant information")
public class VariantResponse {

    @Schema(description = "Variant ID", example = "6748a1b2c3d4e5f678901235")
    private String id;
    
    @Schema(description = "Parent item ID", example = "6748a1b2c3d4e5f678901234")
    private String itemId;
    
    @Schema(description = "Variant SKU", example = "LAPTOP-001-16GB-RED")
    private String variantSku;
    
    @Schema(description = "Variant name", example = "16GB RAM - Red")
    private String variantName;
    
    @Schema(description = "Variant attributes", example = "{\"ram\": \"16GB\", \"color\": \"Red\"}", nullable = true)
    private Map<String, String> attributes;
    
    @Schema(description = "Price adjustment from base price", example = "50.00", nullable = true)
    private BigDecimal priceAdjustment;
    
    @Schema(description = "Active status", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Creation timestamp (epoch millis)", example = "1700000000000")
    private Long createdAt;
    
    @Schema(description = "Last update timestamp (epoch millis)", example = "1700000000000")
    private Long updatedAt;

    /**
     * Calculated final price (basePrice + priceAdjustment).
     * Set by service layer when item information is available.
     */
    @Schema(description = "Final calculated price (base + adjustment)", example = "1049.99", nullable = true)
    private BigDecimal finalPrice;
}
