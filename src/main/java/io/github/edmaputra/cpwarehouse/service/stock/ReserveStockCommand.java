package io.github.edmaputra.cpwarehouse.service.stock;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.request.StockReserveRequest;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;

/**
 * Command to reserve stock for an order.
 */
public interface ReserveStockCommand extends Command<ReserveStockCommand.Request, StockResponse> {

  /**
   * Request wrapper for stock reservation with stock ID.
   */
  record Request(String stockId, StockReserveRequest reserveRequest) {
  }
}
