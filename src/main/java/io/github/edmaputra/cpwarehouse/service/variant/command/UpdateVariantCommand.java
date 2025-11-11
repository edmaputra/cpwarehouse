package io.github.edmaputra.cpwarehouse.service.variant.command;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.request.VariantUpdateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;

/**
 * Command to update a variant.
 */
public interface UpdateVariantCommand extends Command<UpdateVariantCommand.Request, VariantResponse> {

  /**
   * Request object for updating a variant.
   *
   * @param id      the variant ID to update
   * @param request the update request data
   */
  record Request(String id, VariantUpdateRequest request) {
  }
}
