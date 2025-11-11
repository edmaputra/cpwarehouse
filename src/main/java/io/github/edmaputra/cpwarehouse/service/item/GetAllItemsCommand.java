package io.github.edmaputra.cpwarehouse.service.item;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.response.ItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Command interface for getting all items with pagination and filtering.
 * Use case: Get All Items
 */
public interface GetAllItemsCommand extends Command<GetAllItemsCommand.Request, Page<ItemResponse>> {

  /**
   * Request wrapper for getAllItems operation.
   */
  record Request(Pageable pageable, Boolean activeOnly, String search) {
  }
}
