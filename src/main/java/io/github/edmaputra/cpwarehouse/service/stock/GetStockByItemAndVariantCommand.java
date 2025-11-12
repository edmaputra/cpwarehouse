package io.github.edmaputra.cpwarehouse.service.stock;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;
import org.springframework.data.domain.Pageable;

/**
 * Command to get stock for a specific variant.
 */
public interface GetStockByItemAndVariantCommand extends Command<GetStockByItemAndVariantCommand.Request, StockResponse> {

    record Request(String itemId, String variantId) {
    }
}
