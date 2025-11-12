package io.github.edmaputra.cpwarehouse.service.checkout;

import io.github.edmaputra.cpwarehouse.common.Command;
import io.github.edmaputra.cpwarehouse.dto.request.CheckoutRequest;
import io.github.edmaputra.cpwarehouse.dto.response.CheckoutResponse;

/**
 * Command to process checkout request.
 * Checks availability and reserves stock.
 */
public interface ProcessCheckoutCommand extends Command<ProcessCheckoutCommand.Request, CheckoutResponse> {

  /**
   * Request wrapper for checkout.
   */
  record Request(CheckoutRequest checkoutRequest) {
  }
}
