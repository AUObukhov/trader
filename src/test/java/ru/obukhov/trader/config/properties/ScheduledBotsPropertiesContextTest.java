package ru.obukhov.trader.config.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.trader.market.model.CandleResolution;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BotConfig;

import java.util.List;
import java.util.Map;

class ScheduledBotsPropertiesContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EnableConfigurationPropertiesConfiguration.class);

    @Test
    void beanCreated_andValuesInitialized_whenPropertiesFilled() {
        contextRunner
                .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());

                    final ScheduledBotsProperties scheduledBotsProperties = context.getBean(ScheduledBotsProperties.class);

                    final List<BotConfig> botConfigs = scheduledBotsProperties.getBotConfigs();
                    Assertions.assertEquals(2, botConfigs.size());

                    final BotConfig botConfig1 = botConfigs.get(0);
                    Assertions.assertEquals("FXIT", botConfig1.getTicker());
                    Assertions.assertEquals(CandleResolution._5MIN, botConfig1.getCandleResolution());
                    Assertions.assertEquals(StrategyType.CROSS, botConfig1.getStrategyType());

                    final Map<String, Object> expectedStrategyParams1 = Map.of(
                            "minimumProfit", 0.1,
                            "movingAverageType", "SMA",
                            "order", 1,
                            "smallWindow", 50,
                            "bigWindow", 200,
                            "indexCoefficient", 0.5,
                            "greedy", true
                    );
                    AssertUtils.assertEquals(expectedStrategyParams1, botConfig1.getStrategyParams());

                    final BotConfig botConfig2 = botConfigs.get(1);
                    Assertions.assertEquals("FXIT", botConfig2.getTicker());
                    Assertions.assertEquals(CandleResolution._5MIN, botConfig2.getCandleResolution());
                    Assertions.assertEquals(StrategyType.CONSERVATIVE, botConfig2.getStrategyType());

                    final Map<String, Object> expectedStrategyParams2 = Map.of("minimumProfit", 0.1);
                    AssertUtils.assertEquals(expectedStrategyParams2, botConfig2.getStrategyParams());
                });
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenBotConfigsIsNull() {
        contextRunner.run(AssertUtils.createBindValidationExceptionAssertConsumer("botConfigs is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenBotConfigsIsEmpty() {
        contextRunner
                .withPropertyValues("scheduled-bot.bot-configs:")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("botConfigs is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenTickerIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.bot-configs[0].candle-resolution:1min")
                .withPropertyValues("scheduled-bot.bot-configs[0].commission:0")
                .withPropertyValues("scheduled-bot.bot-configs[0].strategy-type:cross")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("ticker is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenCandleResolutionIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.bot-configs[0].ticker:FXCN")
                .withPropertyValues("scheduled-bot.bot-configs[0].commission:0")
                .withPropertyValues("scheduled-bot.bot-configs[0].strategy-type:cross")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("candleResolution is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenCommissionIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.bot-configs[0].ticker:FXCN")
                .withPropertyValues("scheduled-bot.bot-configs[0].candle-resolution:1min")
                .withPropertyValues("scheduled-bot.bot-configs[0].strategy-type:cross")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("commission is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenStrategyTypeIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.bot-configs[0].ticker:FXCN")
                .withPropertyValues("scheduled-bot.bot-configs[0].candle-resolution:1min")
                .withPropertyValues("scheduled-bot.bot-configs[0].commission:0")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("strategyType is mandatory"));
    }

    @EnableConfigurationProperties(ScheduledBotsProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}