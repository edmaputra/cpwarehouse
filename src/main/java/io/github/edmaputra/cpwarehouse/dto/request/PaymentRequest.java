package io.github.edmaputra.cpwarehouse.dto.request;

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
public class PaymentRequest {

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    private BigDecimal paymentAmount;

    @Size(max = 100, message = "Payment reference must not exceed 100 characters")
    private String paymentReference;

    @NotNull(message = "Processed by is required")
    @Size(max = 100, message = "Processed by must not exceed 100 characters")
    private String processedBy;
}
