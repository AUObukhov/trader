package ru.obukhov.trader.market.model.transform;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.obukhov.trader.test.utils.model.HistoricCandleBuilder;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

class CandleMapperUnitTest {

    private final CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);
    private final QuotationMapper quotationMapper = Mappers.getMapper(QuotationMapper.class);

    @Test
    void mapHistoricCandleToCandle() {
        final int openPrice = 100;
        final int closePrice = 200;
        final int highestPrice = 300;
        final int lowestPrice = 400;
        final Timestamp timestamp = TimestampUtils.newTimestamp(2022, 1, 1, 10, 30, 15);

        final HistoricCandle historicCandle = new HistoricCandleBuilder()
                .setOpenPrice(openPrice)
                .setClosePrice(closePrice)
                .setHighestPrice(highestPrice)
                .setLowestPrice(lowestPrice)
                .setTime(timestamp)
                .setIsComplete(false)
                .build();

        final Candle candle = candleMapper.map(historicCandle);

        AssertUtils.assertEquals(openPrice, candle.getOpenPrice());
        AssertUtils.assertEquals(closePrice, candle.getClosePrice());
        AssertUtils.assertEquals(highestPrice, candle.getHighestPrice());
        AssertUtils.assertEquals(lowestPrice, candle.getLowestPrice());
        Assertions.assertEquals(timestamp, candle.getTime());
    }

    @Test
    void mapCandleToHistoricCandle() {
        final int openPrice = 100;
        final int closePrice = 200;
        final int highestPrice = 300;
        final int lowestPrice = 400;
        final Timestamp time = TimestampUtils.newTimestamp(2022, 1, 1, 10, 30, 15);

        final Candle candle = new CandleBuilder()
                .setOpenPrice(openPrice)
                .setClosePrice(closePrice)
                .setHighestPrice(highestPrice)
                .setLowestPrice(lowestPrice)
                .setTime(time)
                .build();
        final boolean isComplete = true;

        final HistoricCandle historicCandle = candleMapper.map(candle, isComplete);

        AssertUtils.assertEquals(openPrice, quotationMapper.toBigDecimal(historicCandle.getOpen()));
        AssertUtils.assertEquals(closePrice, quotationMapper.toBigDecimal(historicCandle.getClose()));
        AssertUtils.assertEquals(highestPrice, quotationMapper.toBigDecimal(historicCandle.getHigh()));
        AssertUtils.assertEquals(lowestPrice, quotationMapper.toBigDecimal(historicCandle.getLow()));
        Assertions.assertEquals(time, historicCandle.getTime());
        Assertions.assertEquals(isComplete, historicCandle.getIsComplete());
    }

}