package io.github.edmaputra.cpwarehouse.dto.response;

import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for stock movement response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {

    private String id;
    private String stockId;
    private MovementType movementType;
    private Integer quantity;
    private Integer previousQuantity;
    private Integer newQuantity;
    private String referenceNumber;
    private String notes;
    private String createdBy;
    private Long createdAt;
}
