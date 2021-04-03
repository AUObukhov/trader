package ru.obukhov.trader.common.service.impl;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.config.QueryThrottleProperties;
import ru.obukhov.trader.config.UrlLimit;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class ThrottlingInterceptorUnitTest extends BaseMockedTest {

    @Mock
    private HttpUrl url;
    @Mock
    private Request request;
    @Mock
    private Response response;
    @Mock
    private Interceptor.Chain chain;

    @BeforeEach
    public void setUp() throws IOException {
        Mockito.when(request.url()).thenReturn(url);
        Mockito.when(chain.request()).thenReturn(request);
        Mockito.when(chain.proceed(ArgumentMatchers.any(Request.class))).thenReturn(response);
    }

    @Test
    void intercept_throwsIllegalStateException_whenAttemptsCountExceeds() throws Exception {

        Mockito.when(url.pathSegments()).thenReturn(Arrays.asList("orders", "market-order"));

        final QueryThrottleProperties queryThrottleProperties =
                createQueryThrottleProperties(1000, 1, 30, 120);
        queryThrottleProperties.setLimits(Collections.singletonList(
                new UrlLimit(Arrays.asList("orders", "market-order"), 50)
        ));

        Mockito.when(chain.proceed(ArgumentMatchers.any(Request.class)))
                .thenThrow(new RuntimeException("exception for test"));
        final ThrottlingInterceptor interceptor = new ThrottlingInterceptor(queryThrottleProperties);

        AssertUtils.assertThrowsWithMessage(() -> interceptor.intercept(chain),
                IllegalStateException.class,
                "Failed to retry for 30 times");

    }

    @Test
    void intercept_doesNotThrottle_whenNoOpenApiPrefix() throws Exception {

        Mockito.when(url.pathSegments()).thenReturn(Arrays.asList("orders", "market-order"));

        final QueryThrottleProperties queryThrottleProperties =
                createQueryThrottleProperties(1000, 5000, 30, 120);
        queryThrottleProperties.setLimits(Collections.singletonList(
                new UrlLimit(Arrays.asList("orders", "market-order"), 50)
        ));

        final long maximumNotThrottledTime = 100;

        final ThrottlingInterceptor interceptor = new ThrottlingInterceptor(queryThrottleProperties);

        final int limit = queryThrottleProperties.getLimits().get(0).getLimit() + 10;
        for (int i = 0; i < limit; i++) {
            AssertUtils.assertFaster(() -> interceptor.intercept(chain), maximumNotThrottledTime);
        }

    }

    @Test
    void intercept_throttles_onlyWhenLimitIsReached() throws Exception {

        Mockito.when(url.pathSegments()).thenReturn(Arrays.asList("openapi", "market", "candles"));

        final QueryThrottleProperties queryThrottleProperties =
                createQueryThrottleProperties(1000, 5000, 30, 120);
        queryThrottleProperties.setLimits(Collections.singletonList(
                new UrlLimit(Collections.singletonList("market"), 120)
        ));

        final long maximumNotThrottledTime = 60;
        final long minimumThrottledTime = queryThrottleProperties.getInterval() - 250;

        final ThrottlingInterceptor interceptor = new ThrottlingInterceptor(queryThrottleProperties);

        final int limit = queryThrottleProperties.getLimits().get(0).getLimit();
        for (int i = 0; i < limit; i++) {
            AssertUtils.assertFaster(() -> interceptor.intercept(chain), maximumNotThrottledTime);
        }

        AssertUtils.assertSlower(() -> interceptor.intercept(chain), minimumThrottledTime);

    }

    @Test
    void intercept_throttlesByLowestLimit() throws Exception {

        Mockito.when(url.pathSegments()).thenReturn(Arrays.asList("openapi", "orders", "market-order"));

        final QueryThrottleProperties queryThrottleProperties =
                createQueryThrottleProperties(1000, 5000, 30, 120);
        queryThrottleProperties.setLimits(Arrays.asList(
                new UrlLimit(Collections.singletonList("orders"), 100),
                new UrlLimit(Arrays.asList("orders", "market-order"), 50)
        ));

        final long maximumNotThrottledTime = 75;
        final long minimumThrottledTime = queryThrottleProperties.getInterval() - 300;

        final ThrottlingInterceptor interceptor = new ThrottlingInterceptor(queryThrottleProperties);

        final int limit = queryThrottleProperties.getLimits().get(1).getLimit();
        for (int i = 0; i < limit; i++) {
            AssertUtils.assertFaster(() -> interceptor.intercept(chain), maximumNotThrottledTime);
        }

        AssertUtils.assertSlower(() -> interceptor.intercept(chain), minimumThrottledTime);

    }

    @Test
    void intercept_throttlesByCommonLimit() throws Exception {

        final List<String> limitOrderSegments = Arrays.asList("openapi", "orders", "limit-order");
        final List<String> marketOrderSegments = Arrays.asList("openapi", "orders", "market-order");

        final QueryThrottleProperties queryThrottleProperties =
                createQueryThrottleProperties(1000, 5000, 30, 120);
        queryThrottleProperties.setLimits(Arrays.asList(
                new UrlLimit(Collections.singletonList("orders"), 100),
                new UrlLimit(Arrays.asList("orders", "limit-order"), 90),
                new UrlLimit(Arrays.asList("orders", "market-order"), 90)
        ));

        final long maximumNotThrottledTime = 75;
        final long minimumThrottledTime = queryThrottleProperties.getInterval() - 250;

        final ThrottlingInterceptor interceptor = new ThrottlingInterceptor(queryThrottleProperties);

        final int limit = queryThrottleProperties.getLimits().get(0).getLimit() / 2;
        for (int i = 0; i < limit; i++) {
            Mockito.when(url.pathSegments()).thenReturn(limitOrderSegments);
            AssertUtils.assertFaster(() -> interceptor.intercept(chain), maximumNotThrottledTime);
            Mockito.when(url.pathSegments()).thenReturn(marketOrderSegments);
            AssertUtils.assertFaster(() -> interceptor.intercept(chain), maximumNotThrottledTime);
        }

        AssertUtils.assertSlower(() -> interceptor.intercept(chain), minimumThrottledTime);

    }

    @Test
    void intercept_throttlesByDefaultLimit_whenNoMatchingCounter() throws Exception {

        Mockito.when(url.pathSegments()).thenReturn(Arrays.asList("openapi", "market", "candles"));

        final QueryThrottleProperties queryThrottleProperties =
                createQueryThrottleProperties(1000, 5000, 30, 50);
        queryThrottleProperties.setLimits(Collections.singletonList(
                new UrlLimit(Collections.singletonList("portfolio"), 120)
        ));

        final long maximumNotThrottledTime = 45;
        final long minimumThrottledTime = queryThrottleProperties.getInterval() - 200;

        final ThrottlingInterceptor interceptor = new ThrottlingInterceptor(queryThrottleProperties);

        final int limit = queryThrottleProperties.getDefaultLimit();
        for (int i = 0; i < limit; i++) {
            AssertUtils.assertFaster(() -> interceptor.intercept(chain), maximumNotThrottledTime);
        }

        AssertUtils.assertSlower(() -> interceptor.intercept(chain), minimumThrottledTime);

    }

    private QueryThrottleProperties createQueryThrottleProperties(long interval,
                                                                  int retryInterval,
                                                                  int attemptsCount,
                                                                  int defaultLimit) {

        QueryThrottleProperties queryThrottleProperties = new QueryThrottleProperties();
        queryThrottleProperties.setInterval(interval);
        queryThrottleProperties.setRetryInterval(retryInterval);
        queryThrottleProperties.setAttemptsCount(attemptsCount);
        queryThrottleProperties.setDefaultLimit(defaultLimit);
        return queryThrottleProperties;

    }

}