package ru.obukhov.investor.model.transform;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.investor.model.Candle;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.HistoricalCandles;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

        ru.tinkoff.invest.openapi.models.market.Candle source = createTinkoffCandle(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(50)
        );

        Candle result = candleMapper.map(source);

        assertTrue(numbersEqual(result.getOpenPrice(), source.openPrice));
        assertTrue(numbersEqual(result.getClosePrice(), source.closePrice));
        assertTrue(numbersEqual(result.getHighestPrice(), source.highestPrice));
        assertTrue(numbersEqual(result.getLowestPrice(), source.lowestPrice));
        assertEquals(source.time, result.getTime());

    }

    @Test
    public void mapsAndRoundsMoneyFieldsOfSingleCandle() {

        final BigDecimal openPrice = BigDecimal.valueOf(100.111111);
        final BigDecimal closePrice = BigDecimal.valueOf(200.125);
        final BigDecimal highestPrice = BigDecimal.valueOf(1000.55555);
        final BigDecimal lowestPrice = BigDecimal.valueOf(50.99999);

        ru.tinkoff.invest.openapi.models.market.Candle source = createTinkoffCandle(
                openPrice,
                closePrice,
                highestPrice,
                lowestPrice
        );

        Candle result = candleMapper.map(source);

        assertTrue(numbersEqual(result.getOpenPrice(), openPrice.setScale(2, RoundingMode.HALF_UP)));
        assertTrue(numbersEqual(result.getClosePrice(), closePrice.setScale(2, RoundingMode.HALF_UP)));
        assertTrue(numbersEqual(result.getHighestPrice(), highestPrice.setScale(2, RoundingMode.HALF_UP)));
        assertTrue(numbersEqual(result.getLowestPrice(), lowestPrice.setScale(2, RoundingMode.HALF_UP)));

    }

    @Test
    public void calculatesSingleCandleSaldo() {

        ru.tinkoff.invest.openapi.models.market.Candle source = createTinkoffCandle(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(250),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(50)
        );

        Candle result = candleMapper.map(source);

        assertTrue(numbersEqual(result.getSaldo(), BigDecimal.valueOf(150)));

    }

    @Test
    public void mapsHistoricalCandles() {

        ru.tinkoff.invest.openapi.models.market.Candle tinkoffCandle1 = createTinkoffCandle(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(50)
        );

        ru.tinkoff.invest.openapi.models.market.Candle tinkoffCandle2 = createTinkoffCandle(
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(400),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(100)
        );

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

    private ru.tinkoff.invest.openapi.models.market.Candle createTinkoffCandle(BigDecimal openPrice,
                                                                               BigDecimal closePrice,
                                                                               BigDecimal highestPrice,
                                                                               BigDecimal lowestPrice) {
        return new ru.tinkoff.invest.openapi.models.market.Candle(
                StringUtils.EMPTY,
                CandleInterval.DAY,
                openPrice,
                closePrice,
                highestPrice,
                lowestPrice,
                BigDecimal.TEN,
                OffsetDateTime.now()
        );
    }

}