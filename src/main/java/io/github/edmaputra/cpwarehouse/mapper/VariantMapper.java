package io.github.edmaputra.cpwarehouse.mapper;

import io.github.edmaputra.cpwarehouse.domain.entity.Variant;
import io.github.edmaputra.cpwarehouse.dto.request.VariantCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.request.VariantUpdateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.VariantResponse;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between Variant entity and DTOs.
 * MapStruct will generate the implementation at compile time.
 * The generated mapper will be a Spring component automatically.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VariantMapper {

  /**
   * Convert VariantCreateRequest to Variant entity.
   * Sets isActive to true by default for new variants.
   *
   * @param request the create request
   * @return Variant entity
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "isActive", constant = "true")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Variant toEntity(VariantCreateRequest request);

  /**
   * Update Variant entity from VariantUpdateRequest.
   * Only updates the fields present in the request.
   *
   * @param request the update request
   * @param variant the existing variant to update
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "itemId", ignore = true)
  @Mapping(target = "variantSku", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntityFromRequest(VariantUpdateRequest request, @MappingTarget Variant variant);

  /**
   * Convert Variant entity to VariantResponse.
   *
   * @param variant the variant entity
   * @return VariantResponse DTO
   */
  VariantResponse toResponse(Variant variant);
}
