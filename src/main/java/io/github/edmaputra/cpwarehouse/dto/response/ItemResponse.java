package io.github.edmaputra.cpwarehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for item response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

  private String id;
  private String sku;
  private String name;
  private String description;
  private BigDecimal basePrice;
  private Boolean isActive;
  private Long createdAt;
  private Long updatedAt;
}
