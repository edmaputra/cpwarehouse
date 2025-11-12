package io.github.edmaputra.cpwarehouse.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * CheckoutItem entity representing a checkout session for e-commerce.
 * Tracks item variant selection, price snapshot, stock reservation, and payment status.
 * Uses optimistic locking with @Version to handle concurrent checkouts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "checkout_items")
public class CheckoutItem {

  @Id
  private String id;

  /**
   * Reference to the item.
   */
  @Indexed
  private String itemId;

  /**
   * Reference to the variant (null if checkout is for base item without variant).
   */
  @Indexed
  private String variantId;

  /**
   * Reference to the stock being reserved.
   */
  @Indexed
  private String stockId;

  /**
   * Quantity to checkout.
   */
  private Integer quantity;

  /**
   * Price snapshot at checkout time (price per unit).
   * Used to validate payment amount later.
   */
  private BigDecimal pricePerUnit;

  /**
   * Total price (pricePerUnit * quantity).
   */
  private BigDecimal totalPrice;

  /**
   * Reference to the stock reservation movement.
   * Set after successful reservation.
   */
  private String reservationId;

  /**
   * Checkout status: PENDING, PAYMENT_FAILED, COMPLETED.
   */
  @Indexed
  @Builder.Default
  private CheckoutStatus status = CheckoutStatus.PENDING;

  /**
   * Customer identifier.
   */
  @Indexed
  private String customerId;

  /**
   * Reference number for this checkout session.
   */
  @Indexed
  private String checkoutReference;

  private Long createdAt;

  private Long updatedAt;

  /**
   * Version field for optimistic locking.
   */
  @Version
  private Long version;

  /**
   * Checkout status enum.
   */
  public enum CheckoutStatus {
    PENDING,        // Checkout created, stock reserved, awaiting payment
    PAYMENT_FAILED, // Payment failed, stock released
    COMPLETED       // Payment succeeded, stock committed
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
