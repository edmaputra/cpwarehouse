package io.github.edmaputra.cpwarehouse.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for creating a new variant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new variant of an item")
public class VariantCreateRequest {

    @NotBlank(message = "Item ID is required")
    @Schema(description = "Parent item ID", example = "6748a1b2c3d4e5f678901234")
    private String itemId;

    @NotBlank(message = "Variant SKU is required")
    @Size(max = 100, message = "Variant SKU must not exceed 100 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Variant SKU must contain only uppercase letters, numbers, and hyphens")
    @Schema(description = "Unique SKU for this variant", example = "LAPTOP-001-16GB-RED", pattern = "^[A-Z0-9-]+$")
    private String variantSku;

    @NotBlank(message = "Variant name is required")
    @Size(min = 3, max = 255, message = "Variant name must be between 3 and 255 characters")
    @Schema(description = "Variant name", example = "16GB RAM - Red")
    private String variantName;

    /**
     * Flexible key-value pairs for variant attributes (e.g., {"size": "L", "color": "Red"}).
     * Optional field.
     */
    @Schema(description = "Variant attributes as key-value pairs", example = "{\"ram\": \"16GB\", \"color\": \"Red\"}", nullable = true)
    private Map<String, String> attributes;

    /**
     * Price adjustment from the base item price.
     * Can be positive or negative, but final price must be >= 0 (validated at service layer).
     */
    @Digits(integer = 10, fraction = 2, message = "Price adjustment must have at most 10 integer digits and 2 decimal places")
    @Schema(description = "Price adjustment (positive or negative) from base item price", example = "50.00", nullable = true)
    private BigDecimal priceAdjustment;
}
