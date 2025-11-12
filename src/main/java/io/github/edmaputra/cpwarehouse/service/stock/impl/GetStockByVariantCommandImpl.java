package io.github.edmaputra.cpwarehouse.service.stock.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.StockMapper;
import io.github.edmaputra.cpwarehouse.repository.StockRepository;
import io.github.edmaputra.cpwarehouse.service.stock.GetStockByVariantCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of GetStockByVariantCommand.
 * Retrieves stock for a specific variant.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetStockByVariantCommandImpl implements GetStockByVariantCommand {

  private final StockRepository stockRepository;
  private final StockMapper stockMapper;

  @Override
  @Transactional(readOnly = true)
  public StockResponse execute(String variantId) {
    log.info("Getting stock for variant: {}", variantId);

    Stock stock = stockRepository.findByVariantId(variantId)
        .orElseThrow(() -> new ResourceNotFoundException("Stock", "variantId", variantId));

    log.info("Stock found for variant: {}", variantId);

    return stockMapper.toResponse(stock);
  }
}
