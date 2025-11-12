package io.github.edmaputra.cpwarehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for stock response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {

    private String id;
    private String itemId;
    private String variantId;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private String warehouseLocation;
    private Long lastRestockedAt;
    private Long createdAt;
    private Long updatedAt;
    private Long version;
    private String reservationId;
}
