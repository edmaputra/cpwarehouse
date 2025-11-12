package io.github.edmaputra.cpwarehouse.dto.response;

import io.github.edmaputra.cpwarehouse.domain.entity.CheckoutItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for payment response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment processing result")
public class PaymentResponse {

    @Schema(description = "Checkout ID", example = "6748a1b2c3d4e5f678901237")
    private String checkoutId;
    
    @Schema(description = "New checkout status after payment", example = "PAID", allowableValues = {"RESERVED", "PAID", "CANCELLED", "EXPIRED"})
    private CheckoutItem.CheckoutStatus status;
    
    @Schema(description = "Original total price", example = "165.00")
    private BigDecimal totalPrice;
    
    @Schema(description = "Amount paid", example = "165.00")
    private BigDecimal paidAmount;
    
    @Schema(description = "Payment success flag", example = "true")
    private Boolean paymentSuccess;
    
    @Schema(description = "Payment reference", example = "PAY-2024-001-TXN", nullable = true)
    private String paymentReference;
    
    @Schema(description = "Result message", example = "Payment successful, stock committed")
    private String message;
    
    @Schema(description = "Processing timestamp (epoch millis)", example = "1700000000000")
    private Long processedAt;
}
