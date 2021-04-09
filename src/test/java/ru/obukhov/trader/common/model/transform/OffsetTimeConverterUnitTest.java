package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetTime;
import java.time.ZoneOffset;

class OffsetTimeConverterUnitTest {

    private final OffsetTimeConverter converter = new OffsetTimeConverter();

    @Test
    void convert() {
        OffsetTime offsetTime = converter.convert("10:15:30.000005+01:00");

        OffsetTime expectedTime = OffsetTime.of(10, 15, 30, 5000, ZoneOffset.ofHours(1));

        Assertions.assertEquals(expectedTime, offsetTime);
    }

}