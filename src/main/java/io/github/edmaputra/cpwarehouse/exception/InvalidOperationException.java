package io.github.edmaputra.cpwarehouse.exception;

/**
 * Exception thrown when an invalid business operation is attempted.
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
