package io.github.edmaputra.cpwarehouse.service.variant.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.repository.VariantRepository;
import io.github.edmaputra.cpwarehouse.service.variant.HardDeleteVariantCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of HardDeleteVariantCommand.
 * Use case: Permanently delete a variant from the database.
 * This should be used with caution and typically protected with authorization.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HardDeleteVariantCommandImpl implements HardDeleteVariantCommand {

  private final VariantRepository variantRepository;

  @Override
  @Transactional
  public Void execute(String id) {
    log.warn("Permanently deleting variant with ID: {}", id);

    Variant variant = variantRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", id));

    variantRepository.delete(variant);
    log.warn("Variant permanently deleted: {}", id);

    return null;
  }
}
