package io.github.edmaputra.cpwarehouse.service.item.command;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.request.ItemCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.ItemResponse;

/**
 * Command interface for creating a new item.
 * Use case: Create Item
 */
public interface CreateItemCommand extends Command<ItemCreateRequest, ItemResponse> {
}
