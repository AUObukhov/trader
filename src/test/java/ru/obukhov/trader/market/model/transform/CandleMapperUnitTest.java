package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.time.OffsetDateTime;

class CandleMapperUnitTest {

    private final CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);
    private final QuotationMapper quotationMapper = Mappers.getMapper(QuotationMapper.class);
    private final DateTimeMapper dateTimeMapper = Mappers.getMapper(DateTimeMapper.class);

    @Test
    void mapHistoricCandleToCandle() {
        final int openPrice = 100;
        final int closePrice = 200;
        final int highestPrice = 300;
        final int lowestPrice = 400;
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2022, 1, 1, 10, 30, 15);

        final HistoricCandle historicCandle = TestData.createHistoricCandle(openPrice, closePrice, highestPrice, lowestPrice, dateTime, true);

        final Candle candle = candleMapper.map(historicCandle);

        AssertUtils.assertEquals(openPrice, candle.getOpenPrice());
        AssertUtils.assertEquals(closePrice, candle.getClosePrice());
        AssertUtils.assertEquals(highestPrice, candle.getHighestPrice());
        AssertUtils.assertEquals(lowestPrice, candle.getLowestPrice());
        Assertions.assertEquals(dateTime, candle.getTime());
    }

    @Test
    void mapCandleToHistoricCandle() {
        final int openPrice = 100;
        final int closePrice = 200;
        final int highestPrice = 300;
        final int lowestPrice = 400;
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2022, 1, 1, 10, 30, 15);

        final Candle candle = TestData.createCandle(openPrice, closePrice, highestPrice, lowestPrice, dateTime);
        final boolean isComplete = true;

        final HistoricCandle historicCandle = candleMapper.map(candle, isComplete);

        AssertUtils.assertEquals(openPrice, quotationMapper.toBigDecimal(historicCandle.getOpen()));
        AssertUtils.assertEquals(closePrice, quotationMapper.toBigDecimal(historicCandle.getClose()));
        AssertUtils.assertEquals(highestPrice, quotationMapper.toBigDecimal(historicCandle.getHigh()));
        AssertUtils.assertEquals(lowestPrice, quotationMapper.toBigDecimal(historicCandle.getLow()));
        Assertions.assertEquals(dateTime, dateTimeMapper.map(historicCandle.getTime()));
        Assertions.assertEquals(isComplete, historicCandle.getIsComplete());
    }

}