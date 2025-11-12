package io.github.edmaputra.cpwarehouse.dto.response;

import io.github.edmaputra.cpwarehouse.domain.entity.CheckoutItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for checkout response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {

    private String id;
    private String itemId;
    private String variantId;
    private String stockId;
    private Integer quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalPrice;
    private String reservationId;
    private CheckoutItem.CheckoutStatus status;
    private String customerId;
    private String checkoutReference;
    private Long createdAt;
    private Long updatedAt;
}
