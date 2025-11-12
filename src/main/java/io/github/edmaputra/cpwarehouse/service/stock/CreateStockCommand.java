package io.github.edmaputra.cpwarehouse.service.stock;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.request.StockCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;

/**
 * Command to create or initialize a stock record.
 */
public interface CreateStockCommand extends Command<StockCreateRequest, StockResponse> {
}
