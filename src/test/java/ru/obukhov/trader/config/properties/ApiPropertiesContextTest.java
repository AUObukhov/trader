package ru.obukhov.trader.config.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.trader.test.utils.AssertUtils;

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

                    Assertions.assertEquals("http://localhost", apiProperties.host());
                    Assertions.assertEquals(8081, apiProperties.port());
                });
    }

    @Test
    void beanCreationFails_whenHostIsNull() {
        this.contextRunner.run(context -> AssertUtils.assertContextStartupFailed(context, "host is mandatory"));
    }

    @EnableConfigurationProperties(ApiProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}