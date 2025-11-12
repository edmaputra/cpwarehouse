package io.github.edmaputra.cpwarehouse.service.stock.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.StockMapper;
import io.github.edmaputra.cpwarehouse.repository.StockRepository;
import io.github.edmaputra.cpwarehouse.service.stock.GetStockByIdCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of GetStockByItemCommand.
 * Retrieves all stock records for a specific item (including all variants).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetStockByIdCommandImpl implements GetStockByIdCommand {

    private final StockRepository stockRepository;
    private final StockMapper stockMapper;

    @Override
    @Transactional(readOnly = true)
    public StockResponse execute(String id) {
        log.info("Getting stock for item: {}", id);

        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "id", id));

        return stockMapper.toResponse(stock);
    }
}
