package io.github.edmaputra.cpwarehouse.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or initializing a stock record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create or initialize a stock record")
public class StockCreateRequest {

    @NotNull(message = "Item ID is required")
    @Schema(description = "Item ID for this stock", example = "6748a1b2c3d4e5f678901234")
    private String itemId;

    @Schema(description = "Variant ID (optional, null = stock for base item)", example = "6748a1b2c3d4e5f678901235", nullable = true)
    private String variantId; // Optional - null means stock for base item

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    @Schema(description = "Initial stock quantity", example = "100", minimum = "0")
    private Integer quantity;

    @Size(max = 100, message = "Warehouse location must not exceed 100 characters")
    @Schema(description = "Warehouse location code", example = "WH-A-01", nullable = true)
    private String warehouseLocation;
}
