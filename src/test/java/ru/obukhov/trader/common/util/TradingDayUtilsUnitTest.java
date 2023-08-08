package ru.obukhov.trader.common.util;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay1;
import ru.tinkoff.piapi.contract.v1.TradingDay;

import java.util.List;
import java.util.stream.Stream;

class TradingDayUtilsUnitTest {

    // region includes tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIncludes() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2021, 10, 11, 7),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19),
                        TimestampUtils.newTimestamp(2021, 10, 11, 7),
                        true
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2021, 10, 11, 7),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19),
                        TimestampUtils.newTimestamp(2021, 10, 11, 7, 13),
                        true
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2021, 10, 11, 7),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19),
                        TimestampUtils.newTimestamp(2021, 10, 11, 18, 59, 59, 999_999_999),
                        true
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2021, 10, 11, 7),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19),
                        TimestampUtils.newTimestamp(2021, 10, 10, 10),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2021, 10, 11, 7),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19),
                        TimestampUtils.newTimestamp(2021, 9, 11, 10),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2021, 10, 11, 7),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19),
                        TimestampUtils.newTimestamp(2020, 10, 11, 10),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2021, 10, 11, 7),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19),
                        TimestampUtils.newTimestamp(2021, 10, 11, 6, 59, 59, 999_999_999),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2021, 10, 11, 7),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2021, 10, 11, 7),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19, 59, 59, 999_999_999),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2021, 10, 11, 7),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19),
                        TimestampUtils.newTimestamp(2021, 10, 12, 10),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2021, 10, 11, 7),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19),
                        TimestampUtils.newTimestamp(2021, 11, 11, 10),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2021, 10, 11, 7),
                        TimestampUtils.newTimestamp(2021, 10, 11, 19),
                        TimestampUtils.newTimestamp(2022, 11, 11, 10),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forIncludes")
    void includes(final Timestamp startTimestamp, final Timestamp endTimestamp, final Timestamp timestamp, final boolean expectedResult) {
        final TradingDay tradingDay = TestData.createTradingDay(startTimestamp, endTimestamp);

        final boolean actualResult = TradingDayUtils.includes(tradingDay, timestamp);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region nextScheduleMinute tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forNextScheduleMinute() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 6, 59, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 21, 7, 0, 59, 999999999)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 6, 59, 30),
                        TimestampUtils.newTimestamp(2023, 7, 21, 7, 0, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 7),
                        TimestampUtils.newTimestamp(2023, 7, 21, 7, 1)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 14, 10),
                        TimestampUtils.newTimestamp(2023, 7, 21, 14, 11)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 58, 30),
                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 59, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 58, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 59),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 19),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 20),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 22, 8, 30),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 23, 13, 30),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 6, 59, 30),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7, 0, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 6, 59, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7, 0, 59, 999999999)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 7),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7, 1)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 14, 10),
                        TimestampUtils.newTimestamp(2023, 7, 24, 14, 11)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 58, 30),
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 58, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59, 30),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59, 59, 99999999),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 19),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 20),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 6, 59, 30),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7, 0, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 6, 59, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7, 0, 59, 999999999)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 7),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7, 1)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 14, 10),
                        TimestampUtils.newTimestamp(2023, 7, 25, 14, 11)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 58, 30),
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 58, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 19),
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59, 30),
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59, 59, 99999999),
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59),
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 20),
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 26, 7),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forNextScheduleMinute")
    void nextScheduleMinute(final Timestamp timestamp, final Timestamp expectedResult) {
        final List<TradingDay> tradingSchedule = TestData.createTradingSchedule(
                TimestampUtils.newTimestamp(2023, 7, 21, 7),
                DateTimeTestData.createTime(19, 0, 0),
                5
        );
        final Timestamp actualResult = TradingDayUtils.nextScheduleMinute(tradingSchedule, timestamp);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region ceilingScheduleMinute tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCeilingScheduleMinute() {
        return Stream.of(
//                Arguments.of(
//                        TimestampUtils.newTimestamp(2023, 7, 21, 6, 59, 59, 999999999),
//                        TimestampUtils.newTimestamp(2023, 7, 21, 7)
//                ),
//                Arguments.of(
//                        TimestampUtils.newTimestamp(2023, 7, 21, 6, 59, 30),
//                        TimestampUtils.newTimestamp(2023, 7, 21, 7)
//                ),
//                Arguments.of(
//                        TimestampUtils.newTimestamp(2023, 7, 21, 7),
//                        TimestampUtils.newTimestamp(2023, 7, 21, 7)
//                ),
//                Arguments.of(
//                        TimestampUtils.newTimestamp(2023, 7, 21, 14, 10),
//                        TimestampUtils.newTimestamp(2023, 7, 21, 14, 10)
//                ),
//                Arguments.of(
//                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 59, 30),
//                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 59, 30)
//                ),
//                Arguments.of(
//                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 59, 59, 999999999),
//                        TimestampUtils.newTimestamp(2023, 7, 21, 18, 59, 59, 999999999)
//                ),

                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 19),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 21, 20),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 22, 8, 30),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 23, 13, 30),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 6, 59, 30),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 6, 59, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 7),
                        TimestampUtils.newTimestamp(2023, 7, 24, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 14, 10),
                        TimestampUtils.newTimestamp(2023, 7, 24, 14, 10)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59, 30),
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 24, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 19),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 24, 20),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 6, 59, 30),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 6, 59, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 7),
                        TimestampUtils.newTimestamp(2023, 7, 25, 7)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 14, 10),
                        TimestampUtils.newTimestamp(2023, 7, 25, 14, 10)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59, 30),
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59, 30)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59, 59, 999999999),
                        TimestampUtils.newTimestamp(2023, 7, 25, 18, 59, 59, 999999999)
                ),

                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 19),
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 25, 20),
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2023, 7, 26, 7),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forCeilingScheduleMinute")
    void ceilingScheduleMinute(final Timestamp timestamp, final Timestamp expectedResult) {
        final List<TradingDay> tradingSchedule = TestData.createTradingSchedule(
                TimestampUtils.newTimestamp(2023, 7, 21, 7),
                DateTimeTestData.createTime(19, 0, 0),
                5
        );
        final Timestamp actualResult = TradingDayUtils.ceilingScheduleMinute(tradingSchedule, timestamp);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    @Test
    void toDateTimesString() {
        final String string = TradingDayUtils.toDateTimesString(TestTradingDay1.TRADING_DAY);

        Assertions.assertEquals(TestTradingDay1.PRETTY_STRING, string);
    }

}