package io.github.edmaputra.cpwarehouse.service.variant.command.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;
import io.github.edmaputra.cpwarehouse.mapper.VariantMapper;
import io.github.edmaputra.cpwarehouse.repository.VariantRepository;
import io.github.edmaputra.cpwarehouse.service.variant.command.GetVariantsByItemIdCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of GetVariantsByItemIdCommand.
 * Use case: Get all variants for a specific item.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetVariantsByItemIdCommandImpl implements GetVariantsByItemIdCommand {

  private final VariantRepository variantRepository;
  private final VariantMapper variantMapper;

  @Override
  @Transactional(readOnly = true)
  public List<VariantResponse> execute(String itemId) {
    log.info("Getting all variants for item ID: {}", itemId);

    List<Variant> variants = variantRepository.findByItemId(itemId);

    log.info("Found {} variants for item ID: {}", variants.size(), itemId);

    return variants.stream()
        .map(variantMapper::toResponse)
        .toList();
  }
}
