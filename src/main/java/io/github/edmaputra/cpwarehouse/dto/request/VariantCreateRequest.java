package io.github.edmaputra.cpwarehouse.dto.request;

import jakarta.validation.constraints.*;
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
public class VariantCreateRequest {

  @NotBlank(message = "Item ID is required")
  private String itemId;

  @NotBlank(message = "Variant SKU is required")
  @Size(max = 100, message = "Variant SKU must not exceed 100 characters")
  @Pattern(regexp = "^[A-Z0-9-]+$", message = "Variant SKU must contain only uppercase letters, numbers, and hyphens")
  private String variantSku;

  @NotBlank(message = "Variant name is required")
  @Size(min = 3, max = 255, message = "Variant name must be between 3 and 255 characters")
  private String variantName;

  /**
   * Flexible key-value pairs for variant attributes (e.g., {"size": "L", "color": "Red"}).
   * Optional field.
   */
  private Map<String, String> attributes;

  /**
   * Price adjustment from the base item price.
   * Can be positive or negative, but final price must be >= 0 (validated at service layer).
   */
  @Digits(integer = 10, fraction = 2, message = "Price adjustment must have at most 10 integer digits and 2 decimal places")
  private BigDecimal priceAdjustment;
}
