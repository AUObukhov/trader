package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

class TradingDayUtilsUnitTest {

    // region includes tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIncludes() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.createDateTime(2021, 10, 11, 7),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19),
                        DateTimeTestData.createDateTime(2021, 10, 11, 7),
                        true
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2021, 10, 11, 7),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19),
                        DateTimeTestData.createDateTime(2021, 10, 11, 7, 13),
                        true
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2021, 10, 11, 7),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19).minusNanos(1),
                        true
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2021, 10, 11, 7),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19),
                        DateTimeTestData.createDateTime(2021, 10, 10, 10),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2021, 10, 11, 7),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19),
                        DateTimeTestData.createDateTime(2021, 9, 11, 10),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2021, 10, 11, 7),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19),
                        DateTimeTestData.createDateTime(2020, 10, 11, 10),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2021, 10, 11, 7),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19),
                        DateTimeTestData.createDateTime(2021, 10, 11, 7).minusNanos(1),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2021, 10, 11, 7),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2021, 10, 11, 7),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19).plusNanos(1),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2021, 10, 11, 7),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19),
                        DateTimeTestData.createDateTime(2021, 10, 12, 10),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2021, 10, 11, 7),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19),
                        DateTimeTestData.createDateTime(2021, 11, 11, 10),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2021, 10, 11, 7),
                        DateTimeTestData.createDateTime(2021, 10, 11, 19),
                        DateTimeTestData.createDateTime(2022, 11, 11, 10),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forIncludes")
    void includes(final OffsetDateTime startTime, final OffsetDateTime endTime, final OffsetDateTime dateTime, final boolean expectedResult) {
        final TradingDay tradingDay = TestData.createTradingDay(startTime, endTime);

        final boolean actualResult = TradingDayUtils.includes(tradingDay, dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region nextScheduleMinute tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forNextScheduleMinute() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 6, 59, 59, 999999999),
                        DateTimeTestData.createDateTime(2023, 7, 21, 7, 0, 59, 999999999)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 6, 59, 30),
                        DateTimeTestData.createDateTime(2023, 7, 21, 7, 0, 30)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 7),
                        DateTimeTestData.createDateTime(2023, 7, 21, 7, 1)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 14, 10),
                        DateTimeTestData.createDateTime(2023, 7, 21, 14, 11)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 18, 58, 30),
                        DateTimeTestData.createDateTime(2023, 7, 21, 18, 59, 30)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 18, 58, 59, 999999999),
                        DateTimeTestData.createDateTime(2023, 7, 21, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 18, 59),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 19),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 20),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 22, 8, 30),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 23, 13, 30),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 6, 59, 30),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7, 0, 30)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 6, 59, 59, 999999999),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7, 0, 59, 999999999)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 7),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7, 1)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 14, 10),
                        DateTimeTestData.createDateTime(2023, 7, 24, 14, 11)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 18, 58, 30),
                        DateTimeTestData.createDateTime(2023, 7, 24, 18, 59, 30)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 18, 58, 59, 999999999),
                        DateTimeTestData.createDateTime(2023, 7, 24, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 18, 59),
                        DateTimeTestData.createDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 18, 59, 30),
                        DateTimeTestData.createDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 18, 59, 59, 99999999),
                        DateTimeTestData.createDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 19),
                        DateTimeTestData.createDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 20),
                        DateTimeTestData.createDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 6, 59, 30),
                        DateTimeTestData.createDateTime(2023, 7, 25, 7, 0, 30)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 6, 59, 59, 999999999),
                        DateTimeTestData.createDateTime(2023, 7, 25, 7, 0, 59, 999999999)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 7),
                        DateTimeTestData.createDateTime(2023, 7, 25, 7, 1)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 14, 10),
                        DateTimeTestData.createDateTime(2023, 7, 25, 14, 11)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 18, 58, 30),
                        DateTimeTestData.createDateTime(2023, 7, 25, 18, 59, 30)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 18, 58, 59, 999999999),
                        DateTimeTestData.createDateTime(2023, 7, 25, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 19),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 18, 59, 30),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 18, 59, 59, 99999999),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 18, 59),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 20),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 26, 7),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forNextScheduleMinute")
    void nextScheduleMinute(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final List<TradingDay> tradingSchedule = TestData.createTradingSchedule(
                DateTimeTestData.createDateTime(2023, 7, 21, 7),
                DateTimeTestData.createTime(19, 0, 0),
                5
        );
        final OffsetDateTime actualResult = TradingDayUtils.nextScheduleMinute(tradingSchedule, dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region ceilingScheduleMinute tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCeilingScheduleMinute() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 6, 59, 59, 999999999),
                        DateTimeTestData.createDateTime(2023, 7, 21, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 6, 59, 30),
                        DateTimeTestData.createDateTime(2023, 7, 21, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 7),
                        DateTimeTestData.createDateTime(2023, 7, 21, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 14, 10),
                        DateTimeTestData.createDateTime(2023, 7, 21, 14, 10)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 18, 59, 30),
                        DateTimeTestData.createDateTime(2023, 7, 21, 18, 59, 30)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 18, 59, 59, 999999999),
                        DateTimeTestData.createDateTime(2023, 7, 21, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 19),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 21, 20),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 22, 8, 30),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 23, 13, 30),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 6, 59, 30),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 6, 59, 59, 999999999),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 7),
                        DateTimeTestData.createDateTime(2023, 7, 24, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 14, 10),
                        DateTimeTestData.createDateTime(2023, 7, 24, 14, 10)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 18, 59, 30),
                        DateTimeTestData.createDateTime(2023, 7, 24, 18, 59, 30)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 18, 59, 59, 999999999),
                        DateTimeTestData.createDateTime(2023, 7, 24, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 19),
                        DateTimeTestData.createDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 24, 20),
                        DateTimeTestData.createDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 6, 59, 30),
                        DateTimeTestData.createDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 6, 59, 59, 999999999),
                        DateTimeTestData.createDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 7),
                        DateTimeTestData.createDateTime(2023, 7, 25, 7)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 14, 10),
                        DateTimeTestData.createDateTime(2023, 7, 25, 14, 10)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 18, 59, 30),
                        DateTimeTestData.createDateTime(2023, 7, 25, 18, 59, 30)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 18, 59, 59, 999999999),
                        DateTimeTestData.createDateTime(2023, 7, 25, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 19),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 25, 20),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2023, 7, 26, 7),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forCeilingScheduleMinute")
    void ceilingScheduleMinute(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final List<TradingDay> tradingSchedule = TestData.createTradingSchedule(
                DateTimeTestData.createDateTime(2023, 7, 21, 7),
                DateTimeTestData.createTime(19, 0, 0),
                5
        );
        final OffsetDateTime actualResult = TradingDayUtils.ceilingScheduleMinute(tradingSchedule, dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

}