package io.github.edmaputra.cpwarehouse.service.variant;

import io.github.edmaputra.cpwarehouse.common.Command;

/**
 * Command to permanently delete a variant from the database.
 */
public interface HardDeleteVariantCommand extends Command<String, Void> {
}
