package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.HistoricCandleBuilder;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.time.OffsetDateTime;

class CandleMapperUnitTest {

    private final CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);

    @Test
    void mapHistoricCandleToCandle() {
        final int open = 100;
        final int close = 200;
        final int high = 300;
        final int low = 400;
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2022, 1, 1, 10, 30, 15);

        final HistoricCandle historicCandle = new HistoricCandleBuilder()
                .setOpen(open)
                .setClose(close)
                .setHigh(high)
                .setLow(low)
                .setTime(dateTime)
                .setIsComplete(false)
                .build();

        final Candle candle = candleMapper.map(historicCandle);

        AssertUtils.assertEquals(open, candle.getOpen());
        AssertUtils.assertEquals(close, candle.getClose());
        AssertUtils.assertEquals(high, candle.getHigh());
        AssertUtils.assertEquals(low, candle.getLow());
        Assertions.assertEquals(dateTime, candle.getTime());
    }

    @Test
    void mapCandleToHistoricCandle() {
        final int open = 100;
        final int close = 200;
        final int high = 300;
        final int low = 400;
        final OffsetDateTime time = DateTimeTestData.createDateTime(2022, 1, 1, 10, 30, 15);

        final Candle candle = new CandleBuilder()
                .setOpen(open)
                .setClose(close)
                .setHighest(high)
                .setLowest(low)
                .setTime(time)
                .build();
        final boolean isComplete = true;

        final HistoricCandle historicCandle = candleMapper.map(candle, isComplete);

        AssertUtils.assertEquals(open, QuotationUtils.toBigDecimal(historicCandle.getOpen()));
        AssertUtils.assertEquals(close, QuotationUtils.toBigDecimal(historicCandle.getClose()));
        AssertUtils.assertEquals(high, QuotationUtils.toBigDecimal(historicCandle.getHigh()));
        AssertUtils.assertEquals(low, QuotationUtils.toBigDecimal(historicCandle.getLow()));
        Assertions.assertEquals(time, TimestampUtils.toOffsetDateTime(historicCandle.getTime()));
        Assertions.assertEquals(isComplete, historicCandle.getIsComplete());
    }

}