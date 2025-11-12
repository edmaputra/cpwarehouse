package io.github.edmaputra.cpwarehouse.service.stock.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement;
import io.github.edmaputra.cpwarehouse.dto.response.StockMovementResponse;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.StockMapper;
import io.github.edmaputra.cpwarehouse.repository.StockMovementRepository;
import io.github.edmaputra.cpwarehouse.service.stock.GetStockMovementByIdCommand;
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
public class GetStockMovementByIdCommandImpl implements GetStockMovementByIdCommand {

    private final StockMovementRepository repository;
    private final StockMapper stockMapper;

    @Override
    @Transactional(readOnly = true)
    public StockMovementResponse execute(String id) {
        log.info("Getting stockMovement for id: {}", id);

        StockMovement stockMovement = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockMovement", "id", id));

        return stockMapper.toMovementResponse(stockMovement);
    }
}
