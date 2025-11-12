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
 * DTO for releasing reserved stock - supports hybrid approach.
 * 
 * Two modes:
 * 1. Reference-based: Provide reservationId (quantity auto-calculated)
 * 2. Quantity-based: Provide quantity (manual mode)
 * 
 * Movement type can be RELEASE (cancel order) or OUT (complete order).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReleaseRequest {

  /**
   * ID of the RESERVATION movement to release (reference-based mode).
   * If provided, quantity will be automatically fetched from the reservation.
   * This prevents user error and ensures accurate release.
   */
  @Size(max = 50, message = "Reservation ID must not exceed 50 characters")
  private String reservationId;

  /**
   * Quantity to release (quantity-based mode).
   * Only used if reservationId is not provided.
   * Required if reservationId is null.
   */
  @Min(value = 1, message = "Quantity must be greater than 0")
  private Integer quantity;

  @NotNull(message = "Movement type is required")
  private MovementType movementType; // RELEASE or OUT

  @Size(max = 100, message = "Reference number must not exceed 100 characters")
  private String referenceNumber;

  @NotNull(message = "Created by is required")
  private String createdBy;
}
