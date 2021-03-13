package ru.obukhov.trader.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

class CandleTest {

    @Test
    void createAverage_throwsIllegalArgumentException_whenLeftCandleAfterRightCandle() {
        Candle leftCandle = Candle.builder()
                .time(DateUtils.getDateTime(2020, 10, 11, 1, 0, 0))
                .build();

        Candle rightCandle = Candle.builder()
                .time(DateUtils.getDateTime(2020, 10, 10, 2, 0, 0))
                .build();

        AssertUtils.assertThrowsWithMessage(() -> Candle.createAverage(leftCandle, rightCandle),
                IllegalArgumentException.class,
                "leftCandle can't be after rightCandle");
    }

    @Test
    void createAverage_throwsIllegalArgumentException_whenIntervalsAreNotEqual() {
        Candle leftCandle = Candle.builder()
                .interval(CandleInterval.DAY)
                .time(DateUtils.getDateTime(2020, 10, 10, 1, 0, 0))
                .build();

        Candle rightCandle = Candle.builder()
                .interval(CandleInterval.HOUR)
                .time(DateUtils.getDateTime(2020, 10, 11, 2, 0, 0))
                .build();

        AssertUtils.assertThrowsWithMessage(() -> Candle.createAverage(leftCandle, rightCandle),
                IllegalArgumentException.class,
                "Candle intervals must be equal");
    }

    @Test
    void createAverage() {
        Candle candle1 = Candle.builder()
                .closePrice(BigDecimal.valueOf(100))
                .time(DateUtils.getDateTime(2020, 10, 10, 1, 0, 0))
                .build();

        Candle candle2 = Candle.builder()
                .openPrice(BigDecimal.valueOf(200))
                .time(DateUtils.getDateTime(2020, 10, 11, 2, 0, 0))
                .build();

        Candle averageCandle = Candle.createAverage(candle1, candle2);

        AssertUtils.assertEquals(BigDecimal.valueOf(100), averageCandle.getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(200), averageCandle.getClosePrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(200), averageCandle.getHighestPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(100), averageCandle.getLowestPrice());

        OffsetDateTime expectedTime = DateUtils.getDateTime(2020, 10, 10, 13, 30, 0);
        Assertions.assertEquals(expectedTime, averageCandle.getTime());
    }

}