package io.github.edmaputra.cpwarehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for detailed item response including related information.
 * Currently identical to ItemResponse but can be extended with variants and stock info later.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetailResponse {

  private String id;
  private String sku;
  private String name;
  private String description;
  private BigDecimal basePrice;
  private Boolean isActive;
  private Long createdAt;
  private Long updatedAt;

  // Future: Add variants and stock information
  // private List<VariantResponse> variants;
  // private List<StockResponse> stock;
}
