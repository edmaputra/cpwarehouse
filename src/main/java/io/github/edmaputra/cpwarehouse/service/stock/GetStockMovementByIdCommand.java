package io.github.edmaputra.cpwarehouse.service.stock;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.response.StockMovementResponse;

/**
 * Command to get all stock records for a specific item.
 */
public interface GetStockMovementByIdCommand extends Command<String, StockMovementResponse> {
}
