package io.github.edmaputra.cpwarehouse.service.item;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.request.ItemUpdateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.ItemResponse;

/**
 * Command interface for updating an existing item.
 * Use case: Update Item
 */
public interface UpdateItemCommand extends Command<UpdateItemCommand.Request, ItemResponse> {

    /**
     * Request wrapper for update operation.
     */
    record Request(String id, ItemUpdateRequest updateRequest) {
    }
}
