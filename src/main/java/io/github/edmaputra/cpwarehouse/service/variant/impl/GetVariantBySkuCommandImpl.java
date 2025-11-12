package io.github.edmaputra.cpwarehouse.service.variant.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.VariantMapper;
import io.github.edmaputra.cpwarehouse.repository.VariantRepository;
import io.github.edmaputra.cpwarehouse.service.variant.GetVariantBySkuCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of GetVariantBySkuCommand.
 * Use case: Get a variant by its variant SKU.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetVariantBySkuCommandImpl implements GetVariantBySkuCommand {

    private final VariantRepository variantRepository;
    private final VariantMapper variantMapper;

    @Override
    @Transactional(readOnly = true)
    public VariantResponse execute(String variantSku) {
        log.info("Getting variant by SKU: {}", variantSku);

        Variant variant = variantRepository.findByVariantSku(variantSku)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "SKU", variantSku));

        log.info("Found variant: {} with SKU: {}", variant.getId(), variant.getVariantSku());

        return variantMapper.toResponse(variant);
    }
}
