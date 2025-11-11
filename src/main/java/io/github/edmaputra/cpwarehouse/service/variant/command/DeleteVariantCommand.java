package io.github.edmaputra.cpwarehouse.service.variant.command;

import io.github.edmaputra.cpwarehouse.common.Command;

/**
 * Command to soft delete a variant (set isActive to false).
 */
public interface DeleteVariantCommand extends Command<String, Void> {
}
