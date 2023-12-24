package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

class FirstCandlesUtilsUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetFirstCandleDate_whenFirstCandleDateIsNull() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_1_MIN,
                        DateTimeTestData.newDateTime(2014, 8, 24)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_2_MIN,
                        DateTimeTestData.newDateTime(2014, 8, 24)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_3_MIN,
                        DateTimeTestData.newDateTime(2014, 8, 24)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_5_MIN,
                        DateTimeTestData.newDateTime(2014, 8, 24)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_10_MIN,
                        DateTimeTestData.newDateTime(2014, 8, 24)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_15_MIN,
                        DateTimeTestData.newDateTime(2014, 8, 24)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_30_MIN,
                        DateTimeTestData.newDateTime(2014, 8, 24)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_HOUR,
                        DateTimeTestData.newDateTime(2014, 8, 24)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_2_HOUR,
                        DateTimeTestData.newDateTime(2014, 8, 24)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_4_HOUR,
                        DateTimeTestData.newDateTime(2014, 8, 24)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_DAY,
                        DateTimeTestData.newDateTime(1984, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_WEEK,
                        DateTimeTestData.newDateTime(1984, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_MONTH,
                        DateTimeTestData.newDateTime(1984, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetFirstCandleDate_whenFirstCandleDateIsNull")
    void getFirstCandleDate_whenFirstCandleDateIsNull(
            final OffsetDateTime now,
            final CandleInterval candleInterval,
            final OffsetDateTime expectedResult
    ) {
        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(now)) {
            final OffsetDateTime actualResult = FirstCandleUtils.getFirstCandleDate(null, null, candleInterval);

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetFirstCandleDate_whenFirstCandleDateIsNotNull() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        null,
                        CandleInterval.CANDLE_INTERVAL_1_MIN,
                        DateTimeTestData.newDateTime(2020, 8, 24)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 11, 12),
                        null,
                        CandleInterval.CANDLE_INTERVAL_2_MIN,
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 10, 12)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 11, 12),
                        null,
                        CandleInterval.CANDLE_INTERVAL_3_MIN,
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 9, 12)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 11, 12),
                        null,
                        CandleInterval.CANDLE_INTERVAL_5_MIN,
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 7, 12)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 11, 12),
                        null,
                        CandleInterval.CANDLE_INTERVAL_10_MIN,
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 2, 12)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 11, 12),
                        null,
                        CandleInterval.CANDLE_INTERVAL_15_MIN,
                        DateTimeTestData.newDateTime(2020, 8, 24, 9, 57, 12)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 11, 12),
                        null,
                        CandleInterval.CANDLE_INTERVAL_30_MIN,
                        DateTimeTestData.newDateTime(2020, 8, 24, 9, 42, 12)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 11, 12),
                        null,
                        CandleInterval.CANDLE_INTERVAL_HOUR,
                        DateTimeTestData.newDateTime(2020, 8, 24, 9, 12, 12)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 11, 12),
                        null,
                        CandleInterval.CANDLE_INTERVAL_2_HOUR,
                        DateTimeTestData.newDateTime(2020, 8, 24, 5, 12, 12)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 11, 12),
                        null,
                        CandleInterval.CANDLE_INTERVAL_4_HOUR,
                        DateTimeTestData.newDateTime(2020, 8, 24, 5, 12, 12)
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 8, 24, 10, 11, 12),
                        CandleInterval.CANDLE_INTERVAL_DAY,
                        DateTimeTestData.newDateTime(2020, 8, 24, 7, 11, 12)
                ),

                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 8, 24),
                        CandleInterval.CANDLE_INTERVAL_WEEK,
                        DateTimeTestData.newDateTime(2020, 8, 17)
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 8, 24, 2, 59, 59, 999_999_999),
                        CandleInterval.CANDLE_INTERVAL_WEEK,
                        DateTimeTestData.newDateTime(2020, 8, 17)
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 8, 24, 3),
                        CandleInterval.CANDLE_INTERVAL_WEEK,
                        DateTimeTestData.newDateTime(2020, 8, 24)
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 8, 27),
                        CandleInterval.CANDLE_INTERVAL_WEEK,
                        DateTimeTestData.newDateTime(2020, 8, 24)
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 8, 30, 23, 59, 59, 999_999_999),
                        CandleInterval.CANDLE_INTERVAL_WEEK,
                        DateTimeTestData.newDateTime(2020, 8, 24)
                ),

                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 8, 1),
                        CandleInterval.CANDLE_INTERVAL_MONTH,
                        DateTimeTestData.newDateTime(2020, 7, 1)
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 8, 1, 2, 59, 59, 999_999_999),
                        CandleInterval.CANDLE_INTERVAL_MONTH,
                        DateTimeTestData.newDateTime(2020, 7, 1)
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 8, 1, 3),
                        CandleInterval.CANDLE_INTERVAL_MONTH,
                        DateTimeTestData.newDateTime(2020, 8, 1)
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 8, 14),
                        CandleInterval.CANDLE_INTERVAL_MONTH,
                        DateTimeTestData.newDateTime(2020, 8, 1)
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 8, 31, 23, 59, 59, 999_999_999),
                        CandleInterval.CANDLE_INTERVAL_MONTH,
                        DateTimeTestData.newDateTime(2020, 8, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetFirstCandleDate_whenFirstCandleDateIsNotNull")
    void getFirstCandleDate_whenFirstCandleDateIsNotNull(
            final OffsetDateTime first1MinCandleDate,
            final OffsetDateTime first1DayCandleDate,
            final CandleInterval candleInterval,
            final OffsetDateTime expectedResult
    ) {
        final OffsetDateTime actualResult = FirstCandleUtils.getFirstCandleDate(first1MinCandleDate, first1DayCandleDate, candleInterval);

        Assertions.assertEquals(expectedResult, actualResult);
    }

}