package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;

import java.math.BigDecimal;

class PortfolioPositionUnitTest {

    @Test
    void getCurrency() {
        final Currency currency = TestShare1.CURRENCY;
        final PortfolioPosition position = TestData.createPortfolioPosition(
                TestShare1.TICKER,
                InstrumentType.STOCK,
                1,
                10,
                5,
                15,
                1,
                currency
        );

        Assertions.assertEquals(currency, position.getCurrency());
    }

    @Test
    void getTotalPrice() {
        final PortfolioPosition position = TestData.createPortfolioPosition(
                TestShare1.TICKER,
                InstrumentType.STOCK,
                3,
                10,
                15,
                15,
                3,
                TestShare1.CURRENCY
        );

        AssertUtils.assertEquals(30, position.getTotalPrice());
    }

    @Test
    void addQuantities() {
        final Currency currency = TestShare1.CURRENCY;
        final PortfolioPosition position = TestData.createPortfolioPosition(
                TestShare1.TICKER,
                InstrumentType.STOCK,
                3,
                10,
                15,
                20,
                3,
                currency
        );

        final PortfolioPosition newPosition = position.addQuantities(
                2,
                2,
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(15)
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
        final PortfolioPosition position = TestData.createPortfolioPosition(
                TestShare1.TICKER,
                InstrumentType.STOCK,
                30,
                10,
                300,
                20,
                3,
                TestShare1.CURRENCY
        );

        final BigDecimal newQuantity = BigDecimal.valueOf(20);
        final BigDecimal newQuantityLots = BigDecimal.valueOf(2);

        final PortfolioPosition newPosition = position.cloneWithNewQuantity(newQuantity, newQuantityLots);

        final PortfolioPosition expectedPosition = new PortfolioPosition(
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
        final PortfolioPosition position = TestData.createPortfolioPosition(
                TestShare1.TICKER,
                InstrumentType.STOCK,
                30,
                10,
                300,
                20,
                3,
                currency
        );

        final BigDecimal newQuantity = BigDecimal.valueOf(20);
        final BigDecimal newExpectedYield = BigDecimal.valueOf(400);
        final BigDecimal newCurrentPrice = BigDecimal.valueOf(30);
        final BigDecimal newQuantityLots = BigDecimal.valueOf(2);

        final PortfolioPosition newPosition = position.cloneWithNewValues(newQuantity, newExpectedYield, newCurrentPrice, newQuantityLots);

        final PortfolioPosition expectedPosition = new PortfolioPosition(
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