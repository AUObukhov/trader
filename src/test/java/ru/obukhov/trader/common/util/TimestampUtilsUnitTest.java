package ru.obukhov.trader.common.util;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

import java.time.Instant;
import java.time.OffsetDateTime;

class TimestampUtilsUnitTest {

    @Test
    void toInstant() {
        final Timestamp timestamp = DateTimeTestData.newTimestamp(1651562430, 123);

        final Instant instant = TimestampUtils.toInstant(timestamp);

        final Instant expectedInstant = DateTimeTestData.newDateTime(2022, 5, 3, 10, 20, 30, 123)
                .toInstant();
        Assertions.assertEquals(expectedInstant, instant);
    }

    @Test
    void toOffsetDateTime() {
        final Timestamp timestamp = DateTimeTestData.newTimestamp(1651562430, 123);

        final OffsetDateTime dateTime = TimestampUtils.toOffsetDateTime(timestamp);

        final OffsetDateTime expectedDateTime = DateTimeTestData.newDateTime(2022, 5, 3, 10, 20, 30, 123);
        Assertions.assertEquals(expectedDateTime, dateTime);
    }

}