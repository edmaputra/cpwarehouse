package io.github.edmaputra.cpwarehouse.service.item;

import io.github.edmaputra.cpwarehouse.common.Command;

/**
 * Command interface for soft deleting an item.
 * Use case: Delete Item (Soft Delete)
 */
public interface DeleteItemCommand extends Command<String, Void> {
}
