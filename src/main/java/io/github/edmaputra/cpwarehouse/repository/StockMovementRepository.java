package io.github.edmaputra.cpwarehouse.repository;

import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement;
import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for StockMovement entity.
 * Provides CRUD operations and custom query methods for stock movements.
 */
@Repository
public interface StockMovementRepository extends MongoRepository<StockMovement, String> {

  /**
   * Find all stock movements for a specific stock record with pagination.
   *
   * @param stockId  the stock ID
   * @param pageable pagination information
   * @return page of stock movements
   */
  Page<StockMovement> findByStockId(String stockId, Pageable pageable);

  /**
   * Find stock movements by stock ID and movement type with pagination.
   *
   * @param stockId      the stock ID
   * @param movementType the movement type
   * @param pageable     pagination information
   * @return page of stock movements
   */
  Page<StockMovement> findByStockIdAndMovementType(String stockId, MovementType movementType, Pageable pageable);

  /**
   * Find all stock movements for a specific stock record.
   *
   * @param stockId the stock ID
   * @return list of stock movements ordered by createdAt descending
   */
  List<StockMovement> findByStockIdOrderByCreatedAtDesc(String stockId);

  /**
   * Find stock movements by reference number.
   *
   * @param referenceNumber the reference number
   * @return list of stock movements
   */
  List<StockMovement> findByReferenceNumber(String referenceNumber);
}
