package io.github.edmaputra.cpwarehouse.service.checkout;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.request.PaymentRequest;
import io.github.edmaputra.cpwarehouse.dto.response.PaymentResponse;

/**
 * Command to process payment.
 * Validates payment amount and commits or releases stock.
 */
public interface ProcessPaymentCommand extends Command<ProcessPaymentCommand.Request, PaymentResponse> {

  /**
   * Request wrapper for payment with checkout ID.
   */
  record Request(String checkoutId, PaymentRequest paymentRequest) {
  }
}
