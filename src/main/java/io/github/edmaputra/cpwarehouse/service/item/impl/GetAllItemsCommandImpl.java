package io.github.edmaputra.cpwarehouse.service.item.impl;

import io.github.edmaputra.cpwarehouse.dto.response.ItemResponse;
import io.github.edmaputra.cpwarehouse.mapper.ItemMapper;
import io.github.edmaputra.cpwarehouse.repository.ItemRepository;
import io.github.edmaputra.cpwarehouse.service.item.GetAllItemsCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of GetAllItemsCommand.
 * Use case: Get all items with pagination, filtering, and search.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetAllItemsCommandImpl implements GetAllItemsCommand {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ItemResponse> execute(GetAllItemsCommand.Request request) {
        Pageable pageable = request.pageable();
        Boolean activeOnly = request.activeOnly();
        String search = request.search();

        log.info("Fetching items - page: {}, size: {}, activeOnly: {}, search: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                activeOnly,
                search);

        // Use custom repository method with MongoTemplate for dynamic filtering
        Page<ItemResponse> result =
                itemRepository.findAllWithFilters(pageable, activeOnly, search).map(itemMapper::toResponse);

        log.info("Fetched {} items", result.getNumberOfElements());
        return result;
    }
}
