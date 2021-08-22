package ru.obukhov.trader.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.TestData;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.time.OffsetDateTime;

class CandleUnitTest {

    @Test
    void createAverage_throwsIllegalArgumentException_whenLeftCandleAfterRightCandle() {
        final Candle leftCandle = new Candle().setTime(DateTimeTestData.createDateTime(2020, 10, 11, 1));

        final Candle rightCandle = new Candle().setTime(DateTimeTestData.createDateTime(2020, 10, 10, 2));

        final Executable executable = () -> Candle.createAverage(leftCandle, rightCandle);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "leftCandle can't be after rightCandle");
    }

    @Test
    void createAverage_throwsIllegalArgumentException_whenIntervalsAreNotEqual() {
        final Candle leftCandle = new Candle()
                .setTime(DateTimeTestData.createDateTime(2020, 10, 10, 1))
                .setInterval(CandleResolution.DAY);

        final Candle rightCandle = new Candle()
                .setTime(DateTimeTestData.createDateTime(2020, 10, 11, 2))
                .setInterval(CandleResolution.HOUR);

        final Executable executable = () -> Candle.createAverage(leftCandle, rightCandle);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "Candle intervals must be equal");
    }

    @Test
    void createAverage() {
        final Candle candle1 = TestData.createCandleWithOpenPriceAndClosePrice(100, 200)
                .setTime(DateTimeTestData.createDateTime(2020, 10, 10, 1));

        final Candle candle2 = TestData.createCandleWithOpenPriceAndClosePrice(300, 400)
                .setTime(DateTimeTestData.createDateTime(2020, 10, 11, 2));

        final Candle averageCandle = Candle.createAverage(candle1, candle2);

        AssertUtils.assertEquals(100, averageCandle.getOpenPrice());
        AssertUtils.assertEquals(400, averageCandle.getClosePrice());
        AssertUtils.assertEquals(400, averageCandle.getHighestPrice());
        AssertUtils.assertEquals(100, averageCandle.getLowestPrice());

        final OffsetDateTime expectedTime = DateTimeTestData.createDateTime(2020, 10, 10, 13, 30);
        Assertions.assertEquals(expectedTime, averageCandle.getTime());
    }

}