package ru.obukhov.trader.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.trader.config.model.UrlLimit;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.util.List;

class QueryThrottlePropertiesIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EnableConfigurationPropertiesConfiguration.class)
            .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void beanCreating_whenPropertiesFilled() {
        contextRunner.run(context -> {
            Assertions.assertNull(context.getStartupFailure());

            final QueryThrottleProperties queryThrottleProperties = context.getBean(QueryThrottleProperties.class);

            AssertUtils.assertEquals(60000L, queryThrottleProperties.getInterval());

            final List<UrlLimit> limits = queryThrottleProperties.getLimits();
            Assertions.assertEquals(3, limits.size());

            final UrlLimit urlLimit0 = limits.get(0);
            Assertions.assertEquals(1, urlLimit0.getSegments().size());
            Assertions.assertEquals("market", urlLimit0.getSegments().get(0));
            Assertions.assertEquals(120, urlLimit0.getLimit());

            final UrlLimit urlLimit1 = limits.get(1);
            Assertions.assertEquals(1, urlLimit1.getSegments().size());
            Assertions.assertEquals("orders", urlLimit1.getSegments().get(0));
            Assertions.assertEquals(100, urlLimit1.getLimit());

            final UrlLimit urlLimit2 = limits.get(2);
            Assertions.assertEquals(2, urlLimit2.getSegments().size());
            Assertions.assertEquals("orders", urlLimit2.getSegments().get(0));
            Assertions.assertEquals("limit-order", urlLimit2.getSegments().get(1));
            Assertions.assertEquals(50, urlLimit2.getLimit());

            Assertions.assertEquals(5000, queryThrottleProperties.getRetryInterval());

            Assertions.assertEquals(30, queryThrottleProperties.getAttemptsCount());
        });

    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenIntervalIsNegative() {
        contextRunner.withPropertyValues("query.throttle.interval: -1")
                .run(AssertUtils.createContextFailureAssertConsumer("interval must be positive"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenIntervalIsZero() {
        contextRunner.withPropertyValues("query.throttle.interval: 0")
                .run(AssertUtils.createContextFailureAssertConsumer("interval must be positive"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenLimitsAreNull() {
        new ApplicationContextRunner()
                .withUserConfiguration(EnableConfigurationPropertiesConfiguration.class)
                .withPropertyValues("query.throttle.interval: 60000")
                .withPropertyValues("query.throttle.retry-interval: 5000")
                .withPropertyValues("query.throttle.attempts-count: 30")
                .withPropertyValues("query.throttle.default-limit: 120")
                .run(AssertUtils.createContextFailureAssertConsumer("limits must not be null"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenLimitsAreEmpty() {
        contextRunner.withPropertyValues("query.throttle.limits:")
                .run(AssertUtils.createContextFailureAssertConsumer("limits must not be empty"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenRetryIntervalIsNegative() {
        contextRunner.withPropertyValues("query.throttle.retry-interval: -1")
                .run(AssertUtils.createContextFailureAssertConsumer("retryInterval must be positive"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenRetryIntervalIsZero() {
        contextRunner.withPropertyValues("query.throttle.retry-interval: 0")
                .run(AssertUtils.createContextFailureAssertConsumer("retryInterval must be positive"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenAttemptsCountIsNegative() {
        contextRunner.withPropertyValues("query.throttle.attempts-count: -1")
                .run(AssertUtils.createContextFailureAssertConsumer("attemptsCount must be positive"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenAttemptsCountIsZero() {
        contextRunner.withPropertyValues("query.throttle.attempts-count: 0")
                .run(AssertUtils.createContextFailureAssertConsumer("attemptsCount must be positive"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenSegmentsAreEmpty() {
        contextRunner.withPropertyValues("query.throttle.limits[0].segments=")
                .withPropertyValues("query.throttle.limits[0].limit: 1")
                .run(AssertUtils.createContextFailureAssertConsumer("segments must not be empty"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenUrlLimitIsNegative() {
        contextRunner.withPropertyValues("query.throttle.limits[0].segments[0]: market")
                .withPropertyValues("query.throttle.limits[0].limits=-1")
                .run(AssertUtils.createContextFailureAssertConsumer("limit must be positive"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenUrlLimitIsZero() {
        contextRunner.withPropertyValues("query.throttle.interval: 60000")
                .withPropertyValues("query.throttle.limits[0].segments[0]: market")
                .withPropertyValues("query.throttle.limits[0].limits=0")
                .withPropertyValues("query.throttle.retry-interval: 1")
                .withPropertyValues("query.throttle.attempts-count: 1")
                .withPropertyValues("query.throttle.default-limit: 1")
                .run(AssertUtils.createContextFailureAssertConsumer("limit must be positive"));
    }

    @EnableConfigurationProperties(QueryThrottleProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}