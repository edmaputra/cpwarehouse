package io.github.edmaputra.cpwarehouse.service.checkout.impl;

import io.github.edmaputra.cpwarehouse.common.CommandExecutor;
import io.github.edmaputra.cpwarehouse.domain.entity.CheckoutItem;
import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement;
import io.github.edmaputra.cpwarehouse.dto.request.PaymentRequest;
import io.github.edmaputra.cpwarehouse.dto.request.StockReleaseRequest;
import io.github.edmaputra.cpwarehouse.dto.response.PaymentResponse;
import io.github.edmaputra.cpwarehouse.dto.response.StockMovementResponse;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;
import io.github.edmaputra.cpwarehouse.exception.InvalidOperationException;
import io.github.edmaputra.cpwarehouse.exception.InvalidPaymentException;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.repository.CheckoutItemRepository;
import io.github.edmaputra.cpwarehouse.service.checkout.ProcessPaymentCommand;
import io.github.edmaputra.cpwarehouse.service.stock.GetStockByIdCommand;
import io.github.edmaputra.cpwarehouse.service.stock.GetStockMovementByIdCommand;
import io.github.edmaputra.cpwarehouse.service.stock.ReleaseStockCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ProcessPaymentCommand.
 * Validates payment amount and commits stock (successful payment only).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentCommandImpl implements ProcessPaymentCommand {

    private final CheckoutItemRepository checkoutItemRepository;
    private final CommandExecutor commandExecutor;

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

        log.info("Processing payment - checkoutId: {}, amount: {}",
                request.checkoutId(), paymentRequest.getPaymentAmount());

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
        StockResponse stock = commandExecutor.execute(GetStockByIdCommand.class, checkout.getStockId());

        // 5. Get reservation movement
        StockMovementResponse reservation = commandExecutor.execute(GetStockMovementByIdCommand.class, checkout.getReservationId());

        // 6. Process successful payment - commit stock (OUT movement)
        return processSuccessfulPayment(checkout, stock, reservation, paymentRequest);
    }

    /**
     * Process successful payment - commit stock with OUT movement.
     */
    private PaymentResponse processSuccessfulPayment(CheckoutItem checkout, StockResponse stock,
                                                     StockMovementResponse reservation, PaymentRequest paymentRequest) {
        log.info("Payment successful - committing stock for checkout: {}", checkout.getId());

        ReleaseStockCommand.Request releaseRequest = new ReleaseStockCommand.Request(stock.getId(), StockReleaseRequest.builder()
                .reservationId(reservation.getId())
                .movementType(StockMovement.MovementType.OUT)
                .build());
        commandExecutor.execute(ReleaseStockCommand.class, releaseRequest);

        // Update checkout status
        checkout.setStatus(CheckoutItem.CheckoutStatus.COMPLETED);
        checkout.preUpdate();
        checkoutItemRepository.save(checkout);

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
}
