package io.github.edmaputra.cpwarehouse.service.variant.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.VariantMapper;
import io.github.edmaputra.cpwarehouse.repository.VariantRepository;
import io.github.edmaputra.cpwarehouse.service.variant.GetVariantByIdCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of GetVariantByIdCommand.
 * Use case: Get a variant by its ID.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetVariantByIdCommandImpl implements GetVariantByIdCommand {

    private final VariantRepository variantRepository;
    private final VariantMapper variantMapper;

    @Override
    @Transactional(readOnly = true)
    public VariantResponse execute(String id) {
        log.info("Getting variant by ID: {}", id);

        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", id));

        log.info("Found variant: {} with SKU: {}", variant.getId(), variant.getVariantSku());

        return variantMapper.toResponse(variant);
    }
}
