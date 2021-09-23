package ru.obukhov.trader.config.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.util.Map;

class ScheduledBotPropertiesContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EnableConfigurationPropertiesConfiguration.class);

    @Test
    void beanCreated_andValuesInitialized_whenPropertiesFilled() {
        contextRunner
                .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());

                    final ScheduledBotProperties scheduledBotProperties = context.getBean(ScheduledBotProperties.class);

                    Assertions.assertTrue(scheduledBotProperties.isEnabled());

                    final BotConfig botConfig = scheduledBotProperties.getBotConfig();
                    Assertions.assertEquals("FXIT", botConfig.getTicker());
                    Assertions.assertEquals(CandleResolution._5MIN, botConfig.getCandleResolution());
                    Assertions.assertEquals(StrategyType.CROSS, botConfig.getStrategyType());

                    Map<String, Object> expectedStrategyParams = Map.of(
                            "minimumProfit", 0.1,
                            "movingAverageType", "SMA",
                            "order", 1,
                            "smallWindow", 50,
                            "bigWindow", 200,
                            "indexCoefficient", 0.5,
                            "greedy", true
                    );
                    AssertUtils.assertEquals(expectedStrategyParams, botConfig.getStrategyParams());
                });
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenBotConfigsIsNull() {
        contextRunner.run(AssertUtils.createBindValidationExceptionAssertConsumer("botConfig is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenTickerIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.bot-config.candle-resolution:1min")
                .withPropertyValues("scheduled-bot.bot-config.strategy-type:cross")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("ticker is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenCandleResolutionIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.bot-config.ticker:FXCN")
                .withPropertyValues("scheduled-bot.bot-config.strategy-type:cross")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("candleResolution is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenStrategyTypeIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.bot-config.ticker:FXCN")
                .withPropertyValues("scheduled-bot.bot-config.candle-resolution:1min")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("strategyType is mandatory"));
    }

    @EnableConfigurationProperties(ScheduledBotProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}