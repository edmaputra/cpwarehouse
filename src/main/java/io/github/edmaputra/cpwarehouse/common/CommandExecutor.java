package io.github.edmaputra.cpwarehouse.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Command executor that retrieves and executes commands from the Spring ApplicationContext.
 * This allows for dependency injection in command classes and centralized command execution.
 * The controller uses this executor to call command interfaces, and Spring resolves
 * the actual implementation at runtime.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommandExecutor {

  private final ApplicationContext applicationContext;

  /**
   * Execute a command by retrieving it from the ApplicationContext and calling its execute method.
   * The command can be an interface, and Spring will resolve the concrete implementation.
   *
   * @param commandClass the command interface/class to execute
   * @param request      the request object to pass to the command
   * @param <R>          the request type
   * @param <T>          the response type
   * @return the response from the command execution
   * @throws RuntimeException if the command execution fails
   */
  public <R, T> T execute(Class<? extends Command<R, T>> commandClass, R request) throws RuntimeException {
    log.debug("Executing command: {}", commandClass.getSimpleName());

    Command<R, T> command = applicationContext.getBean(commandClass);
    T result = command.execute(request);

    log.debug("Command {} executed successfully", commandClass.getSimpleName());
    return result;
  }
}
