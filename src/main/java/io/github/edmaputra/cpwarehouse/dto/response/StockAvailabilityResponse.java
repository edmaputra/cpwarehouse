package io.github.edmaputra.cpwarehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for stock availability check response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAvailabilityResponse {

  private String stockId;
  private Integer quantity;
  private Integer reservedQuantity;
  private Integer availableQuantity;
  private Boolean isAvailable;
}
