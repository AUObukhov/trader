package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.PortfolioPositionBuilder;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.tinkoff.piapi.contract.v1.InstrumentType;

import java.math.BigDecimal;

class PortfolioPositionUnitTest {

    @Test
    void getCurrency() {
        final Currency currency = TestShare1.CURRENCY;
        final PortfolioPosition position = new PortfolioPositionBuilder()
                .setTicker(TestShare1.TICKER)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setAveragePositionPrice(10)
                .setExpectedYield(5)
                .setCurrentPrice(15)
                .setQuantityLots(1)
                .setCurrency(currency)
                .setLotSize(1)
                .build();
        Assertions.assertEquals(currency, position.getCurrency());
    }

    @Test
    void getTotalPrice() {
        final PortfolioPosition position = new PortfolioPositionBuilder()
                .setTicker(TestShare1.TICKER)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setAveragePositionPrice(10)
                .setExpectedYield(15)
                .setCurrentPrice(15)
                .setQuantityLots(3)
                .setCurrency(TestShare1.CURRENCY)
                .setLotSize(1)
                .build();
        AssertUtils.assertEquals(30, position.getTotalPrice());
    }

    @Test
    void addQuantities() {
        final Currency currency = TestShare1.CURRENCY;
        final PortfolioPosition position = new PortfolioPositionBuilder()
                .setTicker(TestShare1.TICKER)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setAveragePositionPrice(10)
                .setExpectedYield(15)
                .setCurrentPrice(20)
                .setQuantityLots(3)
                .setCurrency(currency)
                .setLotSize(1)
                .build();
        final PortfolioPosition newPosition = position.addQuantities(
                2,
                2,
                DecimalUtils.setDefaultScale(30),
                DecimalUtils.setDefaultScale(15)
        );

        AssertUtils.assertEquals(5, newPosition.quantity());
        AssertUtils.assertEquals(12, newPosition.averagePositionPrice().value());
        Assertions.assertEquals(currency, newPosition.averagePositionPrice().currency());
        AssertUtils.assertEquals(15, newPosition.expectedYield());
        AssertUtils.assertEquals(5, newPosition.quantityLots());
        AssertUtils.assertEquals(15, newPosition.currentPrice().value());
        Assertions.assertEquals(currency, newPosition.currentPrice().currency());
        AssertUtils.assertEquals(60, newPosition.getTotalPrice());
    }

    @Test
    void cloneWithNewQuantity() {
        final PortfolioPosition position = new PortfolioPositionBuilder()
                .setTicker(TestShare1.TICKER)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setAveragePositionPrice(10)
                .setExpectedYield(300)
                .setCurrentPrice(20)
                .setQuantityLots(3)
                .setCurrency(TestShare1.CURRENCY)
                .setLotSize(10)
                .build();
        final BigDecimal newQuantity = BigDecimal.valueOf(20);
        final BigDecimal newQuantityLots = BigDecimal.valueOf(2);

        final PortfolioPosition newPosition = position.cloneWithNewQuantity(newQuantity, newQuantityLots);

        final PortfolioPosition expectedPosition = new PortfolioPosition(
                null,
                position.ticker(),
                position.instrumentType(),
                newQuantity,
                position.averagePositionPrice(),
                position.expectedYield(),
                position.currentPrice(),
                newQuantityLots
        );

        Assertions.assertEquals(expectedPosition, newPosition);
    }

    @Test
    void cloneWithNewValues() {
        final Currency currency = TestShare1.CURRENCY;
        final PortfolioPosition position = new PortfolioPositionBuilder()
                .setTicker(TestShare1.TICKER)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setAveragePositionPrice(10)
                .setExpectedYield(300)
                .setCurrentPrice(20)
                .setQuantityLots(3)
                .setCurrency(currency)
                .setLotSize(10)
                .build();

        final BigDecimal newQuantity = BigDecimal.valueOf(20);
        final BigDecimal newExpectedYield = DecimalUtils.setDefaultScale(400);
        final BigDecimal newCurrentPrice = DecimalUtils.setDefaultScale(30);
        final BigDecimal newQuantityLots = BigDecimal.valueOf(2);

        final PortfolioPosition newPosition = position.cloneWithNewValues(newQuantity, newExpectedYield, newCurrentPrice, newQuantityLots);

        final PortfolioPosition expectedPosition = new PortfolioPosition(
                null,
                position.ticker(),
                position.instrumentType(),
                newQuantity,
                position.averagePositionPrice(),
                newExpectedYield,
                Money.of(currency, newCurrentPrice),
                newQuantityLots
        );

        AssertUtils.assertEquals(expectedPosition, newPosition);
    }

}