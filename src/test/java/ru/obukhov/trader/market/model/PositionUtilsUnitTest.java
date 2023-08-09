package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;

class PositionUtilsUnitTest {

    @Test
    void getCurrency() {
        final String currency = TestShare1.CURRENCY;
        final Position position = Position.builder()
                .figi(TestShare1.FIGI)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(DecimalUtils.setDefaultScale(1))
                .averagePositionPrice(TestData.createMoney(currency, 10))
                .expectedYield(DecimalUtils.setDefaultScale(5))
                .currentPrice(TestData.createMoney(currency, 15))
                .build();
        Assertions.assertEquals(currency, PositionUtils.getCurrency(position));
    }

    @Test
    void getTotalPrice() {
        final Position position = Position.builder()
                .figi(TestShare1.FIGI)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(DecimalUtils.setDefaultScale(3))
                .averagePositionPrice(TestData.createMoney(TestShare1.CURRENCY, 10))
                .expectedYield(DecimalUtils.setDefaultScale(15))
                .currentPrice(TestData.createMoney(TestShare1.CURRENCY, 15))
                .build();
        AssertUtils.assertEquals(30, PositionUtils.getTotalPrice(position));
    }

    @Test
    void addQuantities() {
        final String currency = TestShare1.CURRENCY;
        final Position position = Position.builder()
                .figi(TestShare1.FIGI)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(DecimalUtils.setDefaultScale(3))
                .averagePositionPrice(TestData.createMoney(currency, 10))
                .expectedYield(DecimalUtils.setDefaultScale(15))
                .currentPrice(TestData.createMoney(currency, 20))
                .quantityLots(DecimalUtils.setDefaultScale(3))
                .build();
        final Position newPosition = PositionUtils.addQuantities(
                position,
                2,
                2,
                DecimalUtils.setDefaultScale(30),
                DecimalUtils.setDefaultScale(15)
        );

        AssertUtils.assertEquals(5, newPosition.getQuantity());
        AssertUtils.assertEquals(12, newPosition.getAveragePositionPrice().getValue());
        Assertions.assertEquals(currency, newPosition.getAveragePositionPrice().getCurrency());
        AssertUtils.assertEquals(15, newPosition.getExpectedYield());
        AssertUtils.assertEquals(15, newPosition.getCurrentPrice().getValue());
        Assertions.assertEquals(currency, newPosition.getCurrentPrice().getCurrency());
        AssertUtils.assertEquals(5, newPosition.getQuantityLots());
        AssertUtils.assertEquals(60, PositionUtils.getTotalPrice(newPosition));
    }

    @Test
    void cloneWithNewQuantity() {
        final String currency = TestShare1.CURRENCY;
        final Position position = Position.builder()
                .figi(TestShare1.FIGI)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(DecimalUtils.setDefaultScale(3))
                .averagePositionPrice(TestData.createMoney(currency, 10))
                .expectedYield(DecimalUtils.setDefaultScale(300))
                .currentPrice(TestData.createMoney(currency, 20))
                .quantityLots(DecimalUtils.setDefaultScale(4))
                .build();

        final BigDecimal newQuantity = DecimalUtils.setDefaultScale(20);
        final BigDecimal newQuantityLots = DecimalUtils.setDefaultScale(2);

        final Position newPosition = PositionUtils.cloneWithNewQuantity(position, newQuantity, newQuantityLots);

        final Position expectedPosition = Position.builder()
                .figi(position.getFigi())
                .instrumentType(position.getInstrumentType())
                .quantity(newQuantity)
                .averagePositionPrice(position.getAveragePositionPrice())
                .expectedYield(position.getExpectedYield())
                .currentPrice(position.getCurrentPrice())
                .quantityLots(newQuantityLots)
                .build();

        AssertUtils.assertEquals(expectedPosition, newPosition);
    }

    @Test
    void cloneWithNewValues() {
        final String currency = TestShare1.CURRENCY;
        final Position position = Position.builder()
                .figi(TestShare1.FIGI)
                .instrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE.toString())
                .quantity(DecimalUtils.setDefaultScale(3))
                .averagePositionPrice(TestData.createMoney(currency, 10))
                .expectedYield(DecimalUtils.setDefaultScale(300))
                .currentPrice(TestData.createMoney(currency, 20))
                .quantityLots(DecimalUtils.setDefaultScale(4))
                .build();

        final BigDecimal newQuantity = DecimalUtils.setDefaultScale(20);
        final BigDecimal newExpectedYield = DecimalUtils.setDefaultScale(400);
        final BigDecimal newCurrentPrice = DecimalUtils.setDefaultScale(30);
        final BigDecimal newQuantityLots = DecimalUtils.setDefaultScale(2);

        final Position newPosition = PositionUtils.cloneWithNewValues(position, newQuantity, newExpectedYield, newCurrentPrice, newQuantityLots);

        final Position expectedPosition = Position.builder()
                .figi(position.getFigi())
                .instrumentType(position.getInstrumentType())
                .quantity(newQuantity)
                .averagePositionPrice(position.getAveragePositionPrice())
                .expectedYield(newExpectedYield)
                .currentPrice(DataStructsHelper.createMoney(currency, newCurrentPrice))
                .quantityLots(newQuantityLots)
                .build();

        AssertUtils.assertEquals(expectedPosition, newPosition);
    }

}