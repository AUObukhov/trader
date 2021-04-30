package ru.obukhov.trader.common.service.impl;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.obukhov.trader.common.util.ThrottledCounter;
import ru.obukhov.trader.config.QueryThrottleProperties;
import ru.obukhov.trader.config.UrlLimit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ThrottlingInterceptor implements Interceptor {

    private static final String URL_FIRST_SEGMENT = "openapi";

    private final QueryThrottleProperties queryThrottleProperties;
    private final Map<UrlLimit, ThrottledCounter> counters;
    private final ThrottledCounter defaultCounter;

    public ThrottlingInterceptor(QueryThrottleProperties queryThrottleProperties) {
        this.queryThrottleProperties = queryThrottleProperties;
        this.counters = createCounters(queryThrottleProperties);
        this.defaultCounter =
                new ThrottledCounter(queryThrottleProperties.getInterval(), queryThrottleProperties.getDefaultLimit());
    }

    private Map<UrlLimit, ThrottledCounter> createCounters(QueryThrottleProperties queryThrottleProperties) {
        long interval = queryThrottleProperties.getInterval();

        Map<UrlLimit, ThrottledCounter> result = new HashMap<>();
        for (UrlLimit limit : queryThrottleProperties.getLimits()) {
            ThrottledCounter counter = new ThrottledCounter(interval, limit.getLimit());
            result.put(limit, counter);
        }
        return result;
    }

    @NotNull
    @Override
    public Response intercept(Interceptor.Chain chain) {
        HttpUrl url = chain.request().url();

        if (URL_FIRST_SEGMENT.equals(url.pathSegments().get(0))) {
            incrementCounters(url);
        } else {
            log.debug("Unknown path \"" + url.encodedPath() + "\". No throttling");
        }

        return proceed(chain);
    }

    private Response proceed(Interceptor.Chain chain) {
        return proceed(chain, 1);
    }

    private Response proceed(Interceptor.Chain chain, int attemptNumber) {
        if (attemptNumber > queryThrottleProperties.getAttemptsCount()) {
            String message = String.format("Failed to retry for %s times", queryThrottleProperties.getAttemptsCount());
            throw new IllegalStateException(message);
        }

        try {
            return chain.proceed(chain.request());
        } catch (Exception exception) {
            final String path = chain.request().url().encodedPath();
            log.warn("Request to {} failed. Retry after {} milliseconds",
                    path, queryThrottleProperties.getRetryInterval(), exception);
            try {
                TimeUnit.MILLISECONDS.sleep(queryThrottleProperties.getRetryInterval());
                return proceed(chain, attemptNumber + 1);
            } catch (InterruptedException interruptedException) {
                log.warn("Wait before retry call {} interrupted. Retry right not", path, interruptedException);
                Thread.currentThread().interrupt();
                return proceed(chain, attemptNumber + 1);
            }
        }
    }

    private void incrementCounters(HttpUrl url) {
        Map<UrlLimit, ThrottledCounter> matchingCounters = counters.entrySet().stream()
                .filter(entry -> entry.getKey().matchesUrl(url))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (matchingCounters.isEmpty()) {
            defaultCounter.increment();
        } else {
            for (Map.Entry<UrlLimit, ThrottledCounter> entry : matchingCounters.entrySet()) {
                if (entry.getKey().matchesUrl(url)) {
                    entry.getValue().increment();
                }
            }
        }
    }

}