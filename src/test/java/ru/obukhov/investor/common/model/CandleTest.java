package ru.obukhov.investor.common.model;

import org.junit.Assert;
import org.junit.Test;
import ru.obukhov.investor.common.util.DateUtils;
import ru.obukhov.investor.common.util.MathUtils;
import ru.obukhov.investor.market.model.Candle;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CandleTest {

    @Test
    public void createAverage_throwsIllegalArgumentException_whenLeftCandleAfterRightCandle() {
        Candle leftCandle = Candle.builder()
                .time(DateUtils.getDateTime(2020, 10, 11, 1, 0, 0))
                .build();

        Candle rightCandle = Candle.builder()
                .time(DateUtils.getDateTime(2020, 10, 10, 2, 0, 0))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> Candle.createAverage(leftCandle, rightCandle),
                "leftCandle can't be after rightCandle");
    }

    @Test
    public void createAverage_throwsIllegalArgumentException_whenIntervalsAreNotEqual() {
        Candle leftCandle = Candle.builder()
                .interval(CandleInterval.DAY)
                .time(DateUtils.getDateTime(2020, 10, 10, 1, 0, 0))
                .build();

        Candle rightCandle = Candle.builder()
                .interval(CandleInterval.HOUR)
                .time(DateUtils.getDateTime(2020, 10, 11, 2, 0, 0))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> Candle.createAverage(leftCandle, rightCandle),
                "Candle intervals must be equal");
    }

    @Test
    public void createAverage() {
        Candle candle1 = Candle.builder()
                .closePrice(BigDecimal.valueOf(100))
                .time(DateUtils.getDateTime(2020, 10, 10, 1, 0, 0))
                .build();

        Candle candle2 = Candle.builder()
                .openPrice(BigDecimal.valueOf(200))
                .time(DateUtils.getDateTime(2020, 10, 11, 2, 0, 0))
                .build();

        Candle averageCandle = Candle.createAverage(candle1, candle2);

        Assert.assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(100), averageCandle.getOpenPrice()));
        Assert.assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(200), averageCandle.getClosePrice()));
        Assert.assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(200), averageCandle.getHighestPrice()));
        Assert.assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(100), averageCandle.getLowestPrice()));

        OffsetDateTime expectedTime = DateUtils.getDateTime(2020, 10, 10, 13, 30, 0);
        Assert.assertEquals(expectedTime, averageCandle.getTime());
    }

}