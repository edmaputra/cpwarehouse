package io.github.edmaputra.cpwarehouse.service.variant.command.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Item;
import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import io.github.edmaputra.cpwarehouse.dto.request.VariantUpdateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;
import io.github.edmaputra.cpwarehouse.exception.InvalidOperationException;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.VariantMapper;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import io.github.edmaputra.cpwarehouse.repository.VariantRepository;
import io.github.edmaputra.cpwarehouse.service.variant.command.UpdateVariantCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementation of UpdateVariantCommand.
 * Use case: Update variant information with validation for final price calculation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateVariantCommandImpl implements UpdateVariantCommand {

  private final VariantRepository variantRepository;
  private final ItemRepository itemRepository;
  private final VariantMapper variantMapper;

  @Override
  @Transactional
  public VariantResponse execute(Request request) {
    log.info("Updating variant with ID: {}", request.id());

    // Find existing variant
    Variant variant = variantRepository.findById(request.id())
        .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", request.id()));

    // If priceAdjustment is being updated, validate final price
    if (request.request().getPriceAdjustment() != null) {
      Item item = itemRepository.findById(variant.getItemId())
          .orElseThrow(() -> new ResourceNotFoundException("Item", "id", variant.getItemId()));

      BigDecimal priceAdjustment = request.request().getPriceAdjustment();
      BigDecimal finalPrice = item.getBasePrice().add(priceAdjustment);

      if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
        throw new InvalidOperationException(
            String.format("Final price cannot be negative. Base price: %s, Price adjustment: %s, Final price: %s",
                item.getBasePrice(), priceAdjustment, finalPrice));
      }
    }

    // Update entity from request
    variantMapper.updateEntityFromRequest(request.request(), variant);
    variant.preUpdate();

    // Save and return
    Variant updatedVariant = variantRepository.save(variant);
    log.info("Variant updated successfully: {}", updatedVariant.getId());

    return variantMapper.toResponse(updatedVariant);
  }
}
