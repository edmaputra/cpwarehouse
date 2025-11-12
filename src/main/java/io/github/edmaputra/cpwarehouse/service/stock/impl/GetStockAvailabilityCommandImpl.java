package io.github.edmaputra.cpwarehouse.service.stock.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.dto.response.StockAvailabilityResponse;
import io.github.edmaputra.cpwarehouse.exception.ResourceNotFoundException;
import io.github.edmaputra.cpwarehouse.mapper.StockMapper;
import io.github.edmaputra.cpwarehouse.repository.StockRepository;
import io.github.edmaputra.cpwarehouse.service.stock.GetStockAvailabilityCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of GetStockAvailabilityCommand.
 * Checks stock availability for a specific stock record.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetStockAvailabilityCommandImpl implements GetStockAvailabilityCommand {

  private final StockRepository stockRepository;
  private final StockMapper stockMapper;

  @Override
  @Transactional(readOnly = true)
  public StockAvailabilityResponse execute(String stockId) {
    log.info("Checking stock availability for stock: {}", stockId);

    Stock stock = stockRepository.findById(stockId)
        .orElseThrow(() -> new ResourceNotFoundException("Stock", "id", stockId));

    StockAvailabilityResponse response = stockMapper.toAvailabilityResponse(stock);

    log.info("Stock {} availability: {}, Available quantity: {}",
        stockId, response.getIsAvailable(), response.getAvailableQuantity());

    return response;
  }
}
