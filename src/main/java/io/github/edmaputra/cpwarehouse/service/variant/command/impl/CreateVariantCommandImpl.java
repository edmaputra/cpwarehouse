package io.github.edmaputra.cpwarehouse.service.variant.command.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Item;
import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import io.github.edmaputra.cpwarehouse.dto.request.VariantCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;
import io.github.edmaputra.cpwarehouse.exception.DuplicateResourceException;
import io.github.edmaputra.cpwarehouse.exception.InvalidOperationException;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.VariantMapper;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import io.github.edmaputra.cpwarehouse.repository.VariantRepository;
import io.github.edmaputra.cpwarehouse.service.variant.command.CreateVariantCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementation of CreateVariantCommand.
 * Use case: Create a new variant with validation for item existence,
 * SKU uniqueness, and final price calculation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateVariantCommandImpl implements CreateVariantCommand {

  private final VariantRepository variantRepository;
  private final ItemRepository itemRepository;
  private final VariantMapper variantMapper;

  @Override
  @Transactional
  public VariantResponse execute(VariantCreateRequest request) {
    log.info("Creating new variant with SKU: {} for item ID: {}", request.getVariantSku(), request.getItemId());

    // Validate that item exists and is active
    Item item = itemRepository.findById(request.getItemId())
        .orElseThrow(() -> new ResourceNotFoundException("Item", "id", request.getItemId()));

    if (!item.getIsActive()) {
      throw new InvalidOperationException("Cannot create variant for inactive item with ID: " + request.getItemId());
    }

    // Check for duplicate variant SKU
    if (variantRepository.findByVariantSku(request.getVariantSku()).isPresent()) {
      throw new DuplicateResourceException("Variant", "SKU", request.getVariantSku());
    }

    // Validate final price (basePrice + priceAdjustment) must be >= 0
    BigDecimal priceAdjustment = request.getPriceAdjustment() != null ? request.getPriceAdjustment() : BigDecimal.ZERO;
    BigDecimal finalPrice = item.getBasePrice().add(priceAdjustment);

    if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
      throw new InvalidOperationException(
          String.format("Final price cannot be negative. Base price: %s, Price adjustment: %s, Final price: %s",
              item.getBasePrice(), priceAdjustment, finalPrice));
    }

    // Map request to entity
    Variant variant = variantMapper.toEntity(request);
    variant.prePersist();

    // Save and return
    Variant savedVariant = variantRepository.save(variant);
    log.info("Variant created successfully with ID: {} and SKU: {}", savedVariant.getId(),
        savedVariant.getVariantSku());

    VariantResponse response = variantMapper.toResponse(savedVariant);
    response.setFinalPrice(finalPrice);

    return response;
  }
}
