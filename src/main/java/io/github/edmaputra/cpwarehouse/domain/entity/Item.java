package io.github.edmaputra.cpwarehouse.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * Item entity representing a product in the warehouse.
 * Stores base item information including SKU, name, description, and pricing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "items")
public class Item {

  @Id
  private String id;

  @Indexed(unique = true)
  private String sku;

  @TextIndexed(weight = 10)
  private String name;

  @TextIndexed(weight = 5)
  private String description;

  private BigDecimal basePrice;

  @Indexed
  @Builder.Default
  private Boolean isActive = true;

  private Long createdAt;

  private Long updatedAt;

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
