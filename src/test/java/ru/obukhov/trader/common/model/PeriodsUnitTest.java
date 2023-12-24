package ru.obukhov.trader.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.stream.Stream;

class PeriodsUnitTest {

    // region getPeriodByCandleInterval tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetPeriodByCandleInterval() {
        return Stream.of(
                Arguments.of(CandleInterval.CANDLE_INTERVAL_1_MIN, Periods.DAY),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_2_MIN, Periods.DAY),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_3_MIN, Periods.DAY),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_5_MIN, Periods.DAY),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_10_MIN, Periods.DAY),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_15_MIN, Periods.DAY),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_30_MIN, Periods.TWO_DAYS),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_HOUR, Periods.WEEK),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_2_HOUR, Periods.MONTH),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_4_HOUR, Periods.MONTH),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_DAY, Periods.YEAR),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_WEEK, Periods.TWO_YEARS),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_MONTH, Periods.DECADE)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetPeriodByCandleInterval")
    void getPeriodByCandleInterval(final CandleInterval candleInterval, final Period expectedResult) {
        final Period actualResult = Periods.getPeriodByCandleInterval(candleInterval);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @EnumSource(value = CandleInterval.class, names = {"UNRECOGNIZED", "CANDLE_INTERVAL_UNSPECIFIED"})
    void getPeriodByCandleInterval_throwIllegalArgumentException_whenCandleIntervalIsNotSupported(final CandleInterval candleInterval) {
        final Executable executable = () -> System.out.println(Periods.getPeriodByCandleInterval(candleInterval));
        final String expectedMessage = "Unsupported CandleInterval " + candleInterval;
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    // endregion

    // region toStartOfPeriod tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToStartOfPeriod() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        Periods.DAY,
                        DateTimeTestData.newDateTime(2020, 10, 5)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 11, 12, 13),
                        Periods.DAY,
                        DateTimeTestData.newDateTime(2020, 10, 5)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        Periods.DAY,
                        DateTimeTestData.newDateTime(2020, 10, 5)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 4, 10, 11, 12, 13),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 10, 4)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 10, 4)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 11, 12, 13),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 10, 4)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 10, 4)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        Periods.WEEK,
                        DateTimeTestData.newDateTime(2020, 10, 5)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 8, 10, 11, 12, 13),
                        Periods.WEEK,
                        DateTimeTestData.newDateTime(2020, 10, 5)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 11, 23, 59, 59, 999_999_999),
                        Periods.WEEK,
                        DateTimeTestData.newDateTime(2020, 10, 5)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 1),
                        Periods.MONTH,
                        DateTimeTestData.newDateTime(2020, 10, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 8, 10, 11, 12, 13),
                        Periods.MONTH,
                        DateTimeTestData.newDateTime(2020, 10, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 31, 23, 59, 59, 999_999_999),
                        Periods.MONTH,
                        DateTimeTestData.newDateTime(2020, 10, 1)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        Periods.YEAR,
                        DateTimeTestData.newDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 7, 8, 10, 11, 12, 13),
                        Periods.YEAR,
                        DateTimeTestData.newDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999),
                        Periods.YEAR,
                        DateTimeTestData.newDateTime(2020, 1, 1)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 7, 8, 10, 11, 12, 13),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2022, 7, 8, 10, 11, 12, 13),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        Periods.DECADE,
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2025, 7, 8, 10, 11, 12, 13),
                        Periods.DECADE,
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2029, 12, 31, 23, 59, 59, 999_999_999),
                        Periods.DECADE,
                        DateTimeTestData.newDateTime(2021, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToStartOfPeriod")
    void toStartOfPeriod(final OffsetDateTime dateTime, final Period period, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = Periods.toStartOfPeriod(dateTime, period);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toEndOfPeriod tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToEndOfPeriod() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        Periods.DAY,
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 11, 12, 13),
                        Periods.DAY,
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        Periods.DAY,
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 4),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 4, 10, 11, 12, 13),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 11, 12, 13),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        Periods.WEEK,
                        DateTimeTestData.newDateTime(2020, 10, 11, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 8, 10, 11, 12, 13),
                        Periods.WEEK,
                        DateTimeTestData.newDateTime(2020, 10, 11, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 11, 23, 59, 59, 999_999_999),
                        Periods.WEEK,
                        DateTimeTestData.newDateTime(2020, 10, 11, 23, 59, 59, 999_999_999)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 1),
                        Periods.MONTH,
                        DateTimeTestData.newDateTime(2020, 10, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 8, 10, 11, 12, 13),
                        Periods.MONTH,
                        DateTimeTestData.newDateTime(2020, 10, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 31, 23, 59, 59, 999_999_999),
                        Periods.MONTH,
                        DateTimeTestData.newDateTime(2020, 10, 31, 23, 59, 59, 999_999_999)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        Periods.YEAR,
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 7, 8, 10, 11, 12, 13),
                        Periods.YEAR,
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999),
                        Periods.YEAR,
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 7, 8, 10, 11, 12, 13),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2022, 1, 1),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2022, 7, 8, 10, 11, 12, 13),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999)
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        Periods.DECADE,
                        DateTimeTestData.newDateTime(2030, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2025, 7, 8, 10, 11, 12, 13),
                        Periods.DECADE,
                        DateTimeTestData.newDateTime(2030, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2030, 12, 31, 23, 59, 59, 999_999_999),
                        Periods.DECADE,
                        DateTimeTestData.newDateTime(2030, 12, 31, 23, 59, 59, 999_999_999)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToEndOfPeriod")
    void toEndOfPeriod(final OffsetDateTime dateTime, final Period period, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = Periods.toEndOfPeriod(dateTime, period);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

}