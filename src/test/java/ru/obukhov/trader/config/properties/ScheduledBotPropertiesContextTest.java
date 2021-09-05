package ru.obukhov.trader.config.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.util.Map;
import java.util.Set;

class ScheduledBotPropertiesContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EnableConfigurationPropertiesConfiguration.class)
            .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void beanCreated_andValuesInitialized_whenPropertiesFilled() {
        contextRunner.run(context -> {
            Assertions.assertNull(context.getStartupFailure());

            final ScheduledBotProperties scheduledBotProperties = context.getBean(ScheduledBotProperties.class);

            Assertions.assertTrue(scheduledBotProperties.isEnabled());
            final Set<String> tickers = scheduledBotProperties.getTickers();
            Assertions.assertEquals(Set.of("FXIT"), tickers);

            Assertions.assertEquals(CandleResolution._5MIN, scheduledBotProperties.getCandleResolution());
            Assertions.assertEquals(StrategyType.CROSS, scheduledBotProperties.getStrategyType());

            Map<String, Object> expectedStrategyParams = Map.of(
                    "minimumProfit", 0.1,
                    "movingAverageType", "SMA",
                    "order", 1,
                    "smallWindow", 50,
                    "bigWindow", 200,
                    "indexCoefficient", 0.5,
                    "greedy", true
            );
            AssertUtils.assertEquals(expectedStrategyParams, scheduledBotProperties.getStrategyParams());
        });
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenCandleResolutionIsNull() {
        contextRunner.withPropertyValues("scheduled-bot.candleResolution:")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("candleResolution is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenStrategyTypeIsNull() {
        contextRunner.withPropertyValues("scheduled-bot.strategy-type:")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("strategyType is mandatory"));
    }

    @EnableConfigurationProperties(ScheduledBotProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}