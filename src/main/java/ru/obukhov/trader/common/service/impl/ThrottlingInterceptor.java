package ru.obukhov.trader.common.service.impl;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.obukhov.trader.common.util.ThrottledCounter;
import ru.obukhov.trader.config.model.UrlLimit;
import ru.obukhov.trader.config.properties.QueryThrottleProperties;

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

    public ThrottlingInterceptor(final QueryThrottleProperties queryThrottleProperties) {
        this.queryThrottleProperties = queryThrottleProperties;
        this.counters = createCounters(queryThrottleProperties);
        this.defaultCounter = new ThrottledCounter(
                queryThrottleProperties.getDefaultLimit(),
                queryThrottleProperties.getInterval()
        );
    }

    private Map<UrlLimit, ThrottledCounter> createCounters(final QueryThrottleProperties queryThrottleProperties) {
        final long interval = queryThrottleProperties.getInterval();

        final Map<UrlLimit, ThrottledCounter> result = new HashMap<>();
        for (final UrlLimit limit : queryThrottleProperties.getLimits()) {
            final ThrottledCounter counter = new ThrottledCounter(limit.getLimit(), interval);
            result.put(limit, counter);
        }
        return result;
    }

    @NotNull
    @Override
    public Response intercept(final Interceptor.Chain chain) {
        final HttpUrl url = chain.request().url();

        if (URL_FIRST_SEGMENT.equals(url.pathSegments().get(0))) {
            incrementCounters(url);
        } else {
            log.debug("Unknown path \"" + url.encodedPath() + "\". No throttling");
        }

        return proceed(chain);
    }

    private Response proceed(final Interceptor.Chain chain) {
        return proceed(chain, 1);
    }

    private Response proceed(final Interceptor.Chain chain, final int attemptNumber) {
        if (attemptNumber > queryThrottleProperties.getAttemptsCount()) {
            final String message = String.format("Failed to retry for %s times", queryThrottleProperties.getAttemptsCount());
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
                log.warn("Wait before retry call {} interrupted. Retry right now", path, interruptedException);
                Thread.currentThread().interrupt();
                return proceed(chain, attemptNumber + 1);
            }
        }
    }

    private void incrementCounters(final HttpUrl url) {
        final Map<UrlLimit, ThrottledCounter> matchingCounters = counters.entrySet().stream()
                .filter(entry -> entry.getKey().matchesUrl(url))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (matchingCounters.isEmpty()) {
            defaultCounter.increment();
        } else {
            for (final Map.Entry<UrlLimit, ThrottledCounter> entry : matchingCounters.entrySet()) {
                if (entry.getKey().matchesUrl(url)) {
                    entry.getValue().increment();
                }
            }
        }
    }

}