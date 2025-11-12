package io.github.edmaputra.cpwarehouse.dto.response;

import io.github.edmaputra.cpwarehouse.domain.entity.CheckoutItem;
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
public class PaymentResponse {

    private String checkoutId;
    private CheckoutItem.CheckoutStatus status;
    private BigDecimal totalPrice;
    private BigDecimal paidAmount;
    private Boolean paymentSuccess;
    private String paymentReference;
    private String message;
    private Long processedAt;
}
