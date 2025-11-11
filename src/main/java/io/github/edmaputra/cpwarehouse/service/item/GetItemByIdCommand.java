package io.github.edmaputra.cpwarehouse.service.item;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.response.ItemDetailResponse;

/**
 * Command interface for getting an item by its ID.
 * Use case: Get Item By ID
 */
public interface GetItemByIdCommand extends Command<String, ItemDetailResponse> {
}
