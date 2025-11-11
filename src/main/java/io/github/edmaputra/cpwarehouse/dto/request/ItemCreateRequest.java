package io.github.edmaputra.cpwarehouse.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating a new item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateRequest {

  @NotBlank(message = "SKU is required")
  @Size(max = 100, message = "SKU must not exceed 100 characters")
  @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
  private String sku;

  @NotBlank(message = "Name is required")
  @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
  private String name;

  @Size(max = 2000, message = "Description must not exceed 2000 characters")
  private String description;

  @NotNull(message = "Base price is required")
  @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be greater than or equal to 0")
  @Digits(integer = 10, fraction = 2, message = "Base price must have at most 10 integer digits and 2 decimal places")
  private BigDecimal basePrice;
}
