package ru.obukhov.trader.market.model.transform;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

import java.time.Instant;
import java.time.OffsetDateTime;

class DateTimeMapperUnitTest {

    private final DateTimeMapper mapper = Mappers.getMapper(DateTimeMapper.class);

    @Test
    void timestampToOffsetDateTime_whenNull() {
        final OffsetDateTime dateTime = mapper.timestampToOffsetDateTime(null);

        Assertions.assertNull(dateTime);
    }

    @Test
    void timestampToOffsetDateTime_whenNotNull() {
        final Timestamp timestamp = DateTimeTestData.newTimestamp(1651562430L, 123);
        final OffsetDateTime expectedDateTime = DateTimeTestData.newDateTime(2022, 5, 3, 10, 20, 30, 123);

        final OffsetDateTime dateTime = mapper.timestampToOffsetDateTime(timestamp);

        Assertions.assertEquals(expectedDateTime, dateTime);
    }

    @Test
    void offsetDateTimeToTimestamp_whenNull() {
        final Timestamp timestamp = mapper.offsetDateTimeToTimestamp(null);

        Assertions.assertNull(timestamp);
    }

    @Test
    void offsetDateTimeToTimestamp_whenNotNull() {
        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2022, 5, 3, 10, 20, 30, 123);
        final Timestamp expectedTimestamp = DateTimeTestData.newTimestamp(1651562430, 123);

        final Timestamp timestamp = mapper.offsetDateTimeToTimestamp(dateTime);

        Assertions.assertEquals(expectedTimestamp, timestamp);
    }

    @Test
    void instantToTimestampToTimestamp_whenNull() {
        final Timestamp timestamp = mapper.instantToTimestamp(null);

        Assertions.assertNull(timestamp);
    }

    @Test
    void instantToTimestampToTimestamp_whenNotNull() {
        final Instant instant = DateTimeTestData.newDateTime(2022, 5, 3, 10, 20, 30, 123)
                .toInstant();
        final Timestamp expectedTimestamp = DateTimeTestData.newTimestamp(1651562430, 123);

        final Timestamp timestamp = mapper.instantToTimestamp(instant);

        Assertions.assertEquals(expectedTimestamp, timestamp);
    }

}