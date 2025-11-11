package io.github.edmaputra.cpwarehouse.service.item.command;

import io.github.edmaputra.cpwarehouse.common.Command;

/**
 * Command interface for permanently deleting an item.
 * Use case: Hard Delete Item
 */
public interface HardDeleteItemCommand extends Command<String, Void> {
}
