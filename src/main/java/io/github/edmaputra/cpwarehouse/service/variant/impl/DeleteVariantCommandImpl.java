package io.github.edmaputra.cpwarehouse.service.variant.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.repository.VariantRepository;
import io.github.edmaputra.cpwarehouse.service.variant.DeleteVariantCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of DeleteVariantCommand.
 * Use case: Soft delete a variant by setting isActive to false.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteVariantCommandImpl implements DeleteVariantCommand {

  private final VariantRepository variantRepository;

  @Override
  @Transactional
  public Void execute(String id) {
    log.info("Soft deleting variant with ID: {}", id);

    Variant variant = variantRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", id));

    variant.setIsActive(false);
    variant.preUpdate();

    variantRepository.save(variant);
    log.info("Variant soft deleted successfully: {}", id);

    return null;
  }
}
