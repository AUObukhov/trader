package ru.obukhov.investor.model;

import org.junit.Assert;
import org.junit.Test;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.util.MathUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class CandleTest {

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