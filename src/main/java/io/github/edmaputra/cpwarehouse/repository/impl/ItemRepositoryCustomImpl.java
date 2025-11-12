package io.github.edmaputra.cpwarehouse.repository.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Item;
import io.github.edmaputra.cpwarehouse.repository.ItemRepositoryCustom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom repository implementation for Item entity.
 * Uses MongoTemplate for flexible query building with dynamic filters.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Item> findAllWithFilters(Pageable pageable, Boolean activeOnly, String search) {
        log.debug("Finding items with filters - activeOnly: {}, search: {}, page: {}, size: {}",
                activeOnly,
                search,
                pageable.getPageNumber(),
                pageable.getPageSize());

        // Build dynamic query
        List<Criteria> criteriaList = new ArrayList<>();

        // Add active filter if specified
        if (activeOnly != null) {
            criteriaList.add(Criteria.where("isActive").is(activeOnly));
            log.debug("Added isActive filter: {}", activeOnly);
        }

        // Add search filter if specified
        if (StringUtils.hasText(search)) {
            // Search in both name and SKU fields (case-insensitive)
            Criteria searchCriteria = new Criteria().orOperator(Criteria.where("name").regex(search, "i"),
                    Criteria.where("sku").regex(search, "i"));
            criteriaList.add(searchCriteria);
            log.debug("Added search filter: {}", search);
        }

        // Combine all criteria with AND
        Query query = new Query();
        if (!CollectionUtils.isEmpty(criteriaList)) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        // Get total count for pagination
        long total = mongoTemplate.count(query, Item.class);
        log.debug("Total items matching criteria: {}", total);

        // Apply pagination and sorting
        query.with(pageable);

        // Execute query
        List<Item> items = mongoTemplate.find(query, Item.class);
        log.debug("Retrieved {} items for current page", items.size());

        return new PageImpl<>(items, pageable, total);
    }
}
