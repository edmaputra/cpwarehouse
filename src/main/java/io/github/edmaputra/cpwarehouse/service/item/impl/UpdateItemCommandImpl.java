package io.github.edmaputra.cpwarehouse.service.item.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Item;
import io.github.edmaputra.cpwarehouse.dto.request.ItemUpdateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.ItemResponse;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.ItemMapper;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import io.github.edmaputra.cpwarehouse.service.item.UpdateItemCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UpdateItemCommand.
 * Use case: Update existing item.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateItemCommandImpl implements UpdateItemCommand {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemResponse execute(UpdateItemCommand.Request request) {
        String id = request.id();
        ItemUpdateRequest updateRequest = request.updateRequest();

        log.info("Updating item with ID: {}", id);

        // Find existing item
        Item item = itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Item", "ID", id));

        // Note: SKU cannot be updated (it's immutable after creation)

        // Update entity
        itemMapper.updateEntityFromRequest(updateRequest, item);
        item.preUpdate();

        // Save and return
        Item updatedItem = itemRepository.save(item);
        log.info("Item updated successfully with ID: {}", id);

        return itemMapper.toResponse(updatedItem);
    }
}
