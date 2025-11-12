package io.github.edmaputra.cpwarehouse.service.stock;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.request.StockAdjustRequest;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;

/**
 * Command to adjust stock quantity (IN/OUT/ADJUSTMENT).
 */
public interface AdjustStockCommand extends Command<AdjustStockCommand.Request, StockResponse> {

  /**
   * Request wrapper for stock adjustment with stock ID.
   */
  record Request(String stockId, StockAdjustRequest adjustRequest) {
  }
}
