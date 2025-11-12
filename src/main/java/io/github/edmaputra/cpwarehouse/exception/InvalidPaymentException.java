package io.github.edmaputra.cpwarehouse.exception;

/**
 * Exception thrown when payment amount is invalid.
 */
public class InvalidPaymentException extends RuntimeException {

  public InvalidPaymentException(String message) {
    super(message);
  }

  public InvalidPaymentException(String message, Throwable cause) {
    super(message, cause);
  }
}
