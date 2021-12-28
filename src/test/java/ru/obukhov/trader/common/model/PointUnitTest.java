package ru.obukhov.trader.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.common.util.DecimalUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

class PointUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forOf_withBigDecimal() {
        return Stream.of(
                Arguments.of(null, null, null),
                Arguments.of(OffsetDateTime.now(), BigDecimal.valueOf(10.123123), BigDecimal.valueOf(10.123123)),
                Arguments.of(OffsetDateTime.now(), BigDecimal.valueOf(10.123125), BigDecimal.valueOf(10.123125))
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forOf_withBigDecimal")
    void of_withBigDecimal(OffsetDateTime time, BigDecimal value, BigDecimal expectedValue) {
        final Point point = Point.of(time, value);

        Assertions.assertEquals(time, point.getTime());
        Assertions.assertEquals(expectedValue, point.getValue());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forOf_withDouble() {
        return Stream.of(
                Arguments.of(null, null, null),
                Arguments.of(OffsetDateTime.now(), 10.12, DecimalUtils.setDefaultScale(10.12)),
                Arguments.of(OffsetDateTime.now(), 10.123123, BigDecimal.valueOf(10.123123)),
                Arguments.of(OffsetDateTime.now(), 10.123125, BigDecimal.valueOf(10.123125))
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forOf_withDouble")
    void of_withDouble(OffsetDateTime time, Double value, BigDecimal expectedValue) {
        final Point point = Point.of(time, value);

        Assertions.assertEquals(time, point.getTime());
        Assertions.assertEquals(expectedValue, point.getValue());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forOf_withInteger() {
        return Stream.of(
                Arguments.of(null, null, null),
                Arguments.of(OffsetDateTime.now(), 100, DecimalUtils.setDefaultScale(100))
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forOf_withInteger")
    void of_withInteger(OffsetDateTime time, Integer value, BigDecimal expectedValue) {
        final Point point = Point.of(time, value);

        Assertions.assertEquals(time, point.getTime());
        Assertions.assertEquals(expectedValue, point.getValue());
    }

}