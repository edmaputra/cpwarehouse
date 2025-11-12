package io.github.edmaputra.cpwarehouse.controller;

import io.github.edmaputra.cpwarehouse.common.CommandExecutor;
import io.github.edmaputra.cpwarehouse.dto.request.VariantCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.request.VariantUpdateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.ApiResponse;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;
import io.github.edmaputra.cpwarehouse.service.variant.CreateVariantCommand;
import io.github.edmaputra.cpwarehouse.service.variant.DeleteVariantCommand;
import io.github.edmaputra.cpwarehouse.service.variant.GetAllVariantsCommand;
import io.github.edmaputra.cpwarehouse.service.variant.GetVariantByIdCommand;
import io.github.edmaputra.cpwarehouse.service.variant.GetVariantBySkuCommand;
import io.github.edmaputra.cpwarehouse.service.variant.GetVariantsByItemIdCommand;
import io.github.edmaputra.cpwarehouse.service.variant.HardDeleteVariantCommand;
import io.github.edmaputra.cpwarehouse.service.variant.UpdateVariantCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for Variant operations.
 * Uses CommandExecutor to execute command interfaces (Clean Architecture / CQRS pattern).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class VariantController {

    private final CommandExecutor commandExecutor;

    /**
     * Create a new variant for an item.
     * POST /api/v1/items/{itemId}/variants
     *
     * @param request the variant creation request
     * @return created variant response with 201 status
     */
    @PostMapping("/items/{itemId}/variants")
    public ResponseEntity<ApiResponse<VariantResponse>> createVariant(@PathVariable String itemId,
                                                                      @Valid @RequestBody VariantCreateRequest request) {

        log.info("POST /api/v1/items/{}/variants - Creating variant with SKU: {}", itemId, request.getVariantSku());

        // Ensure itemId from path matches request body
        request.setItemId(itemId);

        VariantResponse response = commandExecutor.execute(CreateVariantCommand.class, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Variant created successfully"));
    }

    /**
     * Get all variants for a specific item.
     * GET /api/v1/items/{itemId}/variants
     *
     * @param itemId the item ID
     * @return list of variants for the item
     */
    @GetMapping("/items/{itemId}/variants")
    public ResponseEntity<ApiResponse<List<VariantResponse>>> getVariantsByItemId(@PathVariable String itemId) {

        log.info("GET /api/v1/items/{}/variants - Fetching all variants", itemId);

        List<VariantResponse> variants = commandExecutor.execute(GetVariantsByItemIdCommand.class, itemId);

        return ResponseEntity.ok(ApiResponse.success(variants));
    }

    /**
     * Get all variants with pagination and optional filtering.
     * GET /api/v1/variants
     *
     * @param page     page number (default: 0)
     * @param size     page size (default: 20)
     * @param itemId   filter by item ID
     * @param isActive filter by active status
     * @param search   search term for variant SKU or name
     * @param sortBy   field to sort by (default: createdAt)
     * @param sortDir  sort direction (default: DESC)
     * @return page of variants
     */
    @GetMapping("/variants")
    public ResponseEntity<ApiResponse<Page<VariantResponse>>> getAllVariants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String itemId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("GET /api/v1/variants - page: {}, size: {}, itemId: {}, isActive: {}, search: {}",
                page, size, itemId, isActive, search);

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        GetAllVariantsCommand.Request request = new GetAllVariantsCommand.Request(pageable, itemId, isActive, search);
        Page<VariantResponse> variants = commandExecutor.execute(GetAllVariantsCommand.class, request);

        return ResponseEntity.ok(ApiResponse.success(variants));
    }

    /**
     * Get variant by ID.
     * GET /api/v1/variants/{id}
     *
     * @param id the variant ID
     * @return variant response
     */
    @GetMapping("/variants/{id}")
    public ResponseEntity<ApiResponse<VariantResponse>> getVariantById(@PathVariable String id) {

        log.info("GET /api/v1/variants/{} - Fetching variant", id);

        VariantResponse response = commandExecutor.execute(GetVariantByIdCommand.class, id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get variant by variant SKU.
     * GET /api/v1/variants/sku/{sku}
     *
     * @param sku the variant SKU
     * @return variant response
     */
    @GetMapping("/variants/sku/{sku}")
    public ResponseEntity<ApiResponse<VariantResponse>> getVariantBySku(@PathVariable String sku) {

        log.info("GET /api/v1/variants/sku/{} - Fetching variant", sku);

        VariantResponse response = commandExecutor.execute(GetVariantBySkuCommand.class, sku);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update an existing variant.
     * PUT /api/v1/variants/{id}
     *
     * @param id      the variant ID
     * @param request the update request
     * @return updated variant response
     */
    @PutMapping("/variants/{id}")
    public ResponseEntity<ApiResponse<VariantResponse>> updateVariant(@PathVariable String id,
                                                                      @Valid @RequestBody VariantUpdateRequest request) {

        log.info("PUT /api/v1/variants/{} - Updating variant", id);

        UpdateVariantCommand.Request commandRequest = new UpdateVariantCommand.Request(id, request);
        VariantResponse response = commandExecutor.execute(UpdateVariantCommand.class, commandRequest);

        return ResponseEntity.ok(ApiResponse.success(response, "Variant updated successfully"));
    }

    /**
     * Soft delete a variant.
     * DELETE /api/v1/variants/{id}
     *
     * @param id the variant ID
     * @return no content response
     */
    @DeleteMapping("/variants/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable String id) {

        log.info("DELETE /api/v1/variants/{} - Soft deleting variant", id);

        commandExecutor.execute(DeleteVariantCommand.class, id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Hard delete a variant (admin only - for testing purposes).
     * In production, this should be protected with authorization.
     * DELETE /api/v1/variants/{id}/permanent
     *
     * @param id the variant ID
     * @return no content response
     */
    @DeleteMapping("/variants/{id}/permanent")
    public ResponseEntity<Void> hardDeleteVariant(@PathVariable String id) {

        log.info("DELETE /api/v1/variants/{}/permanent - Permanently deleting variant", id);

        commandExecutor.execute(HardDeleteVariantCommand.class, id);

        return ResponseEntity.noContent().build();
    }
}
