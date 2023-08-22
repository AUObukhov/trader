package ru.obukhov.trader.config.properties;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import ru.obukhov.trader.common.model.transform.DoubleToQuotationConverter;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

class ScheduledBotsPropertiesContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

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
                    Assertions.assertEquals("2000124699", botConfig1.accountId());
                    Assertions.assertEquals("BBG005HLTYH9", botConfig1.figi());
                    Assertions.assertEquals(CandleInterval.CANDLE_INTERVAL_5_MIN, botConfig1.candleInterval());
                    AssertUtils.assertEquals(0.003, botConfig1.commission());
                    Assertions.assertEquals(StrategyType.CROSS, botConfig1.strategyType());

                    final Map<String, Object> expectedStrategyParams1 = Map.of(
                            "minimumProfit", 0.1,
                            "movingAverageType", "SMA",
                            "order", 1,
                            "smallWindow", 50,
                            "bigWindow", 200,
                            "indexCoefficient", 0.5,
                            "greedy", true
                    );
                    AssertUtils.assertEquals(expectedStrategyParams1, botConfig1.strategyParams());

                    final BotConfig botConfig2 = botConfigs.get(1);
                    Assertions.assertEquals("2000124698", botConfig2.accountId());
                    Assertions.assertEquals("BBG005HLTYH9", botConfig2.figi());
                    Assertions.assertEquals(CandleInterval.CANDLE_INTERVAL_5_MIN, botConfig2.candleInterval());
                    AssertUtils.assertEquals(0.003, botConfig2.commission());
                    Assertions.assertEquals(StrategyType.CONSERVATIVE, botConfig2.strategyType());

                    final Map<String, Object> expectedStrategyParams2 = Map.of("minimumProfit", 0.1);
                    AssertUtils.assertEquals(expectedStrategyParams2, botConfig2.strategyParams());
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
    void beanCreationFails_whenAccountIdIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.bot-configs[0].figi:BBG000B9XRY4")
                .withPropertyValues("scheduled-bot.bot-configs[0].candle-interval:CANDLE_INTERVAL_1_MIN")
                .withPropertyValues("scheduled-bot.bot-configs[0].commission:0")
                .withPropertyValues("scheduled-bot.bot-configs[0].strategy-type:cross")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("accountId is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenFigiIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.bot-configs[0].accountId:2000124699")
                .withPropertyValues("scheduled-bot.bot-configs[0].candle-interval:CANDLE_INTERVAL_1_MIN")
                .withPropertyValues("scheduled-bot.bot-configs[0].commission:0")
                .withPropertyValues("scheduled-bot.bot-configs[0].strategy-type:cross")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("figi is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenCandleIntervalIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.bot-configs[0].accountId:2000124699")
                .withPropertyValues("scheduled-bot.bot-configs[0].figi:BBG000B9XRY4")
                .withPropertyValues("scheduled-bot.bot-configs[0].commission:0.003")
                .withPropertyValues("scheduled-bot.bot-configs[0].strategy-type:cross")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("candleInterval is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenCommissionIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.bot-configs[0].accountId:2000124699")
                .withPropertyValues("scheduled-bot.bot-configs[0].figi:BBG000B9XRY4")
                .withPropertyValues("scheduled-bot.bot-configs[0].candle-interval:CANDLE_INTERVAL_1_MIN")
                .withPropertyValues("scheduled-bot.bot-configs[0].strategy-type:cross")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("commission is mandatory"));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void beanCreationFails_whenStrategyTypeIsNull() {
        contextRunner
                .withPropertyValues("scheduled-bot.bot-configs[0].accountId:2000124699")
                .withPropertyValues("scheduled-bot.bot-configs[0].figi:BBG000B9XRY4")
                .withPropertyValues("scheduled-bot.bot-configs[0].candle-interval:CANDLE_INTERVAL_1_MIN")
                .withPropertyValues("scheduled-bot.bot-configs[0].commission:0")
                .run(AssertUtils.createBindValidationExceptionAssertConsumer("strategyType is mandatory"));
    }

    @EnableConfigurationProperties(ScheduledBotsProperties.class)
    private static class TestConfiguration {
        @Bean
        @SuppressWarnings("unused")
        public ConversionService conversionService() {
            DefaultConversionService service = new DefaultConversionService();
            service.addConverter(new DoubleToQuotationConverter());
            service.addConverter(new StringToQuotationConverter());
            return service;
        }

    }

    public static class StringToQuotationConverter implements Converter<String, Quotation> {
        @Override
        public Quotation convert(@NotNull final String source) {
            final BigDecimal bigDecimal = DecimalUtils.setDefaultScale(new BigDecimal(source));
            return QuotationUtils.newQuotation(bigDecimal);
        }
    }

}