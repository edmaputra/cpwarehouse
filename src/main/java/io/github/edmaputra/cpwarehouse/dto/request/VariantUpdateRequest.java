package io.github.edmaputra.cpwarehouse.dto.request;

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
public class VariantUpdateRequest {

    @Size(min = 3, max = 255, message = "Variant name must be between 3 and 255 characters")
    private String variantName;

    /**
     * Flexible key-value pairs for variant attributes (e.g., {"size": "XL", "color": "Blue"}).
     */
    private Map<String, String> attributes;

    /**
     * Price adjustment from the base item price.
     * Can be positive or negative, but final price must be >= 0 (validated at service layer).
     */
    @Digits(integer = 10, fraction = 2, message = "Price adjustment must have at most 10 integer digits and 2 decimal places")
    private BigDecimal priceAdjustment;

    private Boolean isActive;
}
