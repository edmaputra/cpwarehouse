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
 * DTO for reserving stock.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to reserve stock for a customer")
public class StockReserveRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    @Schema(description = "Quantity to reserve", example = "3", minimum = "1")
    private Integer quantity;

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    @Schema(description = "Reference number for tracking", example = "CHECKOUT-2024-001", nullable = true)
    private String referenceNumber;

    @NotNull(message = "Created by is required")
    @Schema(description = "User who created this reservation", example = "system@cpwarehouse.io")
    private String createdBy;
}
