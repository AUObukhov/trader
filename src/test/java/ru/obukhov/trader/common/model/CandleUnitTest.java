package ru.obukhov.trader.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.time.OffsetDateTime;

class CandleUnitTest {

    @Test
    void createAverage_throwsIllegalArgumentException_whenLeftCandleAfterRightCandle() {
        final Candle leftCandle = TestDataHelper.createCandleWithTime(DateTimeTestData.createDateTime(2020, 10, 11, 1));

        final Candle rightCandle = TestDataHelper.createCandleWithTime(DateTimeTestData.createDateTime(2020, 10, 10, 2));

        AssertUtils.assertThrowsWithMessage(
                () -> Candle.createAverage(leftCandle, rightCandle),
                IllegalArgumentException.class,
                "leftCandle can't be after rightCandle"
        );
    }

    @Test
    void createAverage_throwsIllegalArgumentException_whenIntervalsAreNotEqual() {
        final Candle leftCandle = TestDataHelper.createCandleWithTimeAndInterval(
                DateTimeTestData.createDateTime(2020, 10, 10, 1),
                CandleResolution.DAY
        );

        final Candle rightCandle = TestDataHelper.createCandleWithTimeAndInterval(
                DateTimeTestData.createDateTime(2020, 10, 11, 2),
                CandleResolution.HOUR
        );

        AssertUtils.assertThrowsWithMessage(
                () -> Candle.createAverage(leftCandle, rightCandle),
                IllegalArgumentException.class,
                "Candle intervals must be equal"
        );
    }

    @Test
    void createAverage() {
        final Candle candle1 = TestDataHelper.createCandleWithOpenClosePricesAndTime(
                100,
                200,
                DateTimeTestData.createDateTime(2020, 10, 10, 1)
        );

        final Candle candle2 = TestDataHelper.createCandleWithOpenClosePricesAndTime(
                300,
                400,
                DateTimeTestData.createDateTime(2020, 10, 11, 2)
        );

        final Candle averageCandle = Candle.createAverage(candle1, candle2);

        AssertUtils.assertEquals(100, averageCandle.getOpenPrice());
        AssertUtils.assertEquals(400, averageCandle.getClosePrice());
        AssertUtils.assertEquals(400, averageCandle.getHighestPrice());
        AssertUtils.assertEquals(100, averageCandle.getLowestPrice());

        final OffsetDateTime expectedTime = DateTimeTestData.createDateTime(2020, 10, 10, 13, 30);
        Assertions.assertEquals(expectedTime, averageCandle.getTime());
    }

}