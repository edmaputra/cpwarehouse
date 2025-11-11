package io.github.edmaputra.cpwarehouse.service.variant;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Command to get all variants with optional filtering.
 */
public interface GetAllVariantsCommand extends Command<GetAllVariantsCommand.Request, Page<VariantResponse>> {

  /**
   * Request object for getting all variants.
   *
   * @param pageable pagination information
   * @param itemId   optional filter by item ID
   * @param isActive optional filter by active status
   * @param search   optional search term for variant SKU or name
   */
  record Request(Pageable pageable, String itemId, Boolean isActive, String search) {
  }
}
