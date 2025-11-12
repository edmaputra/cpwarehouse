package io.github.edmaputra.cpwarehouse.dto.response;

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
public class VariantResponse {

    private String id;
    private String itemId;
    private String variantSku;
    private String variantName;
    private Map<String, String> attributes;
    private BigDecimal priceAdjustment;
    private Boolean isActive;
    private Long createdAt;
    private Long updatedAt;

    /**
     * Calculated final price (basePrice + priceAdjustment).
     * Set by service layer when item information is available.
     */
    private BigDecimal finalPrice;
}
