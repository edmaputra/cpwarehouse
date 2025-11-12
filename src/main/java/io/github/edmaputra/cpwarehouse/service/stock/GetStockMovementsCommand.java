package io.github.edmaputra.cpwarehouse.service.stock;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement.MovementType;
import io.github.edmaputra.cpwarehouse.dto.response.StockMovementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Command to get stock movement history with pagination.
 */
public interface GetStockMovementsCommand extends Command<GetStockMovementsCommand.Request, Page<StockMovementResponse>> {

  /**
   * Request wrapper for getting stock movements with filters.
   */
  record Request(String stockId, MovementType movementType, Pageable pageable) {
  }
}
