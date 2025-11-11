package io.github.edmaputra.cpwarehouse.repository.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import io.github.edmaputra.cpwarehouse.repository.VariantRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Custom implementation of VariantRepositoryCustom using MongoTemplate.
 * Provides dynamic query building with multiple optional filters.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class VariantRepositoryCustomImpl implements VariantRepositoryCustom {

  private final MongoTemplate mongoTemplate;

  @Override
  public Page<Variant> findAllWithFilters(String itemId, Boolean isActive, String search, Pageable pageable) {

    log.debug("Finding variants with filters - itemId: {}, isActive: {}, search: {}", itemId, isActive, search);

    Query query = new Query();
    List<Criteria> criteria = new ArrayList<>();

    // Filter by item ID
    if (itemId != null && !itemId.isBlank()) {
      criteria.add(Criteria.where("itemId").is(itemId));
    }

    // Filter by active status
    if (isActive != null) {
      criteria.add(Criteria.where("isActive").is(isActive));
    }

    // Search in variantSku or variantName (case-insensitive regex)
    if (search != null && !search.isBlank()) {
      Pattern pattern = Pattern.compile(search, Pattern.CASE_INSENSITIVE);
      Criteria searchCriteria = new Criteria().orOperator(
          Criteria.where("variantSku").regex(pattern),
          Criteria.where("variantName").regex(pattern)
      );
      criteria.add(searchCriteria);
    }

    // Combine all criteria
    if (!criteria.isEmpty()) {
      query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
    }

    // Get total count for pagination
    long total = mongoTemplate.count(query, Variant.class);

    // Apply pagination
    query.with(pageable);

    // Execute query
    List<Variant> variants = mongoTemplate.find(query, Variant.class);

    log.debug("Found {} variants out of {} total", variants.size(), total);

    return new PageImpl<>(variants, pageable, total);
  }
}
