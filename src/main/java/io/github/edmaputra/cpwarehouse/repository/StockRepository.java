package io.github.edmaputra.cpwarehouse.repository;

import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Stock entity.
 * Provides CRUD operations and custom query methods for stock.
 */
@Repository
public interface StockRepository extends MongoRepository<Stock, String> {

  /**
   * Find all stock records for a specific item.
   *
   * @param itemId the item ID
   * @return list of stock records
   */
  List<Stock> findByItemId(String itemId);

  /**
   * Find stock for a specific variant.
   *
   * @param variantId the variant ID
   * @return Optional containing the stock if found
   */
  Optional<Stock> findByVariantId(String variantId);

  /**
   * Find stock for an item without variant (base item stock).
   *
   * @param itemId the item ID
   * @return Optional containing the stock if found
   */
  Optional<Stock> findByItemIdAndVariantIdIsNull(String itemId);

  /**
   * Find stock for a specific item and variant combination.
   *
   * @param itemId    the item ID
   * @param variantId the variant ID
   * @return Optional containing the stock if found
   */
  Optional<Stock> findByItemIdAndVariantId(String itemId, String variantId);

  /**
   * Check if stock exists for item-variant combination.
   *
   * @param itemId    the item ID
   * @param variantId the variant ID (can be null)
   * @return true if stock exists
   */
  boolean existsByItemIdAndVariantId(String itemId, String variantId);
}
