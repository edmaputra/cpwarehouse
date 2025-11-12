# Retry Debugging Guide

This guide explains how to debug and monitor the retry process in stock operations.

## 1. Track Current Retry Attempt

You can see which retry attempt is currently executing by checking the logs. The retry tracking has been added to all stock command implementations:

### Log Output Example:
```log
2025-11-12 16:41:39 INFO  - Adjusting stock stock-123 with type: IN, quantity: 10 [Retry attempt: 1/5]
2025-11-12 16:41:39 INFO  - Adjusting stock stock-123 with type: IN, quantity: 10 [Retry attempt: 2/5]
2025-11-12 16:41:39 INFO  - Adjusting stock stock-123 with type: IN, quantity: 10 [Retry attempt: 3/5]
```

### In Code:
The current implementation in all three command classes uses `RetrySynchronizationManager`:

```java
@Override
@Transactional
@Retryable(
    retryFor = OptimisticLockingFailureException.class,
    maxAttempts = 5,
    backoff = @Backoff(delay = 100, multiplier = 2, maxDelay = 2000)
)
public StockResponse execute(Request request) {
    // Get current retry context for debugging
    RetryContext context = RetrySynchronizationManager.getContext();
    int retryCount = context != null ? context.getRetryCount() : 0;
    
    log.info("Processing operation for stock {} [Retry attempt: {}/5]", 
        request.stockId(), retryCount + 1);
    
    // ... rest of implementation
}
```

## 2. Handle Exhausted Retries

When all retry attempts are exhausted, a `@Recover` method is called automatically. This is where you can:
- Log detailed error information
- Send alerts to monitoring systems
- Trigger circuit breakers
- Queue for manual review

### Recovery Method Example:
```java
@Recover
public StockResponse recoverFromOptimisticLockingFailure(
    OptimisticLockingFailureException e, Request request) {
    
    log.error("RETRY EXHAUSTED: Failed to adjust stock {} after 5 attempts " +
            "due to optimistic locking conflicts. StockId: {}, MovementType: {}, Quantity: {}. " +
            "This indicates high contention on this stock record.",
        request.stockId(),
        request.stockId(),
        request.adjustRequest().getMovementType(),
        request.adjustRequest().getQuantity(),
        e);
    
    // Optional: Send to monitoring system
    // monitoringService.sendAlert("High contention detected", stockId);
    
    // Optional: Trigger circuit breaker
    // circuitBreaker.trip(stockId);
    
    // Throw user-friendly exception
    throw new InvalidOperationException(
        String.format("Unable to adjust stock %s after multiple attempts " +
            "due to concurrent modifications. Please try again later.", 
            request.stockId()),
        e);
}
```

## 3. Monitoring Retry Behavior

### Using Application Logs:

**Filter for retry attempts:**
```bash
# Linux/Mac
grep "Retry attempt" application.log

# PowerShell
Select-String -Path application.log -Pattern "Retry attempt"
```

**Find exhausted retries:**
```bash
# Linux/Mac
grep "RETRY EXHAUSTED" application.log

# PowerShell
Select-String -Path application.log -Pattern "RETRY EXHAUSTED"
```

### Using Spring Boot Actuator:

Add metrics to track retry statistics:
```java
@Component
public class RetryMetrics {
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void onRetry(RetryEvent event) {
        meterRegistry.counter("stock.retry.attempts",
            "operation", event.getOperation(),
            "attempt", String.valueOf(event.getRetryCount())
        ).increment();
    }
    
    @EventListener
    public void onRetryExhausted(RetryExhaustedEvent event) {
        meterRegistry.counter("stock.retry.exhausted",
            "operation", event.getOperation()
        ).increment();
    }
}
```

## 4. Retry Configuration Details

Current retry strategy for all stock operations:

| Parameter | Value | Description |
|-----------|-------|-------------|
| **maxAttempts** | 5 | Total attempts (1 initial + 4 retries) |
| **Base Delay** | 100ms | Initial wait before first retry |
| **Multiplier** | 2 | Exponential backoff multiplier |
| **Max Delay** | 2000ms | Maximum wait between retries |

**Retry Timeline:**
- Attempt 1: Immediate
- Attempt 2: Wait 100ms (0.1s)
- Attempt 3: Wait 200ms (0.2s)
- Attempt 4: Wait 400ms (0.4s)
- Attempt 5: Wait 800ms (0.8s)
- **Total Max Time**: ~1.5 seconds

## 5. Common Scenarios

### Scenario 1: Normal Operation (No Contention)
```
[Retry attempt: 1/5] -> Success
Total time: ~10ms
```

### Scenario 2: Light Contention
```
[Retry attempt: 1/5] -> OptimisticLockingFailureException
Wait 100ms...
[Retry attempt: 2/5] -> Success
Total time: ~110ms
```

### Scenario 3: High Contention
```
[Retry attempt: 1/5] -> OptimisticLockingFailureException
Wait 100ms...
[Retry attempt: 2/5] -> OptimisticLockingFailureException
Wait 200ms...
[Retry attempt: 3/5] -> OptimisticLockingFailureException
Wait 400ms...
[Retry attempt: 4/5] -> Success
Total time: ~710ms
```

### Scenario 4: Exhausted Retries (Extreme Contention)
```
[Retry attempt: 1/5] -> OptimisticLockingFailureException
[Retry attempt: 2/5] -> OptimisticLockingFailureException
[Retry attempt: 3/5] -> OptimisticLockingFailureException
[Retry attempt: 4/5] -> OptimisticLockingFailureException
[Retry attempt: 5/5] -> OptimisticLockingFailureException
RETRY EXHAUSTED -> @Recover method called
Total time: ~1500ms
Returns: 500 Internal Server Error (wrapped as InvalidOperationException)
```

## 6. Debugging Tips

### Enable DEBUG logging for Spring Retry:
```yaml
logging:
  level:
    org.springframework.retry: DEBUG
    io.github.edmaputra.cpwarehouse.service.stock: DEBUG
```

### Add custom retry listener:
```java
@Component
public class StockRetryListener implements RetryListener {
    @Override
    public <T, E extends Throwable> void onError(
            RetryContext context, 
            RetryCallback<T, E> callback, 
            Throwable throwable) {
        log.warn("Retry attempt {} failed for operation: {}", 
            context.getRetryCount() + 1,
            context.getAttribute("name"),
            throwable);
    }
}
```

## 7. Production Monitoring

### Key Metrics to Track:
1. **Retry Rate**: Number of operations requiring retries
2. **Retry Exhaustion Rate**: Operations failing after all retries
3. **Average Retry Count**: How many retries typically needed
4. **Stock Contention Hotspots**: Which stockIds have most retries

### Alert Thresholds:
- **Warning**: Retry rate > 5%
- **Critical**: Retry exhaustion rate > 0.1%
- **Emergency**: Any stock with > 10 exhausted retries in 1 minute

## 8. Troubleshooting High Retry Rates

If you see many retries:

1. **Check concurrent load** on specific stocks
2. **Review transaction isolation** level
3. **Consider sharding** hot stocks across multiple records
4. **Implement request throttling** for high-contention items
5. **Add caching** for read-heavy operations
6. **Consider MongoDB replica set** for full transaction support

## 9. Testing Retry Behavior

You can simulate contention in tests:

```java
@Test
public void testRetryBehavior() {
    // Create contention scenario
    List<CompletableFuture<Void>> futures = IntStream.range(0, 10)
        .mapToObj(i -> CompletableFuture.runAsync(() -> {
            adjustStockCommand.execute(new Request(stockId, adjustRequest));
        }))
        .collect(Collectors.toList());
    
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    
    // Check logs for retry attempts
}
```

## Summary

- ✅ Current retry attempt visible in logs: `[Retry attempt: X/5]`
- ✅ Exhausted retries logged as: `RETRY EXHAUSTED`
- ✅ Exponential backoff prevents thundering herd
- ✅ Recovery methods allow custom error handling
- ✅ Easy to monitor and alert on retry behavior

For production use, consider:
- Adding custom metrics
- Implementing circuit breakers
- Setting up alerts for high retry rates
- Monitoring with tools like Prometheus + Grafana
