package io.github.edmaputra.cpwarehouse.service.stock.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement;
import io.github.edmaputra.cpwarehouse.dto.request.StockReserveRequest;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;
import io.github.edmaputra.cpwarehouse.exception.InsufficientStockException;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.StockMapper;
import io.github.edmaputra.cpwarehouse.repository.StockMovementRepository;
import io.github.edmaputra.cpwarehouse.repository.StockRepository;
import io.github.edmaputra.cpwarehouse.service.stock.ReserveStockCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ReserveStockCommand.
 * Reserves stock for an order with optimistic locking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveStockCommandImpl implements ReserveStockCommand {

  private final StockRepository stockRepository;
  private final StockMovementRepository stockMovementRepository;
  private final StockMapper stockMapper;

  @Override
  @Transactional
  @Retryable(
      retryFor = OptimisticLockingFailureException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 100)
  )
  public StockResponse execute(Request request) {
    log.info("Reserving stock {} quantity: {}", request.stockId(), request.reserveRequest().getQuantity());

    Stock stock = stockRepository.findById(request.stockId())
        .orElseThrow(() -> new ResourceNotFoundException("Stock", "id", request.stockId()));

    StockReserveRequest reserveRequest = request.reserveRequest();
    int availableQuantity = stock.getAvailableQuantity();

    // Check availability
    if (availableQuantity < reserveRequest.getQuantity()) {
      throw new InsufficientStockException(request.stockId(), reserveRequest.getQuantity(), availableQuantity);
    }

    // Reserve stock
    int previousReserved = stock.getReservedQuantity();
    stock.setReservedQuantity(previousReserved + reserveRequest.getQuantity());
    stock.preUpdate();

    Stock savedStock = stockRepository.save(stock);

    // Record movement - track reserved quantity change (before → after)
    createStockMovement(savedStock, reserveRequest, previousReserved, savedStock.getReservedQuantity());

    log.info("Stock {} reserved successfully. Reserved quantity: {}",
        request.stockId(), savedStock.getReservedQuantity());

    return stockMapper.toResponse(savedStock);
  }

  private void createStockMovement(Stock stock, StockReserveRequest request,
                                    int previousQuantity, int newQuantity) {
    StockMovement movement = StockMovement.builder()
        .stockId(stock.getId())
        .movementType(StockMovement.MovementType.RESERVATION)
        .quantity(request.getQuantity())
        .previousQuantity(previousQuantity)
        .newQuantity(newQuantity)
        .referenceNumber(request.getReferenceNumber())
        .createdBy(request.getCreatedBy())
        .build();
    movement.prePersist();

    stockMovementRepository.save(movement);
  }
}
