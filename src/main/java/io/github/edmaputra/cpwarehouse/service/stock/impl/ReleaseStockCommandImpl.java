package io.github.edmaputra.cpwarehouse.service.stock.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement;
import io.github.edmaputra.cpwarehouse.dto.request.StockReleaseRequest;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;
import io.github.edmaputra.cpwarehouse.exception.InvalidOperationException;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.StockMapper;
import io.github.edmaputra.cpwarehouse.repository.StockMovementRepository;
import io.github.edmaputra.cpwarehouse.repository.StockRepository;
import io.github.edmaputra.cpwarehouse.service.stock.ReleaseStockCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ReleaseStockCommand.
 * Releases reserved stock (cancel order) or completes order (OUT).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReleaseStockCommandImpl implements ReleaseStockCommand {

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
    Stock stock = stockRepository.findById(request.stockId())
        .orElseThrow(() -> new ResourceNotFoundException("Stock", "id", request.stockId()));

    StockReleaseRequest releaseRequest = request.releaseRequest();
    StockMovement relatedReservation = null;
    int releaseQuantity;

    // Determine release quantity: reference-based or quantity-based
    if (releaseRequest.getReservationId() != null) {
      // Reference-based mode: fetch reservation and validate
      log.info("Releasing stock {} via reservation reference: {}",
          request.stockId(), releaseRequest.getReservationId());

      relatedReservation = stockMovementRepository.findById(releaseRequest.getReservationId())
          .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", releaseRequest.getReservationId()));

      // Validate reservation belongs to this stock
      if (!relatedReservation.getStockId().equals(request.stockId())) {
        throw new InvalidOperationException(
            String.format("Reservation %s does not belong to stock %s",
                releaseRequest.getReservationId(), request.stockId()));
      }

      // Validate it's a RESERVATION movement
      if (relatedReservation.getMovementType() != StockMovement.MovementType.RESERVATION) {
        throw new InvalidOperationException(
            String.format("Movement %s is not a RESERVATION (type: %s)",
                releaseRequest.getReservationId(), relatedReservation.getMovementType()));
      }

      // Validate not already released
      if (relatedReservation.isReleased()) {
        throw new InvalidOperationException(
            String.format("Reservation %s has already been released at %s",
                releaseRequest.getReservationId(), relatedReservation.getReleasedAt()));
      }

      releaseQuantity = relatedReservation.getQuantity();
      log.info("Using quantity from reservation: {}", releaseQuantity);

    } else if (releaseRequest.getQuantity() != null) {
      // Quantity-based mode: manual quantity input
      log.info("Releasing stock {} with type: {}, quantity: {}",
          request.stockId(), releaseRequest.getMovementType(), releaseRequest.getQuantity());

      releaseQuantity = releaseRequest.getQuantity();

    } else {
      // Neither provided - validation error
      throw new InvalidOperationException(
          "Either reservationId or quantity must be provided for release operation");
    }

    // Validate reserved quantity is sufficient
    if (stock.getReservedQuantity() < releaseQuantity) {
      throw new InvalidOperationException(
          String.format("Cannot release %d units. Only %d units are reserved.",
              releaseQuantity, stock.getReservedQuantity()));
    }

    int previousQuantity = stock.getQuantity();
    int previousReserved = stock.getReservedQuantity();
    int movementPreviousQty;
    int movementNewQty;

    // Process based on movement type
    switch (releaseRequest.getMovementType()) {
      case RELEASE:
        // Cancel order - release reservation only
        stock.setReservedQuantity(previousReserved - releaseQuantity);
        // Track reserved quantity change for RELEASE type
        movementPreviousQty = previousReserved;
        movementNewQty = stock.getReservedQuantity();
        break;
      case OUT:
        // Complete order - reduce both reserved and total quantity
        stock.setReservedQuantity(previousReserved - releaseQuantity);
        stock.setQuantity(previousQuantity - releaseQuantity);
        // Track total quantity change for OUT type
        movementPreviousQty = previousQuantity;
        movementNewQty = stock.getQuantity();
        break;
      default:
        throw new InvalidOperationException(
            "Invalid movement type for release. Expected RELEASE or OUT, got: " + releaseRequest.getMovementType());
    }

    stock.preUpdate();
    Stock savedStock = stockRepository.save(stock);

    // Record movement with linkage to original reservation (if reference-based)
    StockMovement releaseMovement = createStockMovement(
        savedStock.getId(), 
        releaseRequest, 
        movementPreviousQty, 
        movementNewQty,
        releaseQuantity,
        relatedReservation != null ? relatedReservation.getId() : null
    );

    // Mark original reservation as released (if reference-based)
    if (relatedReservation != null) {
      relatedReservation.setReleasedAt(System.currentTimeMillis());
      relatedReservation.setReleaseMovementId(releaseMovement.getId());
      stockMovementRepository.save(relatedReservation);
      log.info("Marked reservation {} as released", relatedReservation.getId());
    }

    log.info("Stock {} released successfully. Movement type: {}, Quantity: {}, New reserved: {}",
        request.stockId(), releaseRequest.getMovementType(), releaseQuantity, savedStock.getReservedQuantity());

    return stockMapper.toResponse(savedStock);
  }

  private StockMovement createStockMovement(String stockId, StockReleaseRequest request,
                                            int previousQuantity, int newQuantity,
                                            int releaseQuantity, String relatedReservationId) {
    StockMovement movement = StockMovement.builder()
        .stockId(stockId)
        .movementType(request.getMovementType())
        .quantity(releaseQuantity)
        .previousQuantity(previousQuantity)
        .newQuantity(newQuantity)
        .referenceNumber(request.getReferenceNumber())
        .createdBy(request.getCreatedBy())
        .relatedMovementId(relatedReservationId) // Link to original reservation
        .build();
    movement.prePersist();

    return stockMovementRepository.save(movement);
  }
}
