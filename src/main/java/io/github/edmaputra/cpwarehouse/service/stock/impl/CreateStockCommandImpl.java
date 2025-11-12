package io.github.edmaputra.cpwarehouse.service.stock.impl;

import io.github.edmaputra.cpwarehouse.common.CommandExecutor;
import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.dto.request.StockCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;
import io.github.edmaputra.cpwarehouse.exception.DuplicateResourceException;
import io.github.edmaputra.cpwarehouse.mapper.StockMapper;
import io.github.edmaputra.cpwarehouse.repository.StockRepository;
import io.github.edmaputra.cpwarehouse.service.item.GetItemByIdCommand;
import io.github.edmaputra.cpwarehouse.service.stock.CreateStockCommand;
import io.github.edmaputra.cpwarehouse.service.variant.GetVariantByIdCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Implementation of CreateStockCommand.
 * Creates or initializes a stock record for an item or variant.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateStockCommandImpl implements CreateStockCommand {

    private final StockRepository stockRepository;
    private final CommandExecutor commandExecutor;
    private final StockMapper stockMapper;

    @Override
    @Transactional
    public StockResponse execute(StockCreateRequest request) {
        log.info("Creating stock for itemId: {}, variantId: {}", request.getItemId(), request.getVariantId());

        // Validate item exists using CommandExecutor
        commandExecutor.execute(GetItemByIdCommand.class, request.getItemId());

        // Validate variant exists if provided
        if (StringUtils.hasText(request.getVariantId())) {
            commandExecutor.execute(GetVariantByIdCommand.class, request.getVariantId());
        }

        // Check for duplicate stock record
        if (stockRepository.existsByItemIdAndVariantId(request.getItemId(), request.getVariantId())) {
            String identifier = StringUtils.hasText(request.getVariantId())
                    ? "itemId=" + request.getItemId() + ", variantId=" + request.getVariantId()
                    : "itemId=" + request.getItemId();
            throw new DuplicateResourceException("Stock", "item-variant combination", identifier);
        }

        // Map and save
        Stock stock = stockMapper.toEntity(request);
        stock.prePersist();

        Stock savedStock = stockRepository.save(stock);
        log.info("Stock created successfully with ID: {}", savedStock.getId());

        return stockMapper.toResponse(savedStock);
    }
}
