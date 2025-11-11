package io.github.edmaputra.cpwarehouse.service.item.command.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Item;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import io.github.edmaputra.cpwarehouse.service.item.command.DeleteItemCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of DeleteItemCommand.
 * Use case: Soft delete item (sets isActive to false).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteItemCommandImpl implements DeleteItemCommand {

  private final ItemRepository itemRepository;

  @Override
  @Transactional
  public Void execute(String id) {
    log.info("Soft deleting item with ID: {}", id);

    Item item = itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Item", "ID", id));

    item.setIsActive(false);
    item.preUpdate();

    itemRepository.save(item);
    log.info("Item soft deleted successfully with ID: {}", id);

    return null;
  }
}
