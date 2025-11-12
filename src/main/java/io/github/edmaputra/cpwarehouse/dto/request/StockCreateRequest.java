package io.github.edmaputra.cpwarehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or initializing a stock record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCreateRequest {

  @NotNull(message = "Item ID is required")
  private String itemId;

  private String variantId; // Optional - null means stock for base item

  @NotNull(message = "Quantity is required")
  @Min(value = 0, message = "Quantity must be greater than or equal to 0")
  private Integer quantity;

  @Size(max = 100, message = "Warehouse location must not exceed 100 characters")
  private String warehouseLocation;
}
