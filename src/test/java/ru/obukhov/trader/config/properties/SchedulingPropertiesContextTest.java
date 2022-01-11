package ru.obukhov.trader.config.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SchedulingPropertiesContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EnableConfigurationPropertiesConfiguration.class);

    @Test
    void beanCreated_andValuesInitialized_whenPropertiesFilled() {
        contextRunner
                .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());

                    final SchedulingProperties schedulingProperties = context.getBean(SchedulingProperties.class);

                    Assertions.assertTrue(schedulingProperties.isEnabled());
                });
    }

    @EnableConfigurationProperties(SchedulingProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}