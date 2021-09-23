package ru.obukhov.trader.config.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.trader.common.model.transform.OffsetTimeConverter;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;

import java.time.Duration;
import java.time.OffsetTime;

class MarketPropertiesContextTest {

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

                    final MarketProperties marketProperties = context.getBean(MarketProperties.class);

                    final OffsetTime expectedWorkStartTime = DateTimeTestData.createTime(12, 0, 0);
                    Assertions.assertEquals(expectedWorkStartTime, marketProperties.getWorkStartTime());

                    Assertions.assertEquals(Duration.ofMinutes(480), marketProperties.getWorkDuration());

                    Assertions.assertEquals(Integer.valueOf(5), marketProperties.getConsecutiveEmptyDaysLimit());
                });
    }

    @Test
    void beanCreationFails_whenWorkStartTimeIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "market.commission: 0.003",
                        "market.work-duration: 480",
                        "market.consecutive-empty-days-limit: 5",
                        "market.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> AssertUtils.assertContextStartupFailed(context, "market.workStartTime", "workStartTime is mandatory"));
    }

    @Test
    void beanCreationFails_whenWorkDurationIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "market.commission: 0.003",
                        "market.work-start-time: 12:00:00+03:00",
                        "market.consecutive-empty-days-limit: 5",
                        "market.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> AssertUtils.assertContextStartupFailed(context, "market.workDuration", "workDuration is mandatory"));
    }

    @Test
    void beanCreationFails_whenConsecutiveEmptyDaysLimitIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "market.commission: 0.003",
                        "market.work-start-time: 12:00:00+03:00",
                        "market.work-duration: 480",
                        "market.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> AssertUtils.assertContextStartupFailed(context, "market.consecutiveEmptyDaysLimit", "consecutiveEmptyDaysLimit is " +
                        "mandatory"));
    }

    @Test
    void beanCreationFails_whenStartDateIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "market.commission: 0.003",
                        "market.work-start-time: 12:00:00+03:00",
                        "market.work-duration: 480",
                        "market.consecutive-empty-days-limit: 5"
                )
                .run(context -> AssertUtils.assertContextStartupFailed(context, "market.startDate", "startDate is mandatory"));
    }

    @EnableConfigurationProperties(MarketProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}