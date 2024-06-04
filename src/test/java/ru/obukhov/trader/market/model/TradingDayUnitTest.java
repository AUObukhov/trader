package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.trading_day.TestTradingDays;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

class TradingDayUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIntersect_throwsIllegalArgumentException_whenIntervalsAreNotIntersecting() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 11, 22),
                        DateTimeTestData.newEndOfDay(2023, 11, 22),
                        DateTimeTestData.newDateTime(2023, 11, 23),
                        DateTimeTestData.newEndOfDay(2023, 11, 23)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 11, 22),
                        DateTimeTestData.newDateTime(2023, 11, 22, 5),
                        DateTimeTestData.newDateTime(2023, 11, 23, 5),
                        DateTimeTestData.newDateTime(2023, 11, 23, 10)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2023, 11, 22),
                        DateTimeTestData.newDateTime(2023, 11, 22, 5),
                        DateTimeTestData.newDateTime(2023, 11, 23, 6),
                        DateTimeTestData.newDateTime(2023, 11, 23, 10)
                )
        );
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIntersect_throwsIllegalArgumentException_whenOneOfIntervalEdgesIsNull() {
        return Stream.of(
                Arguments.of(DateTimeTestData.newDateTime(2023, 11, 22), null),
                Arguments.of(null, DateTimeTestData.newDateTime(2023, 11, 23, 10))
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forIntersect_throwsIllegalArgumentException_whenIntervalsAreNotIntersecting")
    void intersect_throwsIllegalArgumentException_whenTradingIntervalsAreNotIntersecting(
            final OffsetDateTime startDateTime1,
            final OffsetDateTime endDateTime1,
            final OffsetDateTime startDateTime2,
            final OffsetDateTime endDateTime2
    ) {
        final TradingDay tradingDay1 = TestData.newTradingDay(startDateTime1, endDateTime1);
        final TradingDay tradingDay2 = TestData.newTradingDay(startDateTime2, endDateTime2);

        assertIntersectThrowsException(tradingDay1, tradingDay2, "Trading intervals are not intersecting");
    }

    @ParameterizedTest
    @MethodSource("getData_forIntersect_throwsIllegalArgumentException_whenIntervalsAreNotIntersecting")
    void intersect_throwsIllegalArgumentException_whenAuctionIntervalsAreNotIntersecting(
            final OffsetDateTime openingAuctionStartTime1,
            final OffsetDateTime closingAuctionEndTime1,
            final OffsetDateTime openingAuctionStartTime2,
            final OffsetDateTime closingAuctionEndTime2
    ) {
        final OffsetDateTime startTime = DateTimeTestData.newDateTime(2023, 11, 22);
        final OffsetDateTime endTime = DateUtils.toEndOfDay(startTime);

        final TradingDay tradingDay1 = TestData.newTradingDay(
                true,
                startTime,
                endTime,
                openingAuctionStartTime1,
                closingAuctionEndTime1,
                startTime,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                endTime
        );
        final TradingDay tradingDay2 = TestData.newTradingDay(
                true,
                startTime,
                endTime,
                openingAuctionStartTime2,
                closingAuctionEndTime2,
                startTime,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                endTime
        );

        assertIntersectThrowsException(tradingDay1, tradingDay2, "Auction intervals are not intersecting");
    }

    @ParameterizedTest
    @MethodSource("getData_forIntersect_throwsIllegalArgumentException_whenOneOfIntervalEdgesIsNull")
    void intersect_throwsIllegalArgumentException_whenAuctionEdgeIsNull(
            final OffsetDateTime openingAuctionStartTime,
            final OffsetDateTime closingAuctionEndTime
    ) {
        final OffsetDateTime startTime = DateTimeTestData.newDateTime(2023, 11, 22);
        final OffsetDateTime endTime = DateUtils.toEndOfDay(startTime);

        final TradingDay tradingDay = TestData.newTradingDay(
                true,
                startTime,
                endTime,
                openingAuctionStartTime,
                closingAuctionEndTime,
                startTime,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                endTime
        );

        assertIntersectThrowsException(tradingDay, tradingDay, "Auction intervals are not intersecting");
    }

    @ParameterizedTest
    @MethodSource("getData_forIntersect_throwsIllegalArgumentException_whenIntervalsAreNotIntersecting")
    void intersect_throwsIllegalArgumentException_whenEveningIntervalsAreNotIntersecting(
            final OffsetDateTime eveningStartTime1,
            final OffsetDateTime eveningEndTime1,
            final OffsetDateTime eveningStartTime2,
            final OffsetDateTime eveningEndTime2
    ) {
        final OffsetDateTime startTime = DateTimeTestData.newDateTime(2023, 11, 22);
        final OffsetDateTime endTime = DateUtils.toEndOfDay(startTime);

        final TradingDay tradingDay1 = TestData.newTradingDay(
                true,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                eveningStartTime1,
                eveningEndTime1,
                startTime,
                endTime,
                startTime,
                endTime
        );
        final TradingDay tradingDay2 = TestData.newTradingDay(
                true,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                eveningStartTime2,
                eveningEndTime2,
                startTime,
                endTime,
                startTime,
                endTime
        );

        assertIntersectThrowsException(tradingDay1, tradingDay2, "Evening intervals are not intersecting");
    }

    @ParameterizedTest
    @MethodSource("getData_forIntersect_throwsIllegalArgumentException_whenOneOfIntervalEdgesIsNull")
    void intersect_throwsIllegalArgumentException_whenEveningEdgeIsNull(
            final OffsetDateTime eveningStartTime,
            final OffsetDateTime eveningEndTime
    ) {
        final OffsetDateTime startTime = DateTimeTestData.newDateTime(2023, 11, 22);
        final OffsetDateTime endTime = DateUtils.toEndOfDay(startTime);

        final TradingDay tradingDay = TestData.newTradingDay(
                true,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                eveningStartTime,
                eveningEndTime,
                startTime,
                endTime,
                startTime,
                endTime
        );

        assertIntersectThrowsException(tradingDay, tradingDay, "Evening intervals are not intersecting");
    }

    @ParameterizedTest
    @MethodSource("getData_forIntersect_throwsIllegalArgumentException_whenIntervalsAreNotIntersecting")
    void intersect_throwsIllegalArgumentException_whenClearingIntervalsAreNotIntersecting(
            final OffsetDateTime clearingStartTime1,
            final OffsetDateTime clearingEndTime1,
            final OffsetDateTime clearingStartTime2,
            final OffsetDateTime clearingEndTime2
    ) {
        final OffsetDateTime startTime = DateTimeTestData.newDateTime(2023, 11, 22);
        final OffsetDateTime endTime = DateUtils.toEndOfDay(startTime);

        final TradingDay tradingDay1 = TestData.newTradingDay(
                true,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                startTime,
                endTime,
                clearingStartTime1,
                clearingEndTime1,
                startTime,
                endTime
        );
        final TradingDay tradingDay2 = TestData.newTradingDay(
                true,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                startTime,
                endTime,
                clearingStartTime2,
                clearingEndTime2,
                startTime,
                endTime
        );

        assertIntersectThrowsException(tradingDay1, tradingDay2, "Clearing intervals are not intersecting");
    }

    @ParameterizedTest
    @MethodSource("getData_forIntersect_throwsIllegalArgumentException_whenOneOfIntervalEdgesIsNull")
    void intersect_throwsIllegalArgumentException_whenClearingEdgeIsNull(
            final OffsetDateTime clearingStartTime,
            final OffsetDateTime clearingEndTime
    ) {
        final OffsetDateTime startTime = DateTimeTestData.newDateTime(2023, 11, 22);
        final OffsetDateTime endTime = DateUtils.toEndOfDay(startTime);

        final TradingDay tradingDay = TestData.newTradingDay(
                true,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                startTime,
                endTime,
                clearingStartTime,
                clearingEndTime,
                startTime,
                endTime
        );

        assertIntersectThrowsException(tradingDay, tradingDay, "Clearing intervals are not intersecting");
    }

    @ParameterizedTest
    @MethodSource("getData_forIntersect_throwsIllegalArgumentException_whenIntervalsAreNotIntersecting")
    void intersect_throwsIllegalArgumentException_whenPremarketIntervalsAreNotIntersecting(
            final OffsetDateTime premarketStartTime1,
            final OffsetDateTime premarketEndTime1,
            final OffsetDateTime premarketStartTime2,
            final OffsetDateTime premarketEndTime2
    ) {
        final OffsetDateTime startTime = DateTimeTestData.newDateTime(2023, 11, 22);
        final OffsetDateTime endTime = DateUtils.toEndOfDay(startTime);

        final TradingDay tradingDay1 = TestData.newTradingDay(
                true,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                startTime,
                endTime,
                startTime,
                endTime,
                premarketStartTime1,
                premarketEndTime1
        );
        final TradingDay tradingDay2 = TestData.newTradingDay(
                true,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                startTime,
                endTime,
                startTime,
                endTime,
                premarketStartTime2,
                premarketEndTime2
        );

        assertIntersectThrowsException(tradingDay1, tradingDay2, "Premarket intervals are not intersecting");
    }

    @ParameterizedTest
    @MethodSource("getData_forIntersect_throwsIllegalArgumentException_whenOneOfIntervalEdgesIsNull")
    void intersect_throwsIllegalArgumentException_whenPremarketEdgeIsNull(
            final OffsetDateTime premarketStartTime,
            final OffsetDateTime premarketEndTime
    ) {
        final OffsetDateTime startTime = DateTimeTestData.newDateTime(2023, 11, 22);
        final OffsetDateTime endTime = DateUtils.toEndOfDay(startTime);

        final TradingDay tradingDay = TestData.newTradingDay(
                true,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                startTime,
                endTime,
                startTime,
                endTime,
                premarketStartTime,
                premarketEndTime
        );

        assertIntersectThrowsException(tradingDay, tradingDay, "Premarket intervals are not intersecting");
    }

    private static void assertIntersectThrowsException(final TradingDay tradingDay1, final TradingDay tradingDay2, final String expectedMessage) {
        final Executable executable = () -> tradingDay1.intersect(tradingDay2);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void intersect_withSameTradingDay_returnsSameTradingDay() {
        final TradingDay tradingDay1 = TestTradingDays.TRADING_DAY1.tradingDay();

        final TradingDay result = tradingDay1.intersect(tradingDay1);

        Assertions.assertEquals(tradingDay1, result);
    }

    @Test
    void intersect_whenOneOfDaysIsNotTradingDay_returnsNotTradingDay() {
        final OffsetDateTime startTime = DateTimeTestData.newDateTime(2023, 11, 22);
        final OffsetDateTime endTime = DateUtils.toEndOfDay(startTime);

        final TradingDay tradingDay1 = TestData.newTradingDay(
                true,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                endTime
        );
        final TradingDay tradingDay2 = TestData.newTradingDay(
                false,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                endTime
        );

        final TradingDay result = tradingDay1.intersect(tradingDay2);

        Assertions.assertFalse(result.isTradingDay());
    }

    @Test
    void intersect_whenBothDaysAreNotTradingDay_returnsNotTradingDay() {
        final OffsetDateTime startTime = DateTimeTestData.newDateTime(2023, 11, 22);
        final OffsetDateTime endTime = DateUtils.toEndOfDay(startTime);

        final TradingDay tradingDay1 = TestData.newTradingDay(
                false,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                endTime
        );
        final TradingDay tradingDay2 = TestData.newTradingDay(
                false,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                startTime,
                endTime,
                startTime,
                endTime,
                startTime,
                endTime
        );

        final TradingDay result = tradingDay1.intersect(tradingDay2);

        Assertions.assertFalse(result.isTradingDay());
    }

    @Test
    void intersect_returnsIntersection() {
        // arrange

        final OffsetDateTime startTime1 = DateTimeTestData.newDateTime(2023, 11, 22);
        final OffsetDateTime startTime2 = DateTimeTestData.newDateTime(2023, 11, 22, 1);
        final OffsetDateTime endTime1 = DateTimeTestData.newDateTime(2023, 11, 22, 2);
        final OffsetDateTime endTime2 = DateTimeTestData.newDateTime(2023, 11, 22, 3);

        final OffsetDateTime openingAuctionStartTime1 = DateTimeTestData.newDateTime(2023, 11, 22, 4);
        final OffsetDateTime openingAuctionStartTime2 = DateTimeTestData.newDateTime(2023, 11, 22, 5);
        final OffsetDateTime closingAuctionEndTime1 = DateTimeTestData.newDateTime(2023, 11, 22, 6);
        final OffsetDateTime closingAuctionEndTime2 = DateTimeTestData.newDateTime(2023, 11, 22, 7);

        final OffsetDateTime eveningOpeningAuctionStartTime1 = DateTimeTestData.newDateTime(2023, 11, 22, 8);
        final OffsetDateTime eveningOpeningAuctionStartTime2 = DateTimeTestData.newDateTime(2023, 11, 22, 9);

        final OffsetDateTime eveningStartTime1 = DateTimeTestData.newDateTime(2023, 11, 22, 10);
        final OffsetDateTime eveningStartTime2 = DateTimeTestData.newDateTime(2023, 11, 22, 11);
        final OffsetDateTime eveningEndTime1 = DateTimeTestData.newDateTime(2023, 11, 22, 12);
        final OffsetDateTime eveningEndTime2 = DateTimeTestData.newDateTime(2023, 11, 22, 13);

        final OffsetDateTime clearingStartTime1 = DateTimeTestData.newDateTime(2023, 11, 22, 14);
        final OffsetDateTime clearingStartTime2 = DateTimeTestData.newDateTime(2023, 11, 22, 15);
        final OffsetDateTime clearingEndTime1 = DateTimeTestData.newDateTime(2023, 11, 22, 16);
        final OffsetDateTime clearingEndTime2 = DateTimeTestData.newDateTime(2023, 11, 22, 17);

        final OffsetDateTime premarketStartTime1 = DateTimeTestData.newDateTime(2023, 11, 22, 18);
        final OffsetDateTime premarketStartTime2 = DateTimeTestData.newDateTime(2023, 11, 22, 19);
        final OffsetDateTime premarketEndTime1 = DateTimeTestData.newDateTime(2023, 11, 22, 20);
        final OffsetDateTime premarketEndTime2 = DateTimeTestData.newDateTime(2023, 11, 22, 21);

        final TradingDay tradingDay1 = TestData.newTradingDay(
                true,
                startTime1,
                endTime1,
                openingAuctionStartTime1,
                closingAuctionEndTime1,
                eveningOpeningAuctionStartTime1,
                eveningStartTime1,
                eveningEndTime1,
                clearingStartTime1,
                clearingEndTime1,
                premarketStartTime1,
                premarketEndTime1
        );
        final TradingDay tradingDay2 = TestData.newTradingDay(
                true,
                startTime2,
                endTime2,
                openingAuctionStartTime2,
                closingAuctionEndTime2,
                eveningOpeningAuctionStartTime2,
                eveningStartTime2,
                eveningEndTime2,
                clearingStartTime2,
                clearingEndTime2,
                premarketStartTime2,
                premarketEndTime2
        );

        // action

        final TradingDay result = tradingDay1.intersect(tradingDay2);

        // assert

        final TradingDay expectedTradingDay = TestData.newTradingDay(
                true,
                startTime2,
                endTime1,
                openingAuctionStartTime2,
                closingAuctionEndTime1,
                eveningOpeningAuctionStartTime2,
                eveningStartTime2,
                eveningEndTime1,
                clearingStartTime2,
                clearingEndTime1,
                premarketStartTime2,
                premarketEndTime1
        );

        Assertions.assertEquals(expectedTradingDay, result);
    }

}