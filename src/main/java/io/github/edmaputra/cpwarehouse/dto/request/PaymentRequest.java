package io.github.edmaputra.cpwarehouse.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for payment request.
 * Processes successful payment, validates amount, and commits stock.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to process payment and commit reserved stock")
public class PaymentRequest {

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @Schema(description = "Payment amount (must be >= checkout total)", example = "165.00", minimum = "0.01")
    private BigDecimal paymentAmount;

    @Size(max = 100, message = "Payment reference must not exceed 100 characters")
    @Schema(description = "Payment transaction reference", example = "PAY-2024-001-TXN", nullable = true)
    private String paymentReference;

    @NotNull(message = "Processed by is required")
    @Size(max = 100, message = "Processed by must not exceed 100 characters")
    @Schema(description = "Payment processor or user who processed payment", example = "stripe_gateway")
    private String processedBy;
}
