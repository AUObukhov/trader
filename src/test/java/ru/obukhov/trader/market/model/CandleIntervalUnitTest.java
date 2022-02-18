package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class CandleIntervalUnitTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndCandleIntervals() {
        return Stream.of(
                Arguments.of("1min", CandleInterval._1MIN),
                Arguments.of("2min", CandleInterval._2MIN),
                Arguments.of("3min", CandleInterval._3MIN),
                Arguments.of("5min", CandleInterval._5MIN),
                Arguments.of("10min", CandleInterval._10MIN),
                Arguments.of("15min", CandleInterval._15MIN),
                Arguments.of("30min", CandleInterval._30MIN),
                Arguments.of("hour", CandleInterval.HOUR),
                Arguments.of("2hour", CandleInterval._2HOUR),
                Arguments.of("4hour", CandleInterval._4HOUR),
                Arguments.of("day", CandleInterval.DAY),
                Arguments.of("week", CandleInterval.WEEK),
                Arguments.of("month", CandleInterval.MONTH)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndCandleIntervals")
    void toString_returnsValue(final String expectedValue, final CandleInterval candleResolution) {
        Assertions.assertEquals(expectedValue, candleResolution.toString());
    }

    @ParameterizedTest
    @MethodSource("valuesAndCandleIntervals")
    void fromValue_returnProperEnum(final String value, final CandleInterval expectedCandleResolution) {
        Assertions.assertEquals(expectedCandleResolution, CandleInterval.fromValue(value));
    }

    @ParameterizedTest
    @MethodSource("valuesAndCandleIntervals")
    void jsonMapping_mapsValue(final String value, final CandleInterval candleInterval) throws JsonProcessingException {
        Assertions.assertEquals('"' + value + '"', objectMapper.writeValueAsString(candleInterval));
    }

    @ParameterizedTest
    @MethodSource("valuesAndCandleIntervals")
    void jsonMapping_createsFromValue(final String value, final CandleInterval candleInterval) throws JsonProcessingException {
        Assertions.assertEquals(candleInterval, objectMapper.readValue('"' + value + '"', CandleInterval.class));
    }

}