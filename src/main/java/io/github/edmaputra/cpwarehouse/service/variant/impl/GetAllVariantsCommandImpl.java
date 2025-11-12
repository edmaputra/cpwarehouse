package io.github.edmaputra.cpwarehouse.service.variant.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;
import io.github.edmaputra.cpwarehouse.mapper.VariantMapper;
import io.github.edmaputra.cpwarehouse.repository.VariantRepository;
import io.github.edmaputra.cpwarehouse.service.variant.GetAllVariantsCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of GetAllVariantsCommand.
 * Use case: Get all variants with optional filtering by item ID, active status, and search term.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetAllVariantsCommandImpl implements GetAllVariantsCommand {

    private final VariantRepository variantRepository;
    private final VariantMapper variantMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<VariantResponse> execute(Request request) {
        log.info("Getting all variants - page: {}, size: {}, itemId: {}, isActive: {}, search: {}",
                request.pageable().getPageNumber(), request.pageable().getPageSize(),
                request.itemId(), request.isActive(), request.search());

        Page<Variant> variantsPage = variantRepository.findAllWithFilters(
                request.itemId(), request.isActive(), request.search(), request.pageable());

        log.info("Found {} variants out of {} total",
                variantsPage.getNumberOfElements(), variantsPage.getTotalElements());

        return variantsPage.map(variantMapper::toResponse);
    }
}
