package io.github.edmaputra.cpwarehouse.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Stock entity representing inventory levels for items and their variants.
 * Tracks available quantity, reserved quantity, and warehouse location.
 * Uses optimistic locking with @Version to handle concurrent stock updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "stock")
@CompoundIndex(name = "itemId_variantId_idx", def = "{'itemId': 1, 'variantId': 1}", unique = true)
public class Stock {

  @Id
  private String id;

  /**
   * Reference to the base item.
   */
  @Indexed
  private String itemId;

  /**
   * Reference to the variant (null if stock is for base item without variant).
   */
  @Indexed
  private String variantId;

  /**
   * Total quantity in stock.
   * Must be >= 0
   */
  @Indexed
  @Builder.Default
  private Integer quantity = 0;

  /**
   * Quantity reserved for orders.
   * Must be >= 0 and <= quantity
   */
  @Builder.Default
  private Integer reservedQuantity = 0;

  /**
   * Warehouse location identifier (e.g., "A-01-05").
   */
  private String warehouseLocation;

  private Long createdAt;

  private Long updatedAt;

  /**
   * Version field for optimistic locking.
   * Prevents concurrent modification issues during stock updates.
   */
  @Version
  private Long version;

  /**
   * Calculate available quantity (quantity - reservedQuantity).
   */
  public Integer getAvailableQuantity() {
    return quantity - reservedQuantity;
  }

  /**
   * Check if stock is available.
   */
  public Boolean isAvailable() {
    return getAvailableQuantity() > 0;
  }

  /**
   * Lifecycle callback to set timestamps before persisting.
   */
  public void prePersist() {
    long now = System.currentTimeMillis();
    if (this.createdAt == null) {
      this.createdAt = now;
    }
    this.updatedAt = now;
  }

  /**
   * Lifecycle callback to update timestamp before updating.
   */
  public void preUpdate() {
    this.updatedAt = System.currentTimeMillis();
  }
}
