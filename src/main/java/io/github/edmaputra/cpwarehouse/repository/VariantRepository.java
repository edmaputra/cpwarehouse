package io.github.edmaputra.cpwarehouse.repository;

import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Variant entity.
 * Provides CRUD operations and custom query methods for variants.
 * Extends VariantRepositoryCustom for MongoTemplate-based custom queries.
 */
@Repository
public interface VariantRepository extends MongoRepository<Variant, String>, VariantRepositoryCustom {

    /**
     * Find a variant by variant SKU.
     *
     * @param variantSku the variant SKU to search for
     * @return Optional containing the variant if found
     */
    Optional<Variant> findByVariantSku(String variantSku);

    /**
     * Find all variants for a specific item.
     *
     * @param itemId the item ID
     * @return list of variants
     */
    List<Variant> findByItemId(String itemId);

    /**
     * Find all variants for a specific item with pagination.
     *
     * @param itemId   the item ID
     * @param pageable pagination information
     * @return page of variants
     */
    Page<Variant> findByItemId(String itemId, Pageable pageable);

    /**
     * Find all active variants for a specific item.
     *
     * @param itemId   the item ID
     * @param isActive the active status
     * @return list of variants
     */
    List<Variant> findByItemIdAndIsActive(String itemId, Boolean isActive);

    /**
     * Count variants by item ID.
     *
     * @param itemId the item ID
     * @return count of variants
     */
    long countByItemId(String itemId);
}
