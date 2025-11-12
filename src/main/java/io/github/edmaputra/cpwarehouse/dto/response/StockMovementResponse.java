package io.github.edmaputra.cpwarehouse.dto.response;

import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Stock movement history record")
public class StockMovementResponse {

    @Schema(description = "Movement ID", example = "6748a1b2c3d4e5f678901239")
    private String id;
    
    @Schema(description = "Stock ID", example = "6748a1b2c3d4e5f678901238")
    private String stockId;
    
    @Schema(description = "Movement type", example = "IN", allowableValues = {"IN", "OUT", "RESERVATION", "RELEASE", "ADJUSTMENT"})
    private MovementType movementType;
    
    @Schema(description = "Quantity moved", example = "25")
    private Integer quantity;
    
    @Schema(description = "Stock quantity before movement", example = "75")
    private Integer previousQuantity;
    
    @Schema(description = "Stock quantity after movement", example = "100")
    private Integer newQuantity;
    
    @Schema(description = "Reference number", example = "PO-2024-001", nullable = true)
    private String referenceNumber;
    
    @Schema(description = "Additional notes", example = "Restocking from supplier", nullable = true)
    private String notes;
    
    @Schema(description = "User who created this movement", example = "admin@cpwarehouse.io")
    private String createdBy;
    
    @Schema(description = "Creation timestamp (epoch millis)", example = "1700000000000")
    private Long createdAt;
}
