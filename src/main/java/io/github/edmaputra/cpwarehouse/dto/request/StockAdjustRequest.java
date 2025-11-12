package io.github.edmaputra.cpwarehouse.dto.request;

import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request to adjust stock quantity")
public class StockAdjustRequest {

    @NotNull(message = "Movement type is required")
    @Schema(description = "Type of stock movement", example = "IN", allowableValues = {"IN", "OUT", "ADJUSTMENT"})
    private MovementType movementType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    @Schema(description = "Quantity to adjust", example = "25", minimum = "1")
    private Integer quantity;

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    @Schema(description = "Reference number for tracking", example = "PO-2024-001", nullable = true)
    private String referenceNumber;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    @Schema(description = "Additional notes", example = "Restocking from supplier", nullable = true)
    private String notes;

    @NotNull(message = "Created by is required")
    @Schema(description = "User who created this adjustment", example = "admin@cpwarehouse.io")
    private String createdBy;
}
