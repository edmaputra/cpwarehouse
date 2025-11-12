package io.github.edmaputra.cpwarehouse.service.stock;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;

import java.util.List;

/**
 * Command to get all stock records for a specific item.
 */
public interface GetStockByIdCommand extends Command<String, StockResponse> {
}
