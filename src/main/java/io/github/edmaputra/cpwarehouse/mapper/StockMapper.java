package io.github.edmaputra.cpwarehouse.mapper;

import io.github.edmaputra.cpwarehouse.domain.entity.Stock;
import io.github.edmaputra.cpwarehouse.domain.entity.StockMovement;
import io.github.edmaputra.cpwarehouse.dto.request.StockCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.StockAvailabilityResponse;
import io.github.edmaputra.cpwarehouse.dto.response.StockMovementResponse;
import io.github.edmaputra.cpwarehouse.dto.response.StockResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for converting between Stock entity and DTOs.
 * MapStruct will generate the implementation at compile time.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StockMapper {

    /**
     * Convert StockCreateRequest to Stock entity.
     *
     * @param request the create request
     * @return Stock entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservedQuantity", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Stock toEntity(StockCreateRequest request);

    /**
     * Convert Stock entity to StockResponse.
     * Calculates availableQuantity from quantity - reservedQuantity.
     *
     * @param stock the stock entity
     * @return StockResponse DTO
     */
    @Mapping(target = "availableQuantity", expression = "java(stock.getAvailableQuantity())")
    StockResponse toResponse(Stock stock);

    /**
     * Convert Stock entity to StockAvailabilityResponse.
     *
     * @param stock the stock entity
     * @return StockAvailabilityResponse DTO
     */
    @Mapping(source = "id", target = "stockId")
    @Mapping(target = "availableQuantity", expression = "java(stock.getAvailableQuantity())")
    @Mapping(target = "isAvailable", expression = "java(stock.isAvailable())")
    StockAvailabilityResponse toAvailabilityResponse(Stock stock);

    /**
     * Convert StockMovement entity to StockMovementResponse.
     *
     * @param stockMovement the stock movement entity
     * @return StockMovementResponse DTO
     */
    StockMovementResponse toMovementResponse(StockMovement stockMovement);
}
