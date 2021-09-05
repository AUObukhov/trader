package ru.obukhov.trader.config.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.trader.test.utils.AssertUtils;

class SimulationPropertiesContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EnableConfigurationPropertiesConfiguration.class)
            .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void beanCreated_andValueInitialized_whenPropertiesFilled() {
        contextRunner.withPropertyValues("simulation.thread-count: 7")
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());

                    final SimulationProperties simulationProperties = context.getBean(SimulationProperties.class);

                    AssertUtils.assertEquals(7, simulationProperties.getThreadCount());
                });
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenThreadCountIsNull() {
        contextRunner.withPropertyValues("simulation.thread-count:")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("threadCount is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenThreadCountIsNegative() {
        contextRunner.withPropertyValues("simulation.thread-count: -1")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("threadCount must be positive"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenThreadCountIsZero() {
        contextRunner.withPropertyValues("simulation.thread-count: 0")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("threadCount must be positive"));
    }

    @EnableConfigurationProperties(SimulationProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}