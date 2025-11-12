package io.github.edmaputra.cpwarehouse.service.item.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Item;
import io.github.edmaputra.cpwarehouse.dto.response.ItemDetailResponse;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.ItemMapper;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import io.github.edmaputra.cpwarehouse.service.item.GetItemByIdCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of GetItemByIdCommand.
 * Use case: Get item details by ID.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetItemByIdCommandImpl implements GetItemByIdCommand {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional(readOnly = true)
    public ItemDetailResponse execute(String id) {
        log.info("Fetching item by ID: {}", id);

        Item item = itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Item", "ID", id));

        return itemMapper.toDetailResponse(item);
    }
}
