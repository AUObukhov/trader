package ru.obukhov.trader.config.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.test.utils.AssertUtils;

@ActiveProfiles("test")
class TradingPropertiesContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EnableConfigurationPropertiesConfiguration.class);

    @Test
    void beanCreating_whenPropertiesFilled() {
        this.contextRunner
                .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());

                    final TradingProperties tradingProperties = context.getBean(TradingProperties.class);

                    Assertions.assertEquals("i identify myself as token", tradingProperties.getToken());
                });
    }

    @Test
    void beanCreationFails_whenTokenIsNull() {
        this.contextRunner.run(context -> AssertUtils.assertContextStartupFailed(context, "trading.token", "token is mandatory"));
    }

    @EnableConfigurationProperties(TradingProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}