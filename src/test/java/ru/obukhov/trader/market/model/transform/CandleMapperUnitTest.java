package ru.obukhov.trader.market.model.transform;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.Candles;

import java.util.List;

class CandleMapperUnitTest {

    private final CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);

    @Test
    void mapsSingleCandleFields() {
        final ru.tinkoff.invest.openapi.model.rest.Candle source = TestDataHelper.createTinkoffCandle(
                100, 200, 1000, 50);

        final Candle result = candleMapper.map(source);

        AssertUtils.assertEquals(source.getO(), result.getOpenPrice());
        AssertUtils.assertEquals(source.getC(), result.getClosePrice());
        AssertUtils.assertEquals(source.getH(), result.getHighestPrice());
        AssertUtils.assertEquals(source.getL(), result.getLowestPrice());
        Assertions.assertEquals(source.getTime(), result.getTime());
    }

    @Test
    void updatesOffset() {
        final ru.tinkoff.invest.openapi.model.rest.Candle source = TestDataHelper.createTinkoffCandle(
                CandleResolution.DAY, 0, 0, 0, 0
        );

        final Candle result = candleMapper.map(source);

        Assertions.assertEquals(source.getTime(), result.getTime());
    }

    @Test
    void mapsMultipleCandles() {
        final ru.tinkoff.invest.openapi.model.rest.Candle tinkoffCandle1 = TestDataHelper.createTinkoffCandle(
                100, 200, 1000, 50
        );

        final ru.tinkoff.invest.openapi.model.rest.Candle tinkoffCandle2 = TestDataHelper.createTinkoffCandle(
                200, 400, 2000, 100
        );

        final List<ru.tinkoff.invest.openapi.model.rest.Candle> candles = List.of(tinkoffCandle1, tinkoffCandle2);
        final Candles source = new Candles();
        source.setFigi(StringUtils.EMPTY);
        source.setInterval(CandleResolution.DAY);
        source.setCandles(candles);

        final List<Candle> result = candleMapper.map(source);
        Assertions.assertEquals(2, result.size());

        final Candle candle1 = result.get(0);
        AssertUtils.assertEquals(tinkoffCandle1.getO(), candle1.getOpenPrice());
        AssertUtils.assertEquals(tinkoffCandle1.getC(), candle1.getClosePrice());
        AssertUtils.assertEquals(tinkoffCandle1.getH(), candle1.getHighestPrice());
        AssertUtils.assertEquals(tinkoffCandle1.getL(), candle1.getLowestPrice());
        Assertions.assertEquals(tinkoffCandle1.getTime(), candle1.getTime());

        final Candle candle2 = result.get(1);
        AssertUtils.assertEquals(tinkoffCandle2.getO(), candle2.getOpenPrice());
        AssertUtils.assertEquals(tinkoffCandle2.getC(), candle2.getClosePrice());
        AssertUtils.assertEquals(tinkoffCandle2.getH(), candle2.getHighestPrice());
        AssertUtils.assertEquals(tinkoffCandle2.getL(), candle2.getLowestPrice());
        Assertions.assertEquals(tinkoffCandle2.getTime(), candle2.getTime());
    }

}