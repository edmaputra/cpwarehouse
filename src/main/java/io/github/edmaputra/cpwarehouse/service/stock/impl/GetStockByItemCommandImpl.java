package io.github.edmaputra.cpwarehouse.service.stock.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;
import io.github.edmaputra.cpwarehouse.mapper.StockMapper;
import io.github.edmaputra.cpwarehouse.repository.StockRepository;
import io.github.edmaputra.cpwarehouse.service.stock.GetStockByItemCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of GetStockByItemCommand.
 * Retrieves all stock records for a specific item (including all variants).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetStockByItemCommandImpl implements GetStockByItemCommand {

  private final StockRepository stockRepository;
  private final StockMapper stockMapper;

  @Override
  @Transactional(readOnly = true)
  public List<StockResponse> execute(String itemId) {
    log.info("Getting stock for item: {}", itemId);

    List<Stock> stocks = stockRepository.findByItemId(itemId);

    log.info("Found {} stock record(s) for item: {}", stocks.size(), itemId);

    return stocks.stream()
        .map(stockMapper::toResponse)
        .collect(Collectors.toList());
  }
}
