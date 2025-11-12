package io.github.edmaputra.cpwarehouse.service.checkout.impl;

import io.github.edmaputra.cpwarehouse.common.CommandExecutor;
import io.github.edmaputra.cpwarehouse.domain.entity.CheckoutItem;
import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement;
import io.github.edmaputra.cpwarehouse.dto.request.CheckoutRequest;
import io.github.edmaputra.cpwarehouse.dto.response.CheckoutResponse;
import io.github.edmaputra.cpwarehouse.dto.response.ItemDetailResponse;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;
import io.github.edmaputra.cpwarehouse.exception.InsufficientStockException;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.CheckoutMapper;
import io.github.edmaputra.cpwarehouse.repository.CheckoutItemRepository;
import io.github.edmaputra.cpwarehouse.repository.StockMovementRepository;
import io.github.edmaputra.cpwarehouse.repository.StockRepository;
import io.github.edmaputra.cpwarehouse.service.checkout.ProcessCheckoutCommand;
import io.github.edmaputra.cpwarehouse.service.item.GetItemByIdCommand;
import io.github.edmaputra.cpwarehouse.service.variant.GetVariantByIdCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementation of ProcessCheckoutCommand.
 * Handles checkout by checking availability and reserving stock.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessCheckoutCommandImpl implements ProcessCheckoutCommand {

  private final CheckoutItemRepository checkoutItemRepository;
  private final StockRepository stockRepository;
  private final StockMovementRepository stockMovementRepository;
  private final CheckoutMapper checkoutMapper;
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
  public CheckoutResponse execute(Request request) {
    CheckoutRequest checkoutRequest = request.checkoutRequest();
    
    log.info("Processing checkout - itemId: {}, variantId: {}, quantity: {}, customer: {}",
        checkoutRequest.getItemId(), checkoutRequest.getVariantId(), 
        checkoutRequest.getQuantity(), checkoutRequest.getCustomerId());

    // 1. Get item to validate and fetch price
    ItemDetailResponse itemResponse = commandExecutor.execute(GetItemByIdCommand.class, checkoutRequest.getItemId());
    
    if (itemResponse == null || !itemResponse.getIsActive()) {
      throw new ResourceNotFoundException("Item", "id", checkoutRequest.getItemId());
    }

    // 2. Calculate price
    BigDecimal pricePerUnit = itemResponse.getBasePrice();
    String variantId = checkoutRequest.getVariantId();

    if (variantId != null && !variantId.isEmpty()) {
      VariantResponse variantResponse = commandExecutor.execute(GetVariantByIdCommand.class, variantId);
      
      if (variantResponse == null || !variantResponse.getIsActive() || !variantResponse.getItemId().equals(itemResponse.getId())) {
        throw new ResourceNotFoundException("Variant", "id", variantId);
      }

      pricePerUnit = pricePerUnit.add(variantResponse.getPriceAdjustment());
    }

    // 3. Find stock
    Stock stock;
    if (variantId != null && !variantId.isEmpty()) {
      stock = stockRepository.findByItemIdAndVariantId(itemResponse.getId(), variantId)
          .orElseThrow(() -> new ResourceNotFoundException("Stock", 
              "itemId and variantId", itemResponse.getId() + " and " + variantId));
    } else {
      stock = stockRepository.findByItemIdAndVariantIdIsNull(itemResponse.getId())
          .orElseThrow(() -> new ResourceNotFoundException("Stock", 
              "itemId", itemResponse.getId()));
    }

    // 4. Check availability
    if (stock.getAvailableQuantity() < checkoutRequest.getQuantity()) {
      throw new InsufficientStockException(
          stock.getId(), 
          checkoutRequest.getQuantity(), 
          stock.getAvailableQuantity());
    }

    // 5. Reserve stock
    int previousReserved = stock.getReservedQuantity();
    stock.setReservedQuantity(previousReserved + checkoutRequest.getQuantity());
    stock.preUpdate();
    Stock savedStock = stockRepository.save(stock);

    // 6. Create reservation movement
    StockMovement reservation = StockMovement.builder()
        .stockId(savedStock.getId())
        .movementType(StockMovement.MovementType.RESERVATION)
        .quantity(checkoutRequest.getQuantity())
        .previousQuantity(previousReserved)
        .newQuantity(savedStock.getReservedQuantity())
        .referenceNumber(checkoutRequest.getCheckoutReference())
        .createdBy(checkoutRequest.getCustomerId())
        .build();
    reservation.prePersist();
    StockMovement savedReservation = stockMovementRepository.save(reservation);

    // 7. Create checkout item
    BigDecimal totalPrice = pricePerUnit.multiply(BigDecimal.valueOf(checkoutRequest.getQuantity()));
    
    CheckoutItem checkoutItem = CheckoutItem.builder()
        .itemId(itemResponse.getId())
        .variantId(variantId)
        .stockId(savedStock.getId())
        .quantity(checkoutRequest.getQuantity())
        .pricePerUnit(pricePerUnit)
        .totalPrice(totalPrice)
        .reservationId(savedReservation.getId())
        .status(CheckoutItem.CheckoutStatus.PENDING)
        .customerId(checkoutRequest.getCustomerId())
        .checkoutReference(checkoutRequest.getCheckoutReference())
        .build();
    checkoutItem.prePersist();
    CheckoutItem savedCheckout = checkoutItemRepository.save(checkoutItem);

    log.info("Checkout completed - checkoutId: {}, reservationId: {}, totalPrice: {}",
        savedCheckout.getId(), savedReservation.getId(), totalPrice);

    return checkoutMapper.toResponse(savedCheckout);
  }
}
