package io.github.edmaputra.cpwarehouse.service.item.command;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.response.ItemResponse;

/**
 * Command interface for getting an item by its SKU.
 * Use case: Get Item By SKU
 */
public interface GetItemBySkuCommand extends Command<String, ItemResponse> {
}
