package ru.obukhov.investor.model.transform;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.investor.model.Candle;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.obukhov.investor.util.MathUtils.numbersEqual;

public class CandleMapperTest {

    private final CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);

    @Test
    public void mapCandle_mapsFields() {

        final BigDecimal openPrice = BigDecimal.valueOf(100);
        final BigDecimal closePrice = BigDecimal.valueOf(200);
        final BigDecimal highestPrice = BigDecimal.valueOf(1000);
        final BigDecimal lowestPrice = BigDecimal.valueOf(50);
        final OffsetDateTime time = OffsetDateTime.now();

        ru.tinkoff.invest.openapi.models.market.Candle source = new ru.tinkoff.invest.openapi.models.market.Candle(
                "figi",
                CandleInterval.DAY,
                openPrice,
                closePrice,
                highestPrice,
                lowestPrice,
                BigDecimal.valueOf(10),
                time
        );

        Candle result = candleMapper.map(source);

        assertTrue(numbersEqual(result.getOpenPrice(), openPrice));
        assertTrue(numbersEqual(result.getClosePrice(), closePrice));
        assertTrue(numbersEqual(result.getHighestPrice(), highestPrice));
        assertTrue(numbersEqual(result.getLowestPrice(), lowestPrice));
        assertEquals(time, result.getTime());

    }

    @Test
    public void mapCandle_mapsAndRoundsMoneyFields() {

        final BigDecimal openPrice = BigDecimal.valueOf(100.111111);
        final BigDecimal closePrice = BigDecimal.valueOf(200.125);
        final BigDecimal highestPrice = BigDecimal.valueOf(1000.55555);
        final BigDecimal lowestPrice = BigDecimal.valueOf(50.99999);

        ru.tinkoff.invest.openapi.models.market.Candle source = new ru.tinkoff.invest.openapi.models.market.Candle(
                "figi",
                CandleInterval.DAY,
                openPrice,
                closePrice,
                highestPrice,
                lowestPrice,
                BigDecimal.valueOf(10),
                OffsetDateTime.now()
        );

        Candle result = candleMapper.map(source);

        assertTrue(numbersEqual(result.getOpenPrice(), openPrice.setScale(2, RoundingMode.HALF_UP)));
        assertTrue(numbersEqual(result.getClosePrice(), closePrice.setScale(2, RoundingMode.HALF_UP)));
        assertTrue(numbersEqual(result.getHighestPrice(), highestPrice.setScale(2, RoundingMode.HALF_UP)));
        assertTrue(numbersEqual(result.getLowestPrice(), lowestPrice.setScale(2, RoundingMode.HALF_UP)));

    }

    @Test
    public void mapCandle_calculatesSaldo() {

        ru.tinkoff.invest.openapi.models.market.Candle source = new ru.tinkoff.invest.openapi.models.market.Candle(
                "figi",
                CandleInterval.DAY,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(250),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(10),
                OffsetDateTime.now()
        );

        Candle result = candleMapper.map(source);

        assertTrue(numbersEqual(result.getSaldo(), BigDecimal.valueOf(150)));

    }

}