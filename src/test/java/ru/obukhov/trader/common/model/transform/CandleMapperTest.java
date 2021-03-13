package ru.obukhov.trader.common.model.transform;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.transform.CandleMapper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.HistoricalCandles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

class CandleMapperTest {

    private final CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);

    @Test
    void mapsSingleCandleFields() {

        ru.tinkoff.invest.openapi.models.market.Candle source = TestDataHelper.createTinkoffCandle(
                100, 200, 1000, 50);

        Candle result = candleMapper.map(source);

        AssertUtils.assertEquals(source.openPrice, result.getOpenPrice());
        AssertUtils.assertEquals(source.closePrice, result.getClosePrice());
        AssertUtils.assertEquals(source.highestPrice, result.getHighestPrice());
        AssertUtils.assertEquals(source.lowestPrice, result.getLowestPrice());
        Assertions.assertEquals(source.time, result.getTime());

    }

    @Test
    void updatesOffset() {

        ru.tinkoff.invest.openapi.models.market.Candle source = new ru.tinkoff.invest.openapi.models.market.Candle(
                StringUtils.EMPTY,
                CandleInterval.DAY,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.TEN,
                OffsetDateTime.now()
        );

        Candle result = candleMapper.map(source);

        Assertions.assertEquals(source.time, result.getTime());

    }

    @Test
    void mapsHistoricalCandles() {

        ru.tinkoff.invest.openapi.models.market.Candle tinkoffCandle1 = TestDataHelper.createTinkoffCandle(
                100, 200, 1000, 50);

        ru.tinkoff.invest.openapi.models.market.Candle tinkoffCandle2 = TestDataHelper.createTinkoffCandle(
                200, 400, 2000, 100);

        List<ru.tinkoff.invest.openapi.models.market.Candle> candles = Arrays.asList(tinkoffCandle1, tinkoffCandle2);
        HistoricalCandles source = new HistoricalCandles(StringUtils.EMPTY, CandleInterval.DAY, candles);

        List<Candle> result = candleMapper.map(source);
        Assertions.assertEquals(2, result.size());

        Candle candle1 = result.get(0);
        AssertUtils.assertEquals(tinkoffCandle1.openPrice, candle1.getOpenPrice());
        AssertUtils.assertEquals(tinkoffCandle1.closePrice, candle1.getClosePrice());
        AssertUtils.assertEquals(tinkoffCandle1.highestPrice, candle1.getHighestPrice());
        AssertUtils.assertEquals(tinkoffCandle1.lowestPrice, candle1.getLowestPrice());
        Assertions.assertEquals(tinkoffCandle1.time, candle1.getTime());

        Candle candle2 = result.get(1);
        AssertUtils.assertEquals(tinkoffCandle2.openPrice, candle2.getOpenPrice());
        AssertUtils.assertEquals(tinkoffCandle2.closePrice, candle2.getClosePrice());
        AssertUtils.assertEquals(tinkoffCandle2.highestPrice, candle2.getHighestPrice());
        AssertUtils.assertEquals(tinkoffCandle2.lowestPrice, candle2.getLowestPrice());
        Assertions.assertEquals(tinkoffCandle2.time, candle2.getTime());

    }

}