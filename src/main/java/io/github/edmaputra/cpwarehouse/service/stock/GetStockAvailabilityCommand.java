package io.github.edmaputra.cpwarehouse.service.stock;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.response.StockAvailabilityResponse;

/**
 * Command to check stock availability.
 */
public interface GetStockAvailabilityCommand extends Command<String, StockAvailabilityResponse> {
}
