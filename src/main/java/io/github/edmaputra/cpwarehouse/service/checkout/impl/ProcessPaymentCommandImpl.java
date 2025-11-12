package io.github.edmaputra.cpwarehouse.service.checkout.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.CheckoutItem;
import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement;
import io.github.edmaputra.cpwarehouse.dto.request.PaymentRequest;
import io.github.edmaputra.cpwarehouse.dto.response.PaymentResponse;
import io.github.edmaputra.cpwarehouse.exception.InvalidOperationException;
import io.github.edmaputra.cpwarehouse.exception.InvalidPaymentException;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.repository.CheckoutItemRepository;
import io.github.edmaputra.cpwarehouse.repository.StockMovementRepository;
import io.github.edmaputra.cpwarehouse.repository.StockRepository;
import io.github.edmaputra.cpwarehouse.service.checkout.ProcessPaymentCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementation of ProcessPaymentCommand.
 * Validates payment amount and commits or releases stock.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentCommandImpl implements ProcessPaymentCommand {

  private final CheckoutItemRepository checkoutItemRepository;
  private final StockRepository stockRepository;
  private final StockMovementRepository stockMovementRepository;

  @Override
  @Transactional
  @Retryable(
      retryFor = OptimisticLockingFailureException.class,
      maxAttempts = 5,
      backoff = @Backoff(
          delay = 100,
          multiplier = 2,
          maxDelay = 800
      )
  )
  public PaymentResponse execute(Request request) {
    PaymentRequest paymentRequest = request.paymentRequest();

    log.info("Processing payment - checkoutId: {}, amount: {}, success: {}",
        request.checkoutId(), paymentRequest.getPaymentAmount(), 
        paymentRequest.getPaymentSuccess());

    // 1. Get checkout item
    CheckoutItem checkout = checkoutItemRepository.findById(request.checkoutId())
        .orElseThrow(() -> new ResourceNotFoundException("CheckoutItem", "id", request.checkoutId()));

    // 2. Validate checkout status
    if (checkout.getStatus() != CheckoutItem.CheckoutStatus.PENDING) {
      throw new InvalidOperationException(
          String.format("Checkout %s is not in PENDING status. Current status: %s",
              request.checkoutId(), checkout.getStatus()));
    }

    // 3. Validate payment amount (must not be less than total price)
    if (paymentRequest.getPaymentAmount().compareTo(checkout.getTotalPrice()) < 0) {
      throw new InvalidPaymentException(
          String.format("Payment amount %.2f is less than required amount %.2f",
              paymentRequest.getPaymentAmount(), checkout.getTotalPrice()));
    }

    // 4. Get stock
    Stock stock = stockRepository.findById(checkout.getStockId())
        .orElseThrow(() -> new ResourceNotFoundException("Stock", "id", checkout.getStockId()));

    // 5. Get reservation movement
    StockMovement reservation = stockMovementRepository.findById(checkout.getReservationId())
        .orElseThrow(() -> new ResourceNotFoundException("StockMovement", "id", 
            checkout.getReservationId()));

    PaymentResponse response;

    if (paymentRequest.getPaymentSuccess()) {
      // Payment succeeded - commit stock (OUT movement)
      response = processSuccessfulPayment(checkout, stock, reservation, paymentRequest);
    } else {
      // Payment failed - release stock (RELEASE movement)
      response = processFailedPayment(checkout, stock, reservation, paymentRequest);
    }

    return response;
  }

  /**
   * Process successful payment - commit stock with OUT movement.
   */
  private PaymentResponse processSuccessfulPayment(CheckoutItem checkout, Stock stock,
                                                     StockMovement reservation, PaymentRequest paymentRequest) {
    log.info("Payment successful - committing stock for checkout: {}", checkout.getId());

    // Reduce both reserved and total quantity
    int previousQuantity = stock.getQuantity();
    int previousReserved = stock.getReservedQuantity();

    stock.setReservedQuantity(previousReserved - checkout.getQuantity());
    stock.setQuantity(previousQuantity - checkout.getQuantity());
    stock.preUpdate();
    Stock savedStock = stockRepository.save(stock);

    // Create OUT movement
    StockMovement outMovement = StockMovement.builder()
        .stockId(savedStock.getId())
        .movementType(StockMovement.MovementType.OUT)
        .quantity(checkout.getQuantity())
        .previousQuantity(previousQuantity)
        .newQuantity(savedStock.getQuantity())
        .referenceNumber(paymentRequest.getPaymentReference())
        .createdBy(paymentRequest.getProcessedBy())
        .relatedMovementId(reservation.getId())
        .build();
    outMovement.prePersist();
    StockMovement savedOutMovement = stockMovementRepository.save(outMovement);

    // Mark reservation as released
    reservation.setReleasedAt(System.currentTimeMillis());
    reservation.setReleaseMovementId(savedOutMovement.getId());
    stockMovementRepository.save(reservation);

    // Update checkout status
    checkout.setStatus(CheckoutItem.CheckoutStatus.COMPLETED);
    checkout.preUpdate();
    checkoutItemRepository.save(checkout);

    log.info("Stock committed - checkout: {}, outMovement: {}, finalQuantity: {}",
        checkout.getId(), savedOutMovement.getId(), savedStock.getQuantity());

    return PaymentResponse.builder()
        .checkoutId(checkout.getId())
        .status(CheckoutItem.CheckoutStatus.COMPLETED)
        .totalPrice(checkout.getTotalPrice())
        .paidAmount(paymentRequest.getPaymentAmount())
        .paymentSuccess(true)
        .paymentReference(paymentRequest.getPaymentReference())
        .message("Payment successful. Stock committed.")
        .processedAt(System.currentTimeMillis())
        .build();
  }

  /**
   * Process failed payment - release stock with RELEASE movement.
   */
  private PaymentResponse processFailedPayment(CheckoutItem checkout, Stock stock,
                                                 StockMovement reservation, PaymentRequest paymentRequest) {
    log.info("Payment failed - releasing stock for checkout: {}", checkout.getId());

    // Only reduce reserved quantity
    int previousReserved = stock.getReservedQuantity();

    stock.setReservedQuantity(previousReserved - checkout.getQuantity());
    stock.preUpdate();
    Stock savedStock = stockRepository.save(stock);

    // Create RELEASE movement
    StockMovement releaseMovement = StockMovement.builder()
        .stockId(savedStock.getId())
        .movementType(StockMovement.MovementType.RELEASE)
        .quantity(checkout.getQuantity())
        .previousQuantity(previousReserved)
        .newQuantity(savedStock.getReservedQuantity())
        .referenceNumber(paymentRequest.getPaymentReference())
        .createdBy(paymentRequest.getProcessedBy())
        .relatedMovementId(reservation.getId())
        .build();
    releaseMovement.prePersist();
    StockMovement savedReleaseMovement = stockMovementRepository.save(releaseMovement);

    // Mark reservation as released
    reservation.setReleasedAt(System.currentTimeMillis());
    reservation.setReleaseMovementId(savedReleaseMovement.getId());
    stockMovementRepository.save(reservation);

    // Update checkout status
    checkout.setStatus(CheckoutItem.CheckoutStatus.PAYMENT_FAILED);
    checkout.preUpdate();
    checkoutItemRepository.save(checkout);

    log.info("Stock released - checkout: {}, releaseMovement: {}, reservedQuantity: {}",
        checkout.getId(), savedReleaseMovement.getId(), savedStock.getReservedQuantity());

    return PaymentResponse.builder()
        .checkoutId(checkout.getId())
        .status(CheckoutItem.CheckoutStatus.PAYMENT_FAILED)
        .totalPrice(checkout.getTotalPrice())
        .paidAmount(paymentRequest.getPaymentAmount())
        .paymentSuccess(false)
        .paymentReference(paymentRequest.getPaymentReference())
        .message("Payment failed. Stock released.")
        .processedAt(System.currentTimeMillis())
        .build();
  }
}
