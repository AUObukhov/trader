package ru.obukhov.trader.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.time.OffsetDateTime;

class CandleUnitTest {

    @Test
    void createAverage_throwsIllegalArgumentException_whenLeftCandleAfterRightCandle() {
        final Candle leftCandle = TestDataHelper.createCandleWithTime(
                DateUtils.getDateTime(2020, 10, 11, 1, 0, 0)
        );

        final Candle rightCandle = TestDataHelper.createCandleWithTime(
                DateUtils.getDateTime(2020, 10, 10, 2, 0, 0)
        );

        AssertUtils.assertThrowsWithMessage(
                () -> Candle.createAverage(leftCandle, rightCandle),
                IllegalArgumentException.class,
                "leftCandle can't be after rightCandle"
        );
    }

    @Test
    void createAverage_throwsIllegalArgumentException_whenIntervalsAreNotEqual() {
        final Candle leftCandle = TestDataHelper.createCandleWithTimeAndInterval(
                DateUtils.getDateTime(2020, 10, 10, 1, 0, 0),
                CandleResolution.DAY
        );

        final Candle rightCandle = TestDataHelper.createCandleWithTimeAndInterval(
                DateUtils.getDateTime(2020, 10, 11, 2, 0, 0),
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
        final Candle candle1 = TestDataHelper.createCandleWithClosePriceAndTime(
                100,
                DateUtils.getDateTime(2020, 10, 10, 1, 0, 0)
        );

        final Candle candle2 = TestDataHelper.createCandleWithOpenPriceAndTime(
                200,
                DateUtils.getDateTime(2020, 10, 11, 2, 0, 0)
        );

        final Candle averageCandle = Candle.createAverage(candle1, candle2);

        AssertUtils.assertEquals(100, averageCandle.getOpenPrice());
        AssertUtils.assertEquals(200, averageCandle.getClosePrice());
        AssertUtils.assertEquals(200, averageCandle.getHighestPrice());
        AssertUtils.assertEquals(100, averageCandle.getLowestPrice());

        final OffsetDateTime expectedTime = DateUtils.getDateTime(2020, 10, 10, 13, 30, 0);
        Assertions.assertEquals(expectedTime, averageCandle.getTime());
    }

}