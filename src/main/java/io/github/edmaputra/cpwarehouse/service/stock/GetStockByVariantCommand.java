package io.github.edmaputra.cpwarehouse.service.stock;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;

/**
 * Command to get stock for a specific variant.
 */
public interface GetStockByVariantCommand extends Command<String, StockResponse> {
}
