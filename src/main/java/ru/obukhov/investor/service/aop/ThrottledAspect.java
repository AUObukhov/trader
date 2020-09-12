package ru.obukhov.investor.service.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.obukhov.investor.util.ThrottledCounter;

import java.time.Duration;

@Slf4j
@Aspect
@Component
public class ThrottledAspect {

    private final ThrottledCounter throttledCounter;

    public ThrottledAspect(@Value("${query.throttle.interval}") Long interval,
                           @Value("${query.throttle.limit}") Integer maxValue) {
        throttledCounter = new ThrottledCounter(interval, maxValue);
    }

    @Around("@annotation(Throttled)")
    public Object throttle(ProceedingJoinPoint joinPoint) throws Throwable {
        Duration throttled = throttledCounter.increment();
        log.trace("Throttled {} ms", throttled.toMillis());
        return joinPoint.proceed();
    }

}