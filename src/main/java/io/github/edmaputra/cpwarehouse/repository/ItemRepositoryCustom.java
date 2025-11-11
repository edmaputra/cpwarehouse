package io.github.edmaputra.cpwarehouse.repository;

import io.github.edmaputra.cpwarehouse.domain.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom repository interface for Item entity.
 * Provides custom query methods using MongoTemplate.
 */
public interface ItemRepositoryCustom {

  /**
   * Find all items with pagination and dynamic filtering.
   * Supports filtering by active status and text search on name/SKU.
   *
   * @param pageable   pagination information (page, size, sort)
   * @param activeOnly filter by active status (null for all items)
   * @param search     search term for name or SKU (case-insensitive, null for no search)
   * @return page of items matching the criteria
   */
  Page<Item> findAllWithFilters(Pageable pageable, Boolean activeOnly, String search);
}
