package ru.obukhov.investor.service.interceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.obukhov.investor.config.QueryThrottleProperties;
import ru.obukhov.investor.config.UrlLimit;
import ru.obukhov.investor.util.ThrottledCounter;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ThrottlingInterceptor implements Interceptor {

    private static final String URL_FIRST_SEGMENT = "openapi";

    private final Map<UrlLimit, ThrottledCounter> counters;

    public ThrottlingInterceptor(QueryThrottleProperties queryThrottleProperties) {
        final Long interval = queryThrottleProperties.getInterval();
        final List<UrlLimit> limits = queryThrottleProperties.getLimits();

        this.counters = new HashMap<>();
        for (UrlLimit limit : limits) {
            ThrottledCounter counter = new ThrottledCounter(interval, limit.getLimit());
            this.counters.put(limit, counter);
        }
    }

    @NotNull
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        HttpUrl url = chain.request().url();

        if (URL_FIRST_SEGMENT.equals(url.pathSegments().get(0))) {
            incrementCounters(url);
        } else {
            log.debug("Unknown path \"" + url.encodedPath() + "\". No throttling");
        }

        return chain.proceed(chain.request());
    }

    private void incrementCounters(HttpUrl url) {
        for (Map.Entry<UrlLimit, ThrottledCounter> entry : counters.entrySet()) {
            if (entry.getKey().matchesUrl(url)) {
                Duration throttled = entry.getValue().increment();
                log.trace("Counter \"{}\" throttled {} ms", entry.getKey(), throttled.toMillis());
            }
        }
    }

}