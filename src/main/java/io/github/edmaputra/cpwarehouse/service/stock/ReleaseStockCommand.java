package io.github.edmaputra.cpwarehouse.service.stock;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.request.StockReleaseRequest;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;

/**
 * Command to release reserved stock (cancel order or complete order).
 */
public interface ReleaseStockCommand extends Command<ReleaseStockCommand.Request, StockResponse> {

    /**
     * Request wrapper for stock release with stock ID.
     */
    record Request(String stockId, StockReleaseRequest releaseRequest) {
    }
}
