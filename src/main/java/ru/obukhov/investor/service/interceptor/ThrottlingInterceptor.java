package ru.obukhov.investor.service.interceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.obukhov.investor.util.ThrottledCounter;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
public class ThrottlingInterceptor implements Interceptor {

    private final ThrottledCounter throttledCounter;

    public ThrottlingInterceptor(@Value("${query.throttle.interval}") Long interval,
                                 @Value("${query.throttle.limit}") Integer maxValue) {
        throttledCounter = new ThrottledCounter(interval, maxValue);
    }

    @NotNull
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Duration throttled = throttledCounter.increment();
        log.trace("Throttled {} ms", throttled.toMillis());

        return chain.proceed(chain.request());
    }

}