package io.github.edmaputra.cpwarehouse.service.item.command.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Item;
import io.github.edmaputra.cpwarehouse.dto.response.ItemResponse;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.ItemMapper;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import io.github.edmaputra.cpwarehouse.service.item.command.GetItemBySkuCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of GetItemBySkuCommand.
 * Use case: Get item by SKU.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetItemBySkuCommandImpl implements GetItemBySkuCommand {

  private final ItemRepository itemRepository;
  private final ItemMapper itemMapper;

  @Override
  @Transactional(readOnly = true)
  public ItemResponse execute(String sku) {
    log.info("Fetching item by SKU: {}", sku);

    Item item = itemRepository.findBySku(sku).orElseThrow(() -> new ResourceNotFoundException("Item", "SKU", sku));

    return itemMapper.toResponse(item);
  }
}
