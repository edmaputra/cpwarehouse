package io.github.edmaputra.cpwarehouse.service.stock.impl;

import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement;
import io.github.edmaputra.cpwarehouse.dto.response.StockMovementResponse;
import io.github.edmaputra.cpwarehouse.mapper.StockMapper;
import io.github.edmaputra.cpwarehouse.repository.StockMovementRepository;
import io.github.edmaputra.cpwarehouse.service.stock.GetStockMovementsCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of GetStockMovementsCommand.
 * Retrieves stock movement history with optional filtering by movement type.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetStockMovementsCommandImpl implements GetStockMovementsCommand {

  private final StockMovementRepository stockMovementRepository;
  private final StockMapper stockMapper;

  @Override
  @Transactional(readOnly = true)
  public Page<StockMovementResponse> execute(Request request) {
    log.info("Getting stock movements for stock: {}, movementType: {}, page: {}",
        request.stockId(), request.movementType(), request.pageable().getPageNumber());

    Page<StockMovement> movements;

    if (request.movementType() != null) {
      // Filter by movement type
      movements = stockMovementRepository.findByStockIdAndMovementType(
          request.stockId(),
          request.movementType(),
          request.pageable()
      );
    } else {
      // Get all movements for stock
      movements = stockMovementRepository.findByStockId(
          request.stockId(),
          request.pageable()
      );
    }

    log.info("Found {} stock movement(s) for stock: {}",
        movements.getTotalElements(), request.stockId());

    return movements.map(stockMapper::toMovementResponse);
  }
}
