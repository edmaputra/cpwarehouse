package io.github.edmaputra.cpwarehouse.service.item.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Item;
import io.github.edmaputra.cpwarehouse.dto.request.ItemCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.ItemResponse;
import io.github.edmaputra.cpwarehouse.exception.DuplicateResourceException;
import io.github.edmaputra.cpwarehouse.mapper.ItemMapper;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import io.github.edmaputra.cpwarehouse.service.item.CreateItemCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CreateItemCommand.
 * Use case: Create a new item with SKU uniqueness validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateItemCommandImpl implements CreateItemCommand {

  private final ItemRepository itemRepository;
  private final ItemMapper itemMapper;

  @Override
  @Transactional
  public ItemResponse execute(ItemCreateRequest request) {
    log.info("Creating new item with SKU: {}", request.getSku());

    // Check for duplicate SKU
    if (itemRepository.findBySku(request.getSku()).isPresent()) {
      throw new DuplicateResourceException("Item", "SKU", request.getSku());
    }

    // Map request to entity
    Item item = itemMapper.toEntity(request);
    item.prePersist();

    // Save and return
    Item savedItem = itemRepository.save(item);
    log.info("Item created successfully with ID: {} and SKU: {}", savedItem.getId(), savedItem.getSku());

    return itemMapper.toResponse(savedItem);
  }
}
