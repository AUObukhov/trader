package ru.obukhov.trader.common.model;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.CandleBuilder;

class CandleUnitTest {

    @Test
    void createAverage_throwsIllegalArgumentException_whenLeftCandleAfterRightCandle() {
        final Candle leftCandle = new Candle().setTime(TimestampUtils.newTimestamp(2020, 10, 11, 1));

        final Candle rightCandle = new Candle().setTime(TimestampUtils.newTimestamp(2020, 10, 10, 2));

        final Executable executable = () -> Candle.createAverage(leftCandle, rightCandle);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "leftCandle can't be after rightCandle");
    }

    @Test
    void createAverage() {
        final Candle candle1 = new CandleBuilder()
                .setOpen(100)
                .setClose(200)
                .setTime(TimestampUtils.newTimestamp(2020, 10, 10, 1))
                .build();
        final Candle candle2 = new CandleBuilder()
                .setOpen(300)
                .setClose(400)
                .setTime(TimestampUtils.newTimestamp(2020, 10, 11, 2))
                .build();

        final Candle averageCandle = Candle.createAverage(candle1, candle2);

        AssertUtils.assertEquals(100, averageCandle.getOpen());
        AssertUtils.assertEquals(400, averageCandle.getClose());
        AssertUtils.assertEquals(400, averageCandle.getHigh());
        AssertUtils.assertEquals(100, averageCandle.getLow());

        final Timestamp expectedTimestamp = TimestampUtils.newTimestamp(2020, 10, 10, 13, 30);
        Assertions.assertEquals(expectedTimestamp, averageCandle.getTime());
    }

}