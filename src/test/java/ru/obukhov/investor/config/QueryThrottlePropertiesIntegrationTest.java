package ru.obukhov.investor.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.investor.test.utils.AssertUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryThrottlePropertiesIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EnableConfigurationPropertiesConfiguration.class);

    @Test
    void beanCreating_whenPropertiesFilled() {

        this.contextRunner
                .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
                .withInitializer(new ConfigFileApplicationContextInitializer())
                .run(context -> {
                    assertNull(context.getStartupFailure());

                    QueryThrottleProperties queryThrottleProperties = context.getBean(QueryThrottleProperties.class);

                    AssertUtils.assertEquals(60000L, queryThrottleProperties.getInterval());

                    List<UrlLimit> limits = queryThrottleProperties.getLimits();
                    assertEquals(3, limits.size());

                    UrlLimit urlLimit0 = limits.get(0);
                    assertEquals(1, urlLimit0.getSegments().size());
                    assertEquals("market", urlLimit0.getSegments().get(0));
                    assertEquals(120, urlLimit0.getLimit());

                    UrlLimit urlLimit1 = limits.get(1);
                    assertEquals(1, urlLimit1.getSegments().size());
                    assertEquals("orders", urlLimit1.getSegments().get(0));
                    assertEquals(100, urlLimit1.getLimit());

                    UrlLimit urlLimit2 = limits.get(2);
                    assertEquals(2, urlLimit2.getSegments().size());
                    assertEquals("orders", urlLimit2.getSegments().get(0));
                    assertEquals("limit-order", urlLimit2.getSegments().get(1));
                    assertEquals(50, urlLimit2.getLimit());

                    assertEquals(5000, queryThrottleProperties.getRetryInterval());

                    assertEquals(30, queryThrottleProperties.getAttemptsCount());
                });

    }

    @Test
    void beanCreationFails_whenIntervalIsNegative() {

        this.contextRunner
                .withPropertyValues("query.throttle.interval: -1")
                .run(context -> assertContextStartupFailed(context,
                        "query.throttle.interval", "interval must be positive")
                );

    }

    @Test
    void beanCreationFails_whenIntervalIsZero() {

        this.contextRunner
                .withPropertyValues("query.throttle.interval: 0")
                .run(context -> assertContextStartupFailed(context,
                        "query.throttle.interval", "interval must be positive")
                );

    }

    @Test
    void beanCreationFails_whenLimitsAreEmpty() {

        this.contextRunner
                .withPropertyValues("query.throttle.limits:")
                .run(context -> assertContextStartupFailed(context,
                        "query.throttle.limits", "limits must not be empty")
                );

    }

    @Test
    void beanCreationFails_whenRetryIntervalIsNegative() {

        this.contextRunner
                .withPropertyValues("query.throttle.retry-interval: -1")
                .run(context -> assertContextStartupFailed(context,
                        "query.throttle.retry-interval", "retryInterval must be positive")
                );

    }

    @Test
    void beanCreationFails_whenRetryIntervalIsZero() {

        this.contextRunner
                .withPropertyValues("query.throttle.retry-interval: 0")
                .run(context -> assertContextStartupFailed(context,
                        "query.throttle.retry-interval", "retryInterval must be positive")
                );

    }

    @Test
    void beanCreationFails_whenAttemptsCountIsNegative() {

        this.contextRunner
                .withPropertyValues("query.throttle.attempts-count: -1")
                .run(context -> assertContextStartupFailed(context,
                        "query.throttle.attempts-count", "attemptsCount must be positive")
                );

    }

    @Test
    void beanCreationFails_whenAttemptsCountIsZero() {

        this.contextRunner
                .withPropertyValues("query.throttle.attempts-count: 0")
                .run(context -> assertContextStartupFailed(context,
                        "query.throttle.attempts-count", "attemptsCount must be positive")
                );

    }

    private void assertContextStartupFailed(AssertableApplicationContext context, String... messageSubstrings) {
        Throwable startupFailure = context.getStartupFailure();

        assertNotNull(startupFailure);

        String message = getBindValidationExceptionMessage(startupFailure);
        for (String substring : messageSubstrings) {
            assertTrue(message.contains(substring));
        }
    }

    private String getBindValidationExceptionMessage(Throwable startupFailure) {
        BindValidationException bindValidationException =
                (BindValidationException) startupFailure.getCause().getCause();
        return bindValidationException.getMessage();
    }

    @EnableConfigurationProperties(QueryThrottleProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}