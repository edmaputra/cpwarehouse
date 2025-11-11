package io.github.edmaputra.cpwarehouse.service.variant.command;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;

/**
 * Command to get a variant by variant SKU.
 */
public interface GetVariantBySkuCommand extends Command<String, VariantResponse> {
}
