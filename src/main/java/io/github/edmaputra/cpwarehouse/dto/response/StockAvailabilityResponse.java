package io.github.edmaputra.cpwarehouse.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for stock availability check response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stock availability check result")
public class StockAvailabilityResponse {

    @Schema(description = "Stock ID", example = "6748a1b2c3d4e5f678901238")
    private String stockId;
    
    @Schema(description = "Total quantity", example = "100")
    private Integer quantity;
    
    @Schema(description = "Reserved quantity", example = "10")
    private Integer reservedQuantity;
    
    @Schema(description = "Available quantity", example = "90")
    private Integer availableQuantity;
    
    @Schema(description = "Availability flag", example = "true")
    private Boolean isAvailable;
}
