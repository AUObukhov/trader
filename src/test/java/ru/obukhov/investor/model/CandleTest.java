package ru.obukhov.investor.model;

import org.junit.Assert;
import org.junit.Test;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.util.MathUtils;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.Assert.assertThrows;

public class CandleTest {

    @Test
    public void createAverage_throwsIllegalArgumentException_whenLeftCandleAfterRightCandle() {
        Candle leftCandle = Candle.builder()
                .time(DateUtils.getDateTime(2020, 10, 11, 1, 0, 0))
                .build();

        Candle rightCandle = Candle.builder()
                .time(DateUtils.getDateTime(2020, 10, 10, 2, 0, 0))
                .build();

        assertThrows("leftCandle can't be after rightCandle",
                IllegalArgumentException.class,
                () -> Candle.createAverage(leftCandle, rightCandle));
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

        assertThrows("Candle intervals must be equal",
                IllegalArgumentException.class,
                () -> Candle.createAverage(leftCandle, rightCandle));
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