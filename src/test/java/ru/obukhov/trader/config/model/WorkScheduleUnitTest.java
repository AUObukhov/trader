package ru.obukhov.trader.config.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

import java.time.Duration;
import java.time.OffsetTime;

class WorkScheduleUnitTest {

    @Test
    void getEndTime() {
        final OffsetTime startTime = DateTimeTestData.newTime(10, 0, 0);
        final Duration duration = Duration.ofHours(9);
        final WorkSchedule workSchedule = new WorkSchedule(startTime, duration);

        final OffsetTime endTime = workSchedule.getEndTime();

        final OffsetTime expectedEndTime = DateTimeTestData.newTime(19, 0, 0);
        Assertions.assertEquals(expectedEndTime, endTime);
    }

}