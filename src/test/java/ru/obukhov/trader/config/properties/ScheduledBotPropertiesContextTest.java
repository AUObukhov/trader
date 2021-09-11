package ru.obukhov.trader.config.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.TradingConfig;
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

                    final TradingConfig tradingConfig = scheduledBotProperties.getTradingConfig();
                    Assertions.assertEquals("FXIT", tradingConfig.getTicker());
                    Assertions.assertEquals(CandleResolution._5MIN, tradingConfig.getCandleResolution());
                    Assertions.assertEquals(StrategyType.CROSS, tradingConfig.getStrategyType());

                    Map<String, Object> expectedStrategyParams = Map.of(
                            "minimumProfit", 0.1,
                            "movingAverageType", "SMA",
                            "order", 1,
                            "smallWindow", 50,
                            "bigWindow", 200,
                            "indexCoefficient", 0.5,
                            "greedy", true
                    );
                    AssertUtils.assertEquals(expectedStrategyParams, tradingConfig.getStrategyParams());
                });
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenTradingConfigsIsNull() {
        contextRunner.run(AssertUtils.createBindValidationExceptionAssertConsumer("tradingConfig is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenTickerIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.trading-config.candle-resolution:1min")
                .withPropertyValues("scheduled-bot.trading-config.strategy-type:cross")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("ticker is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenCandleResolutionIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.trading-config.ticker:FXCN")
                .withPropertyValues("scheduled-bot.trading-config.strategy-type:cross")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("candleResolution is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenStrategyTypeIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.trading-config.ticker:FXCN")
                .withPropertyValues("scheduled-bot.trading-config.candle-resolution:1min")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("strategyType is mandatory"));
    }

    @EnableConfigurationProperties(ScheduledBotProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}