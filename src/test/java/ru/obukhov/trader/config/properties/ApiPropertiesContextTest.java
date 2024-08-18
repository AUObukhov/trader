package ru.obukhov.trader.config.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ApiPropertiesContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EnableConfigurationPropertiesConfiguration.class);

    @Test
    void beanCreating_whenPropertiesFilled() {
        this.contextRunner
                .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());

                    final ApiProperties apiProperties = context.getBean(ApiProperties.class);

                    Assertions.assertEquals(1000, apiProperties.throttlingInterval());
                });
    }

    @Test
    void throttlingIntervalInitializedWithDefaultValue_whenNull() {
        contextRunner.run(context -> {
            Assertions.assertNull(context.getStartupFailure());

            final ApiProperties apiProperties = context.getBean(ApiProperties.class);

            Assertions.assertEquals(60000, apiProperties.throttlingInterval());
        });
    }

    @EnableConfigurationProperties(ApiProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}