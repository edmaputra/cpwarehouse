package io.github.edmaputra.cpwarehouse.common;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Retryable(
    retryFor = OptimisticLockingFailureException.class,
    maxAttempts = 5,
    backoff = @Backoff(
        delay = 500,
        multiplier = 2.0,
        maxDelay = 10000
    )
)
public @interface CommonRetryable {
}
