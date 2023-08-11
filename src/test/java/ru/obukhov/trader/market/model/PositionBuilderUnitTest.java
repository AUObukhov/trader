package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;

class PositionBuilderUnitTest {

    @Test
    void testPrimitives() {
        final String currency = TestShare1.CURRENCY;
        final String figi = TestShare1.FIGI;
        final InstrumentType instrumentType = InstrumentType.INSTRUMENT_TYPE_SHARE;
        final long quantity = 20;
        final double averagePositionPrice = 500;
        final double expectedYield = 20000;
        final double currentNkd = 12;
        final double averagePositionPricePt = 200;
        final double currentPrice = 1500;
        final double averagePositionPriceFifo = 300;
        final long quantityLots = 2;

        final Position position = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(instrumentType)
                .setQuantity(quantity)
                .setAveragePositionPrice(averagePositionPrice)
                .setExpectedYield(expectedYield)
                .setCurrentNkd(currentNkd)
                .setAveragePositionPricePt(averagePositionPricePt)
                .setCurrentPrice(currentPrice)
                .setAveragePositionPriceFifo(averagePositionPriceFifo)
                .setQuantityLots(quantityLots)
                .build();

        Assertions.assertEquals(figi, position.getFigi());
        Assertions.assertEquals(instrumentType.toString(), position.getInstrumentType());
        AssertUtils.assertEquals(quantity, position.getQuantity());

        AssertUtils.assertEquals(averagePositionPrice, position.getAveragePositionPrice().getValue());
        Assertions.assertEquals(currency, position.getAveragePositionPrice().getCurrency());

        AssertUtils.assertEquals(expectedYield, position.getExpectedYield());

        AssertUtils.assertEquals(currentNkd, position.getCurrentNkd().getValue());
        Assertions.assertEquals(currency, position.getCurrentNkd().getCurrency());

        AssertUtils.assertEquals(averagePositionPricePt, position.getAveragePositionPricePt());

        AssertUtils.assertEquals(currentPrice, position.getCurrentPrice().getValue());
        Assertions.assertEquals(currency, position.getCurrentPrice().getCurrency());

        AssertUtils.assertEquals(averagePositionPriceFifo, position.getAveragePositionPriceFifo().getValue());
        Assertions.assertEquals(currency, position.getAveragePositionPriceFifo().getCurrency());

        AssertUtils.assertEquals(quantityLots, position.getQuantityLots());
    }

    @Test
    void testBigDecimals() {
        final String currency = TestShare1.CURRENCY;
        final String figi = TestShare1.FIGI;
        final InstrumentType instrumentType = InstrumentType.INSTRUMENT_TYPE_SHARE;
        final BigDecimal quantity = BigDecimal.valueOf(20);
        final BigDecimal averagePositionPrice = BigDecimal.valueOf(500);
        final BigDecimal expectedYield = BigDecimal.valueOf(20000);
        final BigDecimal currentNkd = BigDecimal.valueOf(12);
        final BigDecimal averagePositionPricePt = BigDecimal.valueOf(200);
        final BigDecimal currentPrice = BigDecimal.valueOf(1500);
        final BigDecimal averagePositionPriceFifo = BigDecimal.valueOf(300);
        final BigDecimal quantityLots = BigDecimal.valueOf(2);

        final Position position = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(instrumentType)
                .setQuantity(quantity)
                .setAveragePositionPrice(averagePositionPrice)
                .setExpectedYield(expectedYield)
                .setCurrentNkd(currentNkd)
                .setAveragePositionPricePt(averagePositionPricePt)
                .setCurrentPrice(currentPrice)
                .setAveragePositionPriceFifo(averagePositionPriceFifo)
                .setQuantityLots(quantityLots)
                .build();

        Assertions.assertEquals(figi, position.getFigi());
        Assertions.assertEquals(instrumentType.toString(), position.getInstrumentType());
        AssertUtils.assertEquals(quantity, position.getQuantity());

        AssertUtils.assertEquals(averagePositionPrice, position.getAveragePositionPrice().getValue());
        Assertions.assertEquals(currency, position.getAveragePositionPrice().getCurrency());

        AssertUtils.assertEquals(expectedYield, position.getExpectedYield());

        AssertUtils.assertEquals(currentNkd, position.getCurrentNkd().getValue());
        Assertions.assertEquals(currency, position.getCurrentNkd().getCurrency());

        AssertUtils.assertEquals(averagePositionPricePt, position.getAveragePositionPricePt());

        AssertUtils.assertEquals(currentPrice, position.getCurrentPrice().getValue());
        Assertions.assertEquals(currency, position.getCurrentPrice().getCurrency());

        AssertUtils.assertEquals(averagePositionPriceFifo, position.getAveragePositionPriceFifo().getValue());
        Assertions.assertEquals(currency, position.getAveragePositionPriceFifo().getCurrency());

        AssertUtils.assertEquals(quantityLots, position.getQuantityLots());
    }

    @Test
    void testMoney() {
        final String currency = TestShare1.CURRENCY;
        final String figi = TestShare1.FIGI;
        final InstrumentType instrumentType = InstrumentType.INSTRUMENT_TYPE_SHARE;
        final int quantity = 20;
        final Money averagePositionPrice = TestData.createMoney(500, currency);
        final BigDecimal expectedYield = BigDecimal.valueOf(20000);
        final Money currentNkd = TestData.createMoney(12, currency);
        final BigDecimal averagePositionPricePt = BigDecimal.valueOf(200);
        final Money currentPrice = TestData.createMoney(1500, currency);
        final Money averagePositionPriceFifo = TestData.createMoney(300, currency);
        final BigDecimal quantityLots = BigDecimal.valueOf(2);

        final Position position = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(instrumentType)
                .setQuantity(quantity)
                .setAveragePositionPrice(averagePositionPrice)
                .setExpectedYield(expectedYield)
                .setCurrentNkd(currentNkd)
                .setAveragePositionPricePt(averagePositionPricePt)
                .setCurrentPrice(currentPrice)
                .setAveragePositionPriceFifo(averagePositionPriceFifo)
                .setQuantityLots(quantityLots)
                .build();

        Assertions.assertEquals(figi, position.getFigi());
        Assertions.assertEquals(instrumentType.toString(), position.getInstrumentType());
        AssertUtils.assertEquals(quantity, position.getQuantity());

        AssertUtils.assertEquals(averagePositionPrice, position.getAveragePositionPrice());
        Assertions.assertEquals(currency, position.getAveragePositionPrice().getCurrency());

        AssertUtils.assertEquals(expectedYield, position.getExpectedYield());

        AssertUtils.assertEquals(currentNkd, position.getCurrentNkd());
        Assertions.assertEquals(currency, position.getCurrentNkd().getCurrency());

        AssertUtils.assertEquals(averagePositionPricePt, position.getAveragePositionPricePt());

        AssertUtils.assertEquals(currentPrice, position.getCurrentPrice());
        Assertions.assertEquals(currency, position.getCurrentPrice().getCurrency());

        AssertUtils.assertEquals(averagePositionPriceFifo, position.getAveragePositionPriceFifo());
        Assertions.assertEquals(currency, position.getAveragePositionPriceFifo().getCurrency());

        AssertUtils.assertEquals(quantityLots, position.getQuantityLots());
    }

}