package io.github.edmaputra.cpwarehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for reserving stock.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReserveRequest {

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be greater than 0")
  private Integer quantity;

  @Size(max = 100, message = "Reference number must not exceed 100 characters")
  private String referenceNumber;

  @NotNull(message = "Created by is required")
  private String createdBy;
}
