package io.github.edmaputra.cpwarehouse.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Variant entity representing a product variant in the warehouse.
 * Stores variant-specific information including attributes, SKU, and price adjustments.
 * Each variant is associated with a base item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "variants")
@CompoundIndex(name = "itemId_isActive_idx", def = "{'itemId': 1, 'isActive': 1}")
public class Variant {

    @Id
    private String id;

    @Indexed
    private String itemId;

    @Indexed(unique = true)
    private String variantSku;

    private String variantName;

    /**
     * Flexible key-value pairs for variant attributes (e.g., size: L, color: Red).
     * Stored as a nested document in MongoDB.
     */
    private Map<String, String> attributes;

    /**
     * Price adjustment from the base item price.
     * Can be positive or negative, but final price (basePrice + priceAdjustment) must be >= 0.
     */
    @Builder.Default
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

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
