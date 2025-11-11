package io.github.edmaputra.cpwarehouse.common;

/**
 * Command interface for implementing Command Pattern with Single Responsibility.
 * Each command should handle one specific business operation.
 *
 * @param <R> Request type (DTO/POJO)
 * @param <T> Response type (DTO/POJO)
 */
public interface Command<R, T> {

  /**
   * Execute the command with the given request.
   *
   * @param request the request object
   * @return the response object
   */
  T execute(R request);
}
