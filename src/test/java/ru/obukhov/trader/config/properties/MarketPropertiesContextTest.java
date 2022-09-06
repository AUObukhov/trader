package ru.obukhov.trader.config.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.obukhov.trader.config.OffsetTimeConverter;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

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
                    Assertions.assertEquals(expectedWorkStartTime, marketProperties.getWorkSchedule().getStartTime());

                    Assertions.assertEquals(Duration.ofMinutes(480), marketProperties.getWorkSchedule().getDuration());

                    Assertions.assertEquals(Integer.valueOf(7), marketProperties.getConsecutiveEmptyDaysLimit());
                });
    }

    @Test
    void beanCreationFails_whenWorkScheduleIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "market.commission: 0.003",
                        "market.consecutive-empty-days-limit: 7",
                        "market.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> AssertUtils.assertContextStartupFailed(context, "market", "workSchedule is mandatory"));
    }

    @Test
    void beanCreationFails_whenWorkScheduleStartTimeIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "market.commission: 0.003",
                        "market.work-schedule.duration: 480",
                        "market.consecutive-empty-days-limit: 7",
                        "market.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> AssertUtils.assertContextStartupFailed(context, "market.work-schedule", "startTime is mandatory"));
    }

    @Test
    void beanCreationFails_whenWorkScheduleDurationIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "market.commission: 0.003",
                        "market.work-schedule.start-time: 12:00:00+03:00",
                        "market.consecutive-empty-days-limit: 7",
                        "market.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> AssertUtils.assertContextStartupFailed(context, "market.work-schedule", "duration is mandatory"));
    }

    @Test
    void beanCreationFails_whenWorkScheduleDurationIsNegative() {
        this.contextRunner
                .withPropertyValues(
                        "market.commission: 0.003",
                        "market.work-schedule.start-time: 12:00:00+03:00",
                        "market.work-schedule.duration: -1",
                        "market.consecutive-empty-days-limit: 7",
                        "market.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> AssertUtils.assertContextStartupFailed(
                        context,
                        "market.work-schedule", "duration must be positive in minutes"
                ));
    }

    @Test
    void beanCreationFails_whenWorkScheduleDurationIsZero() {
        this.contextRunner
                .withPropertyValues(
                        "market.commission: 0.003",
                        "market.work-schedule.start-time: 12:00:00+03:00",
                        "market.work-schedule.duration: 0",
                        "market.consecutive-empty-days-limit: 7",
                        "market.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> AssertUtils.assertContextStartupFailed(
                        context,
                        "market.work-schedule", "duration must be positive in minutes"
                ));
    }

    @Test
    void beanCreationFails_whenWorkScheduleDurationIsOneDay() {
        this.contextRunner
                .withPropertyValues(
                        "market.commission: 0.003",
                        "market.work-schedule.start-time: 12:00:00+03:00",
                        "market.work-schedule.duration: 86400",
                        "market.consecutive-empty-days-limit: 7",
                        "market.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> AssertUtils.assertContextStartupFailed(
                        context,
                        "market.work-schedule", "duration must be less than 1 day"
                ));
    }

    @Test
    void beanCreationFails_whenWorkScheduleDurationIsMoreThanOneDay() {
        this.contextRunner
                .withPropertyValues(
                        "market.commission: 0.003",
                        "market.work-schedule.start-time: 12:00:00+03:00",
                        "market.work-schedule.duration: 86399",
                        "market.consecutive-empty-days-limit: 7",
                        "market.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> AssertUtils.assertContextStartupFailed(
                        context,
                        "market.work-schedule", "duration must be less than 1 day"
                ));
    }

    @Test
    void beanCreationFails_whenConsecutiveEmptyDaysLimitIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "market.commission: 0.003",
                        "market.work-schedule.start-time: 12:00:00+03:00",
                        "market.work-schedule.duration: 480",
                        "market.start-date: 2000-01-01T00:00:00+03:00"
                )
                .run(context -> AssertUtils.assertContextStartupFailed(
                        context,
                        "market.consecutiveEmptyDaysLimit", "consecutiveEmptyDaysLimit is mandatory"
                ));
    }

    @Test
    void beanCreationFails_whenStartDateIsNull() {
        this.contextRunner
                .withPropertyValues(
                        "market.commission: 0.003",
                        "market.work-schedule.start-time: 12:00:00+03:00",
                        "market.work-schedule.duration: 480",
                        "market.consecutive-empty-days-limit: 7"
                )
                .run(context -> AssertUtils.assertContextStartupFailed(context, "market.startDate", "startDate is mandatory"));
    }

    @EnableConfigurationProperties(MarketProperties.class)
    private static class EnableConfigurationPropertiesConfiguration {
    }

}