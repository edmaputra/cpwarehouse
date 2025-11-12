package io.github.edmaputra.cpwarehouse.repository;

import io.github.edmaputra.cpwarehouse.domain.entity.CheckoutItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CheckoutItem entity.
 */
@Repository
public interface CheckoutItemRepository extends MongoRepository<CheckoutItem, String> {

    /**
     * Find checkout by reference.
     */
    Optional<CheckoutItem> findByCheckoutReference(String checkoutReference);

    /**
     * Find all checkouts by customer.
     */
    List<CheckoutItem> findByCustomerId(String customerId);

    /**
     * Find checkouts by status.
     */
    List<CheckoutItem> findByStatus(CheckoutItem.CheckoutStatus status);
}
