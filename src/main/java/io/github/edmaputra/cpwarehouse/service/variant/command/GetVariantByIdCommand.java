package io.github.edmaputra.cpwarehouse.service.variant.command;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;

/**
 * Command to get a variant by ID.
 */
public interface GetVariantByIdCommand extends Command<String, VariantResponse> {
}
