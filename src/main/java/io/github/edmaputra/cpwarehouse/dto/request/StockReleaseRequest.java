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
 * DTO for releasing reserved stock - supports hybrid approach.
 * <p>
 * Two modes:
 * 1. Reference-based: Provide reservationId (quantity auto-calculated)
 * 2. Quantity-based: Provide quantity (manual mode)
 * <p>
 * Movement type can be RELEASE (cancel order) or OUT (complete order).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to release reserved stock (cancel or complete)")
public class StockReleaseRequest {

    /**
     * ID of the RESERVATION movement to release (reference-based mode).
     * If provided, quantity will be automatically fetched from the reservation.
     * This prevents user error and ensures accurate release.
     */
    @Size(max = 50, message = "Reservation ID must not exceed 50 characters")
    @Schema(description = "ID of reservation movement (for reference-based release)", example = "6748a1b2c3d4e5f678901236", nullable = true)
    private String reservationId;

    /**
     * Quantity to release (quantity-based mode).
     * Only used if reservationId is not provided.
     * Required if reservationId is null.
     */
    @Min(value = 1, message = "Quantity must be greater than 0")
    @Schema(description = "Quantity to release (for quantity-based release)", example = "3", minimum = "1", nullable = true)
    private Integer quantity;

    @NotNull(message = "Movement type is required")
    @Schema(description = "Type of release: RELEASE (cancel) or OUT (complete)", example = "OUT", allowableValues = {"RELEASE", "OUT"})
    private MovementType movementType; // RELEASE or OUT

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    @Schema(description = "Reference number for tracking", example = "PAYMENT-2024-001", nullable = true)
    private String referenceNumber;

    @NotNull(message = "Created by is required")
    @Schema(description = "User who created this release", example = "system@cpwarehouse.io")
    private String createdBy;
}
