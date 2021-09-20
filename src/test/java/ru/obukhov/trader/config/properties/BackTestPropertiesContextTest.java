package ru.obukhov.trader.config.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.trader.test.utils.AssertUtils;

class BackTestPropertiesContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EnableConfigurationPropertiesConfiguration.class)
            .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void beanCreated_andValueInitialized_whenPropertiesFilled() {
        contextRunner.withPropertyValues("back-test.thread-count: 7")
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());

                    final BackTestProperties backTestProperties = context.getBean(BackTestProperties.class);

                    Assertions.assertEquals(7, backTestProperties.getThreadCount());
                });
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenThreadCountIsNull() {
        contextRunner.withPropertyValues("back-test.thread-count:")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("threadCount is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenThreadCountIsNegative() {
        contextRunner.withPropertyValues("back-test.thread-count: -1")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("threadCount must be positive"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenThreadCountIsZero() {
        contextRunner.withPropertyValues("back-test.thread-count: 0")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("threadCount must be positive"));
    }

    @EnableConfigurationProperties(BackTestProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}