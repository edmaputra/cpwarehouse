package io.github.edmaputra.cpwarehouse.mapper;

import io.github.edmaputra.cpwarehouse.domain.entity.Item;
import io.github.edmaputra.cpwarehouse.dto.request.ItemCreateRequest;
import io.github.edmaputra.cpwarehouse.dto.request.ItemUpdateRequest;
import io.github.edmaputra.cpwarehouse.dto.response.ItemDetailResponse;
import io.github.edmaputra.cpwarehouse.dto.response.ItemResponse;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between Item entity and DTOs.
 * MapStruct will generate the implementation at compile time.
 * The generated mapper will be a Spring component automatically.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ItemMapper {

  /**
   * Convert ItemCreateRequest to Item entity.
   * Sets isActive to true by default for new items.
   *
   * @param request the create request
   * @return Item entity
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "isActive", constant = "true")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Item toEntity(ItemCreateRequest request);

  /**
   * Update Item entity from ItemUpdateRequest.
   * Only updates the fields present in the request.
   *
   * @param request the update request
   * @param item    the existing item to update
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "sku", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntityFromRequest(ItemUpdateRequest request, @MappingTarget Item item);

  /**
   * Convert Item entity to ItemResponse.
   *
   * @param item the item entity
   * @return ItemResponse DTO
   */
  ItemResponse toResponse(Item item);

  /**
   * Convert Item entity to ItemDetailResponse.
   *
   * @param item the item entity
   * @return ItemDetailResponse DTO
   */
  ItemDetailResponse toDetailResponse(Item item);
}
