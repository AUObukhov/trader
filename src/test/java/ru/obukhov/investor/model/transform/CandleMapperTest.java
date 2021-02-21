package ru.obukhov.investor.model.transform;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.HistoricalCandles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.obukhov.investor.util.MathUtils.numbersEqual;

public class CandleMapperTest {

    private final CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);

    @Test
    public void mapsSingleCandleFields() {

        ru.tinkoff.invest.openapi.models.market.Candle source = TestDataHelper.createTinkoffCandle(
                100, 200, 1000, 50);

        Candle result = candleMapper.map(source);

        assertTrue(numbersEqual(result.getOpenPrice(), source.openPrice));
        assertTrue(numbersEqual(result.getClosePrice(), source.closePrice));
        assertTrue(numbersEqual(result.getHighestPrice(), source.highestPrice));
        assertTrue(numbersEqual(result.getLowestPrice(), source.lowestPrice));
        assertEquals(source.time, result.getTime());

    }

    @Test
    public void updatesOffset() {

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

        assertEquals(source.time, result.getTime());

    }

    @Test
    public void mapsHistoricalCandles() {

        ru.tinkoff.invest.openapi.models.market.Candle tinkoffCandle1 = TestDataHelper.createTinkoffCandle(
                100, 200, 1000, 50);

        ru.tinkoff.invest.openapi.models.market.Candle tinkoffCandle2 = TestDataHelper.createTinkoffCandle(
                200, 400, 2000, 100);

        List<ru.tinkoff.invest.openapi.models.market.Candle> candles = newArrayList(tinkoffCandle1, tinkoffCandle2);
        HistoricalCandles source = new HistoricalCandles(StringUtils.EMPTY, CandleInterval.DAY, candles);

        List<Candle> result = candleMapper.map(source);
        assertEquals(2, result.size());

        Candle candle1 = result.get(0);
        assertTrue(numbersEqual(candle1.getOpenPrice(), tinkoffCandle1.openPrice));
        assertTrue(numbersEqual(candle1.getClosePrice(), tinkoffCandle1.closePrice));
        assertTrue(numbersEqual(candle1.getHighestPrice(), tinkoffCandle1.highestPrice));
        assertTrue(numbersEqual(candle1.getLowestPrice(), tinkoffCandle1.lowestPrice));
        assertEquals(tinkoffCandle1.time, candle1.getTime());

        Candle candle2 = result.get(1);
        assertTrue(numbersEqual(candle2.getOpenPrice(), tinkoffCandle2.openPrice));
        assertTrue(numbersEqual(candle2.getClosePrice(), tinkoffCandle2.closePrice));
        assertTrue(numbersEqual(candle2.getHighestPrice(), tinkoffCandle2.highestPrice));
        assertTrue(numbersEqual(candle2.getLowestPrice(), tinkoffCandle2.lowestPrice));
        assertEquals(tinkoffCandle2.time, candle2.getTime());

    }

}