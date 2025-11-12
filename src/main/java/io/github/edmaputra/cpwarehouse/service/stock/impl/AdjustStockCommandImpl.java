package io.github.edmaputra.cpwarehouse.service.stock.impl;

import io.github.edmaputra.cpwarehouse.common.CommonRetryable;
import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement;
import io.github.edmaputra.cpwarehouse.dto.request.StockAdjustRequest;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;
import io.github.edmaputra.cpwarehouse.exception.InvalidOperationException;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.StockMapper;
import io.github.edmaputra.cpwarehouse.repository.StockMovementRepository;
import io.github.edmaputra.cpwarehouse.repository.StockRepository;
import io.github.edmaputra.cpwarehouse.service.stock.AdjustStockCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AdjustStockCommand.
 * Adjusts stock quantity with IN, OUT, or ADJUSTMENT movement types.
 * Uses optimistic locking with retry logic for concurrent updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdjustStockCommandImpl implements AdjustStockCommand {

  private final StockRepository stockRepository;
  private final StockMovementRepository stockMovementRepository;
  private final StockMapper stockMapper;

  @Override
  @Transactional
  @CommonRetryable
  public StockResponse execute(Request request) {
    // Get current retry context for debugging
    RetryContext context = RetrySynchronizationManager.getContext();
    int retryCount = context != null ? context.getRetryCount() : 0;
    
    log.info("Adjusting stock {} with type: {}, quantity: {} [Retry attempt: {}]",
        request.stockId(), request.adjustRequest().getMovementType(), request.adjustRequest().getQuantity(), retryCount + 1);

    Stock stock = stockRepository.findById(request.stockId())
        .orElseThrow(() -> new ResourceNotFoundException("Stock", "id", request.stockId()));

    StockAdjustRequest adjustRequest = request.adjustRequest();
    int previousQuantity = stock.getQuantity();
    int newQuantity;

    // Calculate new quantity based on movement type
    switch (adjustRequest.getMovementType()) {
      case IN:
        newQuantity = previousQuantity + adjustRequest.getQuantity();
        break;
      case OUT:
        newQuantity = previousQuantity - adjustRequest.getQuantity();
        if (newQuantity < 0) {
          throw new InvalidOperationException("Cannot adjust stock below zero. Current quantity: " + previousQuantity);
        }
        if (newQuantity < stock.getReservedQuantity()) {
          throw new InvalidOperationException(
              "Cannot reduce stock below reserved quantity. Reserved: " + stock.getReservedQuantity());
        }
        break;
      case ADJUSTMENT:
        newQuantity = adjustRequest.getQuantity(); // Direct adjustment
        if (newQuantity < stock.getReservedQuantity()) {
          throw new InvalidOperationException(
              "Adjusted quantity cannot be less than reserved quantity. Reserved: " + stock.getReservedQuantity());
        }
        break;
      default:
        throw new InvalidOperationException("Invalid movement type for adjustment: " + adjustRequest.getMovementType());
    }

    // Update stock
    stock.setQuantity(newQuantity);
    stock.preUpdate();

    Stock savedStock = stockRepository.save(stock);

    // Record movement
    createStockMovement(savedStock.getId(), adjustRequest, previousQuantity, newQuantity);

    log.info("Stock {} adjusted successfully. Previous: {}, New: {}", request.stockId(), previousQuantity, newQuantity);

    return stockMapper.toResponse(savedStock);
  }

  private void createStockMovement(String stockId, StockAdjustRequest request,
                                    int previousQuantity, int newQuantity) {
    StockMovement movement = StockMovement.builder()
        .stockId(stockId)
        .movementType(request.getMovementType())
        .quantity(request.getQuantity())
        .previousQuantity(previousQuantity)
        .newQuantity(newQuantity)
        .referenceNumber(request.getReferenceNumber())
        .notes(request.getNotes())
        .createdBy(request.getCreatedBy())
        .build();
    movement.prePersist();

    stockMovementRepository.save(movement);
  }
}
