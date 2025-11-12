package io.github.edmaputra.cpwarehouse.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * StockMovement entity representing audit trail for stock changes.
 * Records all stock movements including type, quantity changes, and reference information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "stock_movements")
public class StockMovement {

  @Id
  private String id;

  /**
   * Reference to the stock record.
   */
  @Indexed
  private String stockId;

  /**
   * Type of stock movement.
   * Values: IN, OUT, ADJUSTMENT, RESERVATION, RELEASE
   */
  @Indexed
  private MovementType movementType;

  /**
   * Quantity involved in this movement.
   * Must be > 0
   */
  private Integer quantity;

  /**
   * Stock quantity before this movement.
   */
  private Integer previousQuantity;

  /**
   * Stock quantity after this movement.
   */
  private Integer newQuantity;

  /**
   * Reference number for traceability (e.g., PO number, Order number).
   */
  @Indexed
  private String referenceNumber;

  /**
   * Additional notes or comments about this movement.
   */
  private String notes;

  /**
   * User or system that created this movement.
   */
  private String createdBy;

  @Indexed
  private Long createdAt;

  /**
   * For RELEASE/OUT movements: references the original RESERVATION movement ID.
   * This creates a link between reservation and its release for audit trail.
   */
  @Indexed
  private String relatedMovementId;

  /**
   * For RESERVATION movements: timestamp when this reservation was released.
   * Used to prevent double-release of the same reservation.
   */
  private Long releasedAt;

  /**
   * For RESERVATION movements: ID of the RELEASE/OUT movement that released this reservation.
   */
  private String releaseMovementId;

  /**
   * Lifecycle callback to set timestamp before persisting.
   */
  public void prePersist() {
    if (this.createdAt == null) {
      this.createdAt = System.currentTimeMillis();
    }
  }

  /**
   * Check if this reservation has been released.
   * Only applicable for RESERVATION movement type.
   */
  public boolean isReleased() {
    return releasedAt != null && releaseMovementId != null;
  }

  /**
   * Enum for stock movement types.
   */
  public enum MovementType {
    /**
     * Stock coming in (restock, purchase).
     */
    IN,

    /**
     * Stock going out (sale, damage, loss).
     */
    OUT,

    /**
     * Manual stock adjustment (correction).
     */
    ADJUSTMENT,

    /**
     * Stock reserved for an order.
     */
    RESERVATION,

    /**
     * Reserved stock released (order cancelled or stock returned).
     */
    RELEASE
  }
}
