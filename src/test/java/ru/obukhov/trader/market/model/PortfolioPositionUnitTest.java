package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;

import java.math.BigDecimal;

class PortfolioPositionUnitTest {

    @Test
    void getCurrency() {
        final Currency currency = Currency.EUR;
        final PortfolioPosition position = TestData.createPortfolioPosition(
                "ticker",
                InstrumentType.STOCK,
                1,
                10,
                5,
                15,
                1,
                currency
        );

        Assertions.assertEquals(currency.name(), position.getCurrency());
    }

    @Test
    void getTotalPrice() {
        final PortfolioPosition position = TestData.createPortfolioPosition(
                "ticker",
                InstrumentType.STOCK,
                3,
                10,
                15,
                15,
                3,
                Currency.EUR
        );

        AssertUtils.assertEquals(30, position.getTotalPrice());
    }

    @Test
    void addQuantities() {
        final PortfolioPosition position = TestData.createPortfolioPosition(
                "ticker",
                InstrumentType.STOCK,
                3,
                10,
                15,
                20,
                3,
                Currency.EUR
        );

        final PortfolioPosition newPosition = position.addQuantities(
                2,
                2,
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(15)
        );

        AssertUtils.assertEquals(5, newPosition.quantity());
        AssertUtils.assertEquals(12, newPosition.averagePositionPrice().value());
        Assertions.assertEquals(Currency.EUR.name(), newPosition.averagePositionPrice().currency());
        AssertUtils.assertEquals(15, newPosition.expectedYield());
        AssertUtils.assertEquals(5, newPosition.quantityLots());
        AssertUtils.assertEquals(15, newPosition.currentPrice().value());
        Assertions.assertEquals(Currency.EUR.name(), newPosition.currentPrice().currency());
        AssertUtils.assertEquals(60, newPosition.getTotalPrice());
    }

    @Test
    void cloneWithNewQuantity() {
        final PortfolioPosition position = TestData.createPortfolioPosition(
                "ticker",
                InstrumentType.STOCK,
                30,
                10,
                300,
                20,
                3,
                Currency.EUR
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

}