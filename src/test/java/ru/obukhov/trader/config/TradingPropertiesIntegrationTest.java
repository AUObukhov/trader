package ru.obukhov.trader.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.trader.common.model.transform.OffsetTimeConverter;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.time.Duration;
import java.time.OffsetTime;

class TradingPropertiesIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(OffsetTimeConverter.class)
            .withUserConfiguration(EnableConfigurationPropertiesConfiguration.class);

    @Test
    void beanCreating_whenPropertiesFilled() {

        this.contextRunner
                .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());

                    final TradingProperties tradingProperties = context.getBean(TradingProperties.class);

                    AssertUtils.assertEquals(0.1d, tradingProperties.getCommission());

                    final OffsetTime expectedWorkStartTime = DateUtils.getTime(12, 0, 0).toOffsetTime();
                    Assertions.assertEquals(expectedWorkStartTime, tradingProperties.getWorkStartTime());

                    Assertions.assertEquals(Duration.ofMinutes(480), tradingProperties.getWorkDuration());

                    Assertions.assertEquals(Integer.valueOf(5), tradingProperties.getConsecutiveEmptyDaysLimit());
                });
    }

    @Test
    void beanCreationFails_whenTokenIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "trading.sandbox: false",
                        "trading.commission: 0.003",
                        "trading.work-start-time: 12:00:00+03:00",
                        "trading.work-duration: 480",
                        "trading.consecutive-empty-days-limit: 5",
                        "trading.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> assertContextStartupFailed(
                        context,
                        "trading.token", "не должно быть пустым"
                ));
    }

    @Test
    void beanCreationFails_whenWorkStartTimeIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "trading.sandbox: false",
                        "trading.token: i identify myself as token",
                        "trading.commission: 0.003",
                        "trading.work-duration: 480",
                        "trading.consecutive-empty-days-limit: 5",
                        "trading.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> assertContextStartupFailed(
                        context,
                        "trading.workStartTime", "не должно равняться null"
                ));
    }

    @Test
    void beanCreationFails_whenWorkDurationIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "trading.sandbox: false",
                        "trading.token: i identify myself as token",
                        "trading.commission: 0.003",
                        "trading.work-start-time: 12:00:00+03:00",
                        "trading.consecutive-empty-days-limit: 5",
                        "trading.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> assertContextStartupFailed(
                        context,
                        "trading.workDuration", "не должно равняться null"
                ));
    }

    @Test
    void beanCreationFails_whenConsecutiveEmptyDaysLimitIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "trading.sandbox: false",
                        "trading.token: i identify myself as token",
                        "trading.commission: 0.003",
                        "trading.work-start-time: 12:00:00+03:00",
                        "trading.work-duration: 480",
                        "trading.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> assertContextStartupFailed(
                        context,
                        "trading.consecutiveEmptyDaysLimit", "не должно равняться null"
                ));
    }

    @Test
    void beanCreationFails_whenStartDateIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "trading.sandbox: false",
                        "trading.token: i identify myself as token",
                        "trading.commission: 0.003",
                        "trading.work-start-time: 12:00:00+03:00",
                        "trading.work-duration: 480",
                        "trading.consecutive-empty-days-limit: 5"
                )
                .run(context -> assertContextStartupFailed(
                        context,
                        "trading.startDate", "не должно равняться null"
                ));
    }

    private void assertContextStartupFailed(
            final AssertableApplicationContext context,
            final String... messageSubstrings
    ) {
        final Throwable startupFailure = context.getStartupFailure();

        Assertions.assertNotNull(startupFailure);

        final String message = getBindValidationExceptionMessage(startupFailure);
        for (final String substring : messageSubstrings) {
            Assertions.assertTrue(message.contains(substring));
        }
    }

    private String getBindValidationExceptionMessage(final Throwable startupFailure) {
        final BindValidationException bindValidationException =
                (BindValidationException) startupFailure.getCause().getCause();
        return bindValidationException.getMessage();
    }

    @EnableConfigurationProperties(TradingProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}