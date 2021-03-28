package ru.obukhov.trader.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.trader.common.model.transform.OffsetTimeConverter;
import ru.obukhov.trader.common.util.DateUtils;
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
                .withInitializer(new ConfigFileApplicationContextInitializer())
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());

                    TradingProperties tradingProperties = context.getBean(TradingProperties.class);

                    AssertUtils.assertEquals(0.1d, tradingProperties.getCommission());

                    OffsetTime expectedWorkStartTime = DateUtils.getTime(12, 0, 0).toOffsetTime();
                    Assertions.assertEquals(expectedWorkStartTime, tradingProperties.getWorkStartTime());

                    Assertions.assertEquals(Duration.ofMinutes(480), tradingProperties.getWorkDuration());

                    Assertions.assertEquals(Integer.valueOf(5), tradingProperties.getConsecutiveEmptyDaysLimit());
                });

    }

    @Test
    void beanCreationFails_whenTokenIsNull() {

        this.contextRunner
                .run(context -> assertContextStartupFailed(context,
                        "trading.token", "не должно быть пустым")
                );

    }

    @Test
    void beanCreationFails_whenTokenIsBlank() {

        this.contextRunner
                .withPropertyValues("trading.token=")
                .run(context -> assertContextStartupFailed(context,
                        "trading.token", "не должно быть пустым")
                );

    }

    @Test
    void beanCreationFails_whenWorkStartTimeIsNull() {

        this.contextRunner
                .withPropertyValues("trading.token: i identify myself as token")
                .withPropertyValues("trading.work-duration: 480")
                .withPropertyValues("trading.consecutive-empty-days-limit: 5")
                .withPropertyValues("start-date: 2000-01-01T00:00:00+03:00")
                .run(context -> assertContextStartupFailed(context,
                        "trading.workStartTime", "не должно равняться null")
                );

    }

    @Test
    void beanCreationFails_whenWorkDurationIsNull() {

        this.contextRunner
                .withPropertyValues("trading.token: i identify myself as token")
                .withPropertyValues("trading.work-start-time: 12:00:00+03:00")
                .withPropertyValues("trading.consecutive-empty-days-limit: 5")
                .withPropertyValues("start-date: 2000-01-01T00:00:00+03:00")
                .run(context -> assertContextStartupFailed(context,
                        "trading.workDuration", "не должно равняться null")
                );

    }

    @Test
    void beanCreationFails_whenConsecutiveEmptyDaysLimitIsNull() {

        this.contextRunner
                .withPropertyValues("trading.token: i identify myself as token")
                .withPropertyValues("trading.work-start-time: 12:00:00+03:00")
                .withPropertyValues("trading.work-duration: 480")
                .withPropertyValues("start-date: 2000-01-01T00:00:00+03:00")
                .run(context -> assertContextStartupFailed(context,
                        "trading.consecutiveEmptyDaysLimit", "не должно равняться null")
                );

    }

    @Test
    void beanCreationFails_whenStartDateIsNull() {

        this.contextRunner
                .withPropertyValues("trading.token: i identify myself as token")
                .withPropertyValues("trading.work-start-time: 12:00:00+03:00")
                .withPropertyValues("trading.work-duration: 480")
                .withPropertyValues("trading.consecutive-empty-days-limit: 5")
                .run(context -> assertContextStartupFailed(context,
                        "trading.startDate", "не должно равняться null")
                );

    }

    private void assertContextStartupFailed(AssertableApplicationContext context, String... messageSubstrings) {
        Throwable startupFailure = context.getStartupFailure();

        Assertions.assertNotNull(startupFailure);

        String message = getBindValidationExceptionMessage(startupFailure);
        for (String substring : messageSubstrings) {
            Assertions.assertTrue(message.contains(substring));
        }
    }

    private String getBindValidationExceptionMessage(Throwable startupFailure) {
        BindValidationException bindValidationException =
                (BindValidationException) startupFailure.getCause().getCause();
        return bindValidationException.getMessage();
    }

    @EnableConfigurationProperties(TradingProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}