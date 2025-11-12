package io.github.edmaputra.cpwarehouse.controller;

import io.github.edmaputra.cpwarehouse.common.CommandExecutor;
import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement.MovementType;
import io.github.edmaputra.cpwarehouse.dto.request.StockAdjustRequest;
import io.github.edmaputra.cpwarehouse.dto.request.StockCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.request.StockReleaseRequest;
import io.github.edmaputra.cpwarehouse.dto.request.StockReserveRequest;
import io.github.edmaputra.cpwarehouse.dto.response.ApiResponse;
import io.github.edmaputra.cpwarehouse.dto.response.StockMovementResponse;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;
import io.github.edmaputra.cpwarehouse.service.stock.AdjustStockCommand;
import io.github.edmaputra.cpwarehouse.service.stock.CreateStockCommand;
import io.github.edmaputra.cpwarehouse.service.stock.GetStockByItemCommand;
import io.github.edmaputra.cpwarehouse.service.stock.GetStockByVariantCommand;
import io.github.edmaputra.cpwarehouse.service.stock.GetStockMovementsCommand;
import io.github.edmaputra.cpwarehouse.service.stock.ReleaseStockCommand;
import io.github.edmaputra.cpwarehouse.service.stock.ReserveStockCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * REST controller for Stock operations.
 * Uses CommandExecutor to execute command interfaces (Clean Architecture / CQRS pattern).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final CommandExecutor commandExecutor;

    /**
     * Create or initialize a stock record.
     *
     * @param request the stock creation request
     * @return created stock response with 201 status
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StockResponse>> createStock(@Valid @RequestBody StockCreateRequest request) {

        log.info("POST /api/v1/stock - Creating stock for itemId: {}, variantId: {}",
                request.getItemId(), request.getVariantId());

        StockResponse response = commandExecutor.execute(CreateStockCommand.class, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Stock created successfully"));
    }

    /**
     * Adjust stock quantity (IN/OUT/ADJUSTMENT).
     *
     * @param id      the stock ID
     * @param request the adjustment request
     * @return updated stock response
     */
    @PutMapping("/{id}/adjust")
    public ResponseEntity<ApiResponse<StockResponse>> adjustStock(
            @PathVariable String id,
            @Valid @RequestBody StockAdjustRequest request) {

        log.info("PUT /api/v1/stock/{}/adjust - Adjusting stock with type: {}",
                id, request.getMovementType());

        AdjustStockCommand.Request commandRequest = new AdjustStockCommand.Request(id, request);
        StockResponse response = commandExecutor.execute(AdjustStockCommand.class, commandRequest);

        return ResponseEntity.ok(ApiResponse.success(response, "Stock adjusted successfully"));
    }

    /**
     * Reserve stock for an order.
     *
     * @param id      the stock ID
     * @param request the reservation request
     * @return updated stock response
     */
    @PostMapping("/{id}/reserve")
    public ResponseEntity<ApiResponse<StockResponse>> reserveStock(
            @PathVariable String id,
            @Valid @RequestBody StockReserveRequest request) {

        log.info("POST /api/v1/stock/{}/reserve - Reserving {} units", id, request.getQuantity());

        ReserveStockCommand.Request commandRequest = new ReserveStockCommand.Request(id, request);
        StockResponse response = commandExecutor.execute(ReserveStockCommand.class, commandRequest);

        return ResponseEntity.ok(ApiResponse.success(response, "Stock reserved successfully"));
    }

    /**
     * Release reserved stock (cancel order or complete order).
     *
     * @param id      the stock ID
     * @param request the release request
     * @return updated stock response
     */
    @PostMapping("/{id}/release")
    public ResponseEntity<ApiResponse<StockResponse>> releaseStock(
            @PathVariable String id,
            @Valid @RequestBody StockReleaseRequest request) {

        log.info("POST /api/v1/stock/{}/release - Releasing {} units with type: {}",
                id, request.getQuantity(), request.getMovementType());

        ReleaseStockCommand.Request commandRequest = new ReleaseStockCommand.Request(id, request);
        StockResponse response = commandExecutor.execute(ReleaseStockCommand.class, commandRequest);

        return ResponseEntity.ok(ApiResponse.success(response, "Stock released successfully"));
    }

    /**
     * Get stock information for an item (including all variants).
     *
     * @param itemId the item ID
     * @return list of stock records
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getStockByItem(@PathVariable String itemId) {

        log.info("GET /api/v1/stock/item/{} - Fetching stock for item", itemId);

        List<StockResponse> response = commandExecutor.execute(GetStockByItemCommand.class, itemId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get stock information for a specific variant.
     *
     * @param variantId the variant ID
     * @return stock response
     */
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<ApiResponse<StockResponse>> getStockByVariant(@PathVariable String variantId) {

        log.info("GET /api/v1/stock/variant/{} - Fetching stock for variant", variantId);

        StockResponse response = commandExecutor.execute(GetStockByVariantCommand.class, variantId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get stock movement history.
     *
     * @param id           the stock ID
     * @param page         page number (default: 0)
     * @param size         page size (default: 50)
     * @param movementType optional filter by movement type
     * @param sortBy       field to sort by (default: createdAt)
     * @param sortDir      sort direction (default: DESC)
     * @return page of stock movements
     */
    @GetMapping("/{id}/movements")
    public ResponseEntity<ApiResponse<Page<StockMovementResponse>>> getStockMovements(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) MovementType movementType,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("GET /api/v1/stock/{}/movements - page: {}, size: {}, movementType: {}",
                id, page, size, movementType);

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        GetStockMovementsCommand.Request request = new GetStockMovementsCommand.Request(id, movementType, pageable);
        Page<StockMovementResponse> response = commandExecutor.execute(GetStockMovementsCommand.class, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
