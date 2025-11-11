package io.github.edmaputra.cpwarehouse.repository;

import io.github.edmaputra.cpwarehouse.domain.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Item entity.
 * Provides CRUD operations and custom query methods for items.
 * Extends ItemRepositoryCustom for MongoTemplate-based custom queries.
 */
@Repository
public interface ItemRepository extends MongoRepository<Item, String>, ItemRepositoryCustom {

  /**
   * Find an item by SKU.
   *
   * @param sku the SKU to search for
   * @return Optional containing the item if found
   */
  Optional<Item> findBySku(String sku);
}
