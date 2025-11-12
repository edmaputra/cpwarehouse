package io.github.edmaputra.cpwarehouse.dto.request;

import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for adjusting stock quantity (IN/OUT/ADJUSTMENT).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustRequest {

  @NotNull(message = "Movement type is required")
  private MovementType movementType;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be greater than 0")
  private Integer quantity;

  @Size(max = 100, message = "Reference number must not exceed 100 characters")
  private String referenceNumber;

  @Size(max = 2000, message = "Notes must not exceed 2000 characters")
  private String notes;

  @NotNull(message = "Created by is required")
  private String createdBy;
}
