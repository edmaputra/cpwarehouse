package io.github.edmaputra.cpwarehouse.service.variant.command;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;

import java.util.List;

/**
 * Command to get all variants for a specific item.
 */
public interface GetVariantsByItemIdCommand extends Command<String, List<VariantResponse>> {
}
