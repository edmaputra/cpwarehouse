package io.github.edmaputra.cpwarehouse.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for updating an existing variant.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing variant (all fields optional)")
public class VariantUpdateRequest {

    @Size(min = 3, max = 255, message = "Variant name must be between 3 and 255 characters")
    @Schema(description = "Updated variant name", example = "32GB RAM - Blue", nullable = true)
    private String variantName;

    /**
     * Flexible key-value pairs for variant attributes (e.g., {"size": "XL", "color": "Blue"}).
     */
    @Schema(description = "Updated variant attributes", example = "{\"ram\": \"32GB\", \"color\": \"Blue\"}", nullable = true)
    private Map<String, String> attributes;

    /**
     * Price adjustment from the base item price.
     * Can be positive or negative, but final price must be >= 0 (validated at service layer).
     */
    @Digits(integer = 10, fraction = 2, message = "Price adjustment must have at most 10 integer digits and 2 decimal places")
    @Schema(description = "Updated price adjustment from base price", example = "100.00", nullable = true)
    private BigDecimal priceAdjustment;

    @Schema(description = "Whether the variant is active", example = "true", nullable = true)
    private Boolean isActive;
}
