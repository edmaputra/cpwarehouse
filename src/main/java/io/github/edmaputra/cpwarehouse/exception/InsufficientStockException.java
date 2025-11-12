package io.github.edmaputra.cpwarehouse.exception;

/**
 * Exception thrown when there is insufficient stock for the requested operation.
 * Typically used when attempting to reserve or sell more than available quantity.
 */
public class InsufficientStockException extends RuntimeException {

  public InsufficientStockException(String stockId, Integer requested, Integer available) {
    super(String.format("Insufficient stock for ID %s. Requested: %d, Available: %d", 
        stockId, requested, available));
  }
}
