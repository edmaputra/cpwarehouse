package io.github.edmaputra.cpwarehouse.service.item.impl;

import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import io.github.edmaputra.cpwarehouse.service.item.HardDeleteItemCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of HardDeleteItemCommand.
 * Use case: Permanently delete item from database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HardDeleteItemCommandImpl implements HardDeleteItemCommand {

    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public Void execute(String id) {
        log.info("Hard deleting item with ID: {}", id);

        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item", "ID", id);
        }

        itemRepository.deleteById(id);
        log.info("Item permanently deleted with ID: {}", id);

        return null;
    }
}
