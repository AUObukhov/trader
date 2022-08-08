package ru.obukhov.trader.market.model.transform;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

import java.time.OffsetDateTime;

class DateTimeMapperUnitTest {

    private final DateTimeMapper mapper = Mappers.getMapper(DateTimeMapper.class);

    @Test
    void mapsTimestampToOffsetDateTime() {
        final Timestamp timestamp = DateTimeTestData.createTimestamp(1651562430, 123);
        final OffsetDateTime expectedDateTime = DateTimeTestData.createDateTime(2022, 5, 3, 10, 20, 30, 123);

        final OffsetDateTime dateTime = mapper.map(timestamp);

        Assertions.assertEquals(expectedDateTime, dateTime);
    }

    @Test
    void mapsOffsetDateTimeToTimestamp() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2022, 5, 3, 10, 20, 30, 123);
        final Timestamp expectedTimestamp = DateTimeTestData.createTimestamp(1651562430, 123);

        final Timestamp timestamp = mapper.map(dateTime);

        Assertions.assertEquals(expectedTimestamp, timestamp);
    }

}