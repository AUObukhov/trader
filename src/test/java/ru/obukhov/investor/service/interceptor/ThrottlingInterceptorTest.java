package ru.obukhov.investor.service.interceptor;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.obukhov.investor.config.QueryThrottleProperties;
import ru.obukhov.investor.config.UrlLimit;
import ru.obukhov.investor.test.utils.TimeTestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpUrl.class, Request.class, Response.class, Interceptor.Chain.class})
// https://github.com/mockito/mockito/issues/1562
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class ThrottlingInterceptorTest {

    private HttpUrl url;
    private Interceptor.Chain chain;

    @Before
    public void setUp() throws IOException {
        this.url = PowerMockito.mock(HttpUrl.class);
        Request request = PowerMockito.mock(Request.class);
        Response response = PowerMockito.mock(Response.class);
        this.chain = PowerMockito.mock(Interceptor.Chain.class);

        PowerMockito.when(request.url()).thenReturn(url);
        PowerMockito.when(chain.request()).thenReturn(request);
        PowerMockito.when(chain.proceed(any(Request.class))).thenReturn(response);
    }

    @Test
    @SuppressWarnings("java:S2699") // Sonar warning "Tests should include assertions"
    public void intercept_doesNotThrottle_whenNoOpenApiPrefix() throws Exception {

        when(url.pathSegments()).thenReturn(Arrays.asList("orders", "market-order"));

        final QueryThrottleProperties queryThrottleProperties = new QueryThrottleProperties();
        queryThrottleProperties.setInterval(1000L);
        queryThrottleProperties.setLimits(Collections.singletonList(
                new UrlLimit(Arrays.asList("orders", "market-order"), 50)
        ));

        final long maximumNotThrottledTime = 100;

        final ThrottlingInterceptor interceptor = new ThrottlingInterceptor(queryThrottleProperties);

        final int limit = queryThrottleProperties.getLimits().get(0).getLimit() + 10;
        for (int i = 0; i < limit; i++) {
            TimeTestUtils.executeAndAssertFaster(() -> interceptor.intercept(chain), maximumNotThrottledTime);
        }

    }

    @Test
    @SuppressWarnings("java:S2699") // Sonar warning "Tests should include assertions"
    public void intercept_throttles_onlyWhenLimitIsReached() throws Exception {

        when(url.pathSegments()).thenReturn(Arrays.asList("openapi", "market", "candles"));

        final QueryThrottleProperties queryThrottleProperties = new QueryThrottleProperties();
        queryThrottleProperties.setInterval(1000L);
        queryThrottleProperties.setLimits(Collections.singletonList(
                new UrlLimit(Collections.singletonList("market"), 120)
        ));

        final long maximumNotThrottledTime = 30;
        final long minimumThrottledTime = queryThrottleProperties.getInterval() - 200;

        final ThrottlingInterceptor interceptor = new ThrottlingInterceptor(queryThrottleProperties);

        final int limit = queryThrottleProperties.getLimits().get(0).getLimit();
        for (int i = 0; i < limit; i++) {
            TimeTestUtils.executeAndAssertFaster(() -> interceptor.intercept(chain), maximumNotThrottledTime);
        }

        TimeTestUtils.executeAndAssertSlower(() -> interceptor.intercept(chain), minimumThrottledTime);

    }

    @Test
    @SuppressWarnings("java:S2699") // Sonar warning "Tests should include assertions"
    public void intercept_throttlesByLowestLimit() throws Exception {

        when(url.pathSegments()).thenReturn(Arrays.asList("openapi", "orders", "market-order"));

        final QueryThrottleProperties queryThrottleProperties = new QueryThrottleProperties();
        queryThrottleProperties.setInterval(1000L);
        queryThrottleProperties.setLimits(Arrays.asList(
                new UrlLimit(Collections.singletonList("orders"), 100),
                new UrlLimit(Arrays.asList("orders", "market-order"), 50)
        ));

        final long maximumNotThrottledTime = 30;
        final long minimumThrottledTime = queryThrottleProperties.getInterval() - 200;

        final ThrottlingInterceptor interceptor = new ThrottlingInterceptor(queryThrottleProperties);

        final int limit = queryThrottleProperties.getLimits().get(1).getLimit();
        for (int i = 0; i < limit; i++) {
            TimeTestUtils.executeAndAssertFaster(() -> interceptor.intercept(chain), maximumNotThrottledTime);
        }

        TimeTestUtils.executeAndAssertSlower(() -> interceptor.intercept(chain), minimumThrottledTime);

    }

    @Test
    @SuppressWarnings("java:S2699") // Sonar warning "Tests should include assertions"
    public void intercept_throttlesByCommonLimit() throws Exception {

        final List<String> limitOrderSegments = Arrays.asList("openapi", "orders", "limit-order");
        final List<String> marketOrderSegments = Arrays.asList("openapi", "orders", "market-order");

        final QueryThrottleProperties queryThrottleProperties = new QueryThrottleProperties();
        queryThrottleProperties.setInterval(1000L);
        queryThrottleProperties.setLimits(Arrays.asList(
                new UrlLimit(Collections.singletonList("orders"), 100),
                new UrlLimit(Arrays.asList("orders", "limit-order"), 90),
                new UrlLimit(Arrays.asList("orders", "market-order"), 90)
        ));

        final long maximumNotThrottledTime = 30;
        final long minimumThrottledTime = queryThrottleProperties.getInterval() - 200;

        final ThrottlingInterceptor interceptor = new ThrottlingInterceptor(queryThrottleProperties);

        final int limit = queryThrottleProperties.getLimits().get(0).getLimit() / 2;
        for (int i = 0; i < limit; i++) {
            when(url.pathSegments()).thenReturn(limitOrderSegments);
            TimeTestUtils.executeAndAssertFaster(() -> interceptor.intercept(chain), maximumNotThrottledTime);
            when(url.pathSegments()).thenReturn(marketOrderSegments);
            TimeTestUtils.executeAndAssertFaster(() -> interceptor.intercept(chain), maximumNotThrottledTime);
        }

        TimeTestUtils.executeAndAssertSlower(() -> interceptor.intercept(chain), minimumThrottledTime);

    }

}