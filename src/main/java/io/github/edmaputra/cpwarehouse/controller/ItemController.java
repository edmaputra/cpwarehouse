package io.github.edmaputra.cpwarehouse.controller;

import io.github.edmaputra.cpwarehouse.common.CommandExecutor;
import io.github.edmaputra.cpwarehouse.dto.request.ItemCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.request.ItemUpdateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.ApiResponse;
import io.github.edmaputra.cpwarehouse.dto.response.ItemDetailResponse;
import io.github.edmaputra.cpwarehouse.dto.response.ItemResponse;
import io.github.edmaputra.cpwarehouse.service.item.command.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Item operations.
 * Uses CommandExecutor to execute command interfaces (Clean Architecture / CQRS pattern).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

  private final CommandExecutor commandExecutor;

  /**
   * Create a new item.
   *
   * @param request the item creation request
   * @return created item response with 201 status
   */
  @PostMapping
  public ResponseEntity<ApiResponse<ItemResponse>> createItem(@Valid @RequestBody ItemCreateRequest request) {

    log.info("POST /api/v1/items - Creating item with SKU: {}", request.getSku());

    ItemResponse response = commandExecutor.execute(CreateItemCommand.class, request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Item created successfully"));
  }

  /**
   * Get all items with pagination and optional filtering.
   *
   * @param page     page number (default: 0)
   * @param size     page size (default: 20)
   * @param isActive filter by active status
   * @param search   search term for name/SKU
   * @param sortBy   field to sort by (default: createdAt)
   * @param sortDir  sort direction (default: DESC)
   * @return page of items
   */
  @GetMapping
  public ResponseEntity<ApiResponse<Page<ItemResponse>>> getAllItems(@RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) Boolean isActive,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "DESC") String sortDir) {

    log.info("GET /api/v1/items - page: {}, size: {}, isActive: {}, search: {}", page, size, isActive, search);

    Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

    Pageable pageable = PageRequest.of(page, size, sort);

    GetAllItemsCommand.Request request = new GetAllItemsCommand.Request(pageable, isActive, search);
    Page<ItemResponse> items = commandExecutor.execute(GetAllItemsCommand.class, request);

    return ResponseEntity.ok(ApiResponse.success(items));
  }

  /**
   * Get item by ID.
   *
   * @param id the item ID
   * @return item detail response
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ItemDetailResponse>> getItemById(@PathVariable String id) {

    log.info("GET /api/v1/items/{} - Fetching item", id);

    ItemDetailResponse response = commandExecutor.execute(GetItemByIdCommand.class, id);

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * Update an existing item.
   *
   * @param id      the item ID
   * @param request the update request
   * @return updated item response
   */
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ItemResponse>> updateItem(@PathVariable String id,
      @Valid @RequestBody ItemUpdateRequest request) {

    log.info("PUT /api/v1/items/{} - Updating item", id);

    UpdateItemCommand.Request commandRequest = new UpdateItemCommand.Request(id, request);
    ItemResponse response = commandExecutor.execute(UpdateItemCommand.class, commandRequest);

    return ResponseEntity.ok(ApiResponse.success(response, "Item updated successfully"));
  }

  /**
   * Soft delete an item.
   *
   * @param id the item ID
   * @return no content response
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteItem(@PathVariable String id) {

    log.info("DELETE /api/v1/items/{} - Soft deleting item", id);

    commandExecutor.execute(DeleteItemCommand.class, id);

    return ResponseEntity.noContent().build();
  }

  /**
   * Hard delete an item (admin only - for testing purposes).
   * In production, this should be protected with authorization.
   *
   * @param id the item ID
   * @return no content response
   */
  @DeleteMapping("/{id}/permanent")
  public ResponseEntity<Void> hardDeleteItem(@PathVariable String id) {

    log.info("DELETE /api/v1/items/{}/permanent - Permanently deleting item", id);

    commandExecutor.execute(HardDeleteItemCommand.class, id);

    return ResponseEntity.noContent().build();
  }
}
