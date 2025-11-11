package io.github.edmaputra.cpwarehouse.repository;

import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom repository interface for Variant entity.
 * Provides complex query methods using MongoTemplate.
 */
public interface VariantRepositoryCustom {

  /**
   * Find all variants with optional filters.
   *
   * @param itemId     filter by item ID (optional)
   * @param isActive   filter by active status (optional)
   * @param search     search term for variant SKU or name (optional)
   * @param pageable   pagination information
   * @return page of variants matching the criteria
   */
  Page<Variant> findAllWithFilters(String itemId, Boolean isActive, String search, Pageable pageable);
}
