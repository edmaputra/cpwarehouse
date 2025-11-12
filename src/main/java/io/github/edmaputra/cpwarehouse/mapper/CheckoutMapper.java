package io.github.edmaputra.cpwarehouse.mapper;

import io.github.edmaputra.cpwarehouse.domain.entity.CheckoutItem;
import io.github.edmaputra.cpwarehouse.dto.response.CheckoutResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for CheckoutItem entity.
 * Handles conversions between entity and response DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
public interface CheckoutMapper {

    /**
     * Convert CheckoutItem entity to CheckoutResponse DTO.
     */
    CheckoutResponse toResponse(CheckoutItem checkoutItem);
}
