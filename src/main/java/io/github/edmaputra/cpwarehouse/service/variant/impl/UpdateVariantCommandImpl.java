package io.github.edmaputra.cpwarehouse.service.variant.impl;

import io.github.edmaputra.cpwarehouse.common.CommandExecutor;
import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import io.github.edmaputra.cpwarehouse.dto.response.ItemDetailResponse;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;
import io.github.edmaputra.cpwarehouse.exception.InvalidOperationException;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.VariantMapper;
import io.github.edmaputra.cpwarehouse.repository.VariantRepository;
import io.github.edmaputra.cpwarehouse.service.item.GetItemByIdCommand;
import io.github.edmaputra.cpwarehouse.service.variant.UpdateVariantCommand;
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
    private final VariantMapper variantMapper;
    private final CommandExecutor commandExecutor;

    @Override
    @Transactional
    public VariantResponse execute(Request request) {
        log.info("Updating variant with ID: {}", request.id());

        // Find existing variant
        Variant variant = variantRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", request.id()));

        // If priceAdjustment is being updated, validate final price
        ItemDetailResponse item = commandExecutor.execute(GetItemByIdCommand.class, variant.getItemId());
        BigDecimal finalPrice = calculateFinalPrice(request, item, variant);

        // Update entity from request
        variantMapper.updateEntityFromRequest(request.request(), variant);
        variant.preUpdate();

        // Save and return
        Variant updatedVariant = variantRepository.save(variant);
        log.info("Variant updated successfully: {}", updatedVariant.getId());

        VariantResponse response = variantMapper.toResponse(updatedVariant);
        response.setFinalPrice(finalPrice);

        return response;
    }

    private static BigDecimal calculateFinalPrice(Request request, ItemDetailResponse item, Variant variant) {
        BigDecimal finalPrice = item.getBasePrice().add(variant.getPriceAdjustment());

        if (request.request().getPriceAdjustment() != null) {
            BigDecimal priceAdjustment = request.request().getPriceAdjustment();
            finalPrice = item.getBasePrice().add(priceAdjustment);

            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOperationException(
                        String.format("Final price cannot be negative. Base price: %s, Price adjustment: %s, Final price: %s",
                                item.getBasePrice(), priceAdjustment, finalPrice));
            }
        }

        return finalPrice;
    }
}
