package io.github.edmaputra.cpwarehouse.controller;

import io.github.edmaputra.cpwarehouse.common.CommandExecutor;
import io.github.edmaputra.cpwarehouse.dto.request.CheckoutRequest;
import io.github.edmaputra.cpwarehouse.dto.request.PaymentRequest;
import io.github.edmaputra.cpwarehouse.dto.response.ApiResponse;
import io.github.edmaputra.cpwarehouse.dto.response.CheckoutResponse;
import io.github.edmaputra.cpwarehouse.dto.response.PaymentResponse;
import io.github.edmaputra.cpwarehouse.service.checkout.ProcessCheckoutCommand;
import io.github.edmaputra.cpwarehouse.service.checkout.ProcessPaymentCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Checkout and Payment operations.
 * Handles e-commerce checkout flow including stock reservation and payment processing.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CommandExecutor commandExecutor;

    /**
     * Process checkout request.
     * Checks availability and reserves stock for the customer.
     *
     * @param request the checkout request
     * @return checkout response with reservation details
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CheckoutResponse>> processCheckout(
            @Valid @RequestBody CheckoutRequest request) {

        log.info("POST /api/v1/checkout - Processing checkout for item: {}, variant: {}, quantity: {}, customer: {}",
                request.getItemId(), request.getVariantId(), request.getQuantity(), request.getCustomerId());

        ProcessCheckoutCommand.Request commandRequest = new ProcessCheckoutCommand.Request(request);
        CheckoutResponse response = commandExecutor.execute(ProcessCheckoutCommand.class, commandRequest);

        return ResponseEntity.ok(ApiResponse.success(response, "Checkout processed successfully. Stock reserved."));
    }

    /**
     * Process payment for a checkout.
     * Validates payment amount and commits or releases stock based on payment success.
     *
     * @param checkoutId the checkout ID
     * @param request    the payment request
     * @return payment response with final status
     */
    @PostMapping("/{checkoutId}/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @PathVariable String checkoutId,
            @Valid @RequestBody PaymentRequest request) {

        log.info("POST /api/v1/checkout/{}/payment - Processing payment, amount: {}",
                checkoutId, request.getPaymentAmount());

        ProcessPaymentCommand.Request commandRequest = new ProcessPaymentCommand.Request(checkoutId, request);
        PaymentResponse response = commandExecutor.execute(ProcessPaymentCommand.class, commandRequest);

        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }
}
