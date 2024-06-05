package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;

class PositionBuilderUnitTest {

    @Test
    void testPrimitives() {
        final String currency = TestShares.APPLE.getCurrency();
        final String figi = TestShares.APPLE.getFigi();
        final InstrumentType instrumentType = InstrumentType.INSTRUMENT_TYPE_SHARE;
        final long quantity = 20;
        final double averagePositionPrice = 500;
        final double expectedYield = 20000;
        final double currentNkd = 12;
        final double currentPrice = 1500;
        final double averagePositionPriceFifo = 300;

        final Position position = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(instrumentType)
                .setQuantity(quantity)
                .setAveragePositionPrice(averagePositionPrice)
                .setExpectedYield(expectedYield)
                .setCurrentNkd(currentNkd)
                .setCurrentPrice(currentPrice)
                .setAveragePositionPriceFifo(averagePositionPriceFifo)
                .build();

        Assertions.assertEquals(figi, position.getFigi());
        Assertions.assertEquals(instrumentType.toString(), position.getInstrumentType());
        AssertUtils.assertEquals(quantity, position.getQuantity());

        AssertUtils.assertEquals(averagePositionPrice, position.getAveragePositionPrice().getValue());
        Assertions.assertEquals(currency, position.getAveragePositionPrice().getCurrency());

        AssertUtils.assertEquals(expectedYield, position.getExpectedYield());

        AssertUtils.assertEquals(currentNkd, position.getCurrentNkd().getValue());
        Assertions.assertEquals(currency, position.getCurrentNkd().getCurrency());

        AssertUtils.assertEquals(currentPrice, position.getCurrentPrice().getValue());
        Assertions.assertEquals(currency, position.getCurrentPrice().getCurrency());

        AssertUtils.assertEquals(averagePositionPriceFifo, position.getAveragePositionPriceFifo().getValue());
        Assertions.assertEquals(currency, position.getAveragePositionPriceFifo().getCurrency());

        AssertUtils.assertEquals(0, position.getQuantityLots());
    }

    @Test
    void testBigDecimals() {
        final String currency = TestShares.APPLE.getCurrency();
        final String figi = TestShares.APPLE.getFigi();
        final InstrumentType instrumentType = InstrumentType.INSTRUMENT_TYPE_SHARE;
        final BigDecimal quantity = BigDecimal.valueOf(20);
        final BigDecimal averagePositionPrice = BigDecimal.valueOf(500);
        final BigDecimal expectedYield = BigDecimal.valueOf(20000);
        final BigDecimal currentNkd = BigDecimal.valueOf(12);
        final BigDecimal currentPrice = BigDecimal.valueOf(1500);
        final BigDecimal averagePositionPriceFifo = BigDecimal.valueOf(300);

        final Position position = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(instrumentType)
                .setQuantity(quantity)
                .setAveragePositionPrice(averagePositionPrice)
                .setExpectedYield(expectedYield)
                .setCurrentNkd(currentNkd)
                .setCurrentPrice(currentPrice)
                .setAveragePositionPriceFifo(averagePositionPriceFifo)
                .build();

        Assertions.assertEquals(figi, position.getFigi());
        Assertions.assertEquals(instrumentType.toString(), position.getInstrumentType());
        AssertUtils.assertEquals(quantity, position.getQuantity());

        AssertUtils.assertEquals(averagePositionPrice, position.getAveragePositionPrice().getValue());
        Assertions.assertEquals(currency, position.getAveragePositionPrice().getCurrency());

        AssertUtils.assertEquals(expectedYield, position.getExpectedYield());

        AssertUtils.assertEquals(currentNkd, position.getCurrentNkd().getValue());
        Assertions.assertEquals(currency, position.getCurrentNkd().getCurrency());

        AssertUtils.assertEquals(currentPrice, position.getCurrentPrice().getValue());
        Assertions.assertEquals(currency, position.getCurrentPrice().getCurrency());

        AssertUtils.assertEquals(averagePositionPriceFifo, position.getAveragePositionPriceFifo().getValue());
        Assertions.assertEquals(currency, position.getAveragePositionPriceFifo().getCurrency());

        AssertUtils.assertEquals(0, position.getQuantityLots());
    }

    @Test
    void testMoney() {
        final String currency = TestShares.APPLE.getCurrency();
        final String figi = TestShares.APPLE.getFigi();
        final InstrumentType instrumentType = InstrumentType.INSTRUMENT_TYPE_SHARE;
        final int quantity = 20;
        final Money averagePositionPrice = TestData.newMoney(500, currency);
        final BigDecimal expectedYield = BigDecimal.valueOf(20000);
        final Money currentNkd = TestData.newMoney(12, currency);
        final Money currentPrice = TestData.newMoney(1500, currency);
        final Money averagePositionPriceFifo = TestData.newMoney(300, currency);

        final Position position = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(figi)
                .setInstrumentType(instrumentType)
                .setQuantity(quantity)
                .setAveragePositionPrice(averagePositionPrice)
                .setExpectedYield(expectedYield)
                .setCurrentNkd(currentNkd)
                .setCurrentPrice(currentPrice)
                .setAveragePositionPriceFifo(averagePositionPriceFifo)
                .build();

        Assertions.assertEquals(figi, position.getFigi());
        Assertions.assertEquals(instrumentType.toString(), position.getInstrumentType());
        AssertUtils.assertEquals(quantity, position.getQuantity());

        AssertUtils.assertEquals(averagePositionPrice, position.getAveragePositionPrice());
        Assertions.assertEquals(currency, position.getAveragePositionPrice().getCurrency());

        AssertUtils.assertEquals(expectedYield, position.getExpectedYield());

        AssertUtils.assertEquals(currentNkd, position.getCurrentNkd());
        Assertions.assertEquals(currency, position.getCurrentNkd().getCurrency());

        AssertUtils.assertEquals(currentPrice, position.getCurrentPrice());
        Assertions.assertEquals(currency, position.getCurrentPrice().getCurrency());

        AssertUtils.assertEquals(averagePositionPriceFifo, position.getAveragePositionPriceFifo());
        Assertions.assertEquals(currency, position.getAveragePositionPriceFifo().getCurrency());

        AssertUtils.assertEquals(0, position.getQuantityLots());
    }

}