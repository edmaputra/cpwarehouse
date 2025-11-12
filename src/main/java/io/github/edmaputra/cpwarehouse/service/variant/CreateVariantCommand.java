package io.github.edmaputra.cpwarehouse.service.variant;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.request.VariantCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;

/**
 * Command to create a new variant.
 */
public interface CreateVariantCommand extends Command<VariantCreateRequest, VariantResponse> {
}
