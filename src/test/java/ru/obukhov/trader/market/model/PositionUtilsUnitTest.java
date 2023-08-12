package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;

class PositionUtilsUnitTest {

    @Test
    void getCurrency() {
        final String currency = TestShare1.CURRENCY;
        final Position position = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(TestShare1.FIGI)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(1)
                .setAveragePositionPrice(10)
                .setExpectedYield(5)
                .setCurrentPrice(15)
                .build();
        Assertions.assertEquals(currency, PositionUtils.getCurrency(position));
    }

    @Test
    void getTotalPrice() {
        final Position position = new PositionBuilder()
                .setCurrency(TestShare1.CURRENCY)
                .setFigi(TestShare1.FIGI)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(3)
                .setAveragePositionPrice(10)
                .setExpectedYield(15)
                .setCurrentPrice(15)
                .build();
        AssertUtils.assertEquals(30, PositionUtils.getTotalPrice(position));
    }

    @Test
    void addQuantities() {
        final String currency = TestShare1.CURRENCY;
        final Position position = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(TestShare1.FIGI)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(3)
                .setAveragePositionPrice(10)
                .setExpectedYield(15)
                .setCurrentNkd(123.123)
                .setAveragePositionPricePt(456.456)
                .setCurrentPrice(20)
                .setAveragePositionPriceFifo(789.789)
                .setQuantityLots(3)
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

        AssertUtils.assertEquals(123.123, newPosition.getCurrentNkd().getValue());
        Assertions.assertEquals(currency, newPosition.getCurrentNkd().getCurrency());

        AssertUtils.assertEquals(456.456, newPosition.getAveragePositionPricePt());

        AssertUtils.assertEquals(15, newPosition.getCurrentPrice().getValue());
        Assertions.assertEquals(currency, newPosition.getCurrentPrice().getCurrency());

        AssertUtils.assertEquals(789.789, newPosition.getAveragePositionPriceFifo().getValue());
        Assertions.assertEquals(currency, newPosition.getAveragePositionPriceFifo().getCurrency());

        AssertUtils.assertEquals(5, newPosition.getQuantityLots());
        AssertUtils.assertEquals(60, PositionUtils.getTotalPrice(newPosition));
    }

    @Test
    void cloneWithNewQuantity() {
        final String currency = TestShare1.CURRENCY;
        final Position position = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(TestShare1.FIGI)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(3)
                .setAveragePositionPrice(10)
                .setExpectedYield(300)
                .setCurrentNkd(123.123)
                .setAveragePositionPricePt(456.456)
                .setCurrentPrice(20)
                .setAveragePositionPriceFifo(789.789)
                .setQuantityLots(4)
                .build();

        final BigDecimal newQuantity = DecimalUtils.setDefaultScale(20);
        final BigDecimal newQuantityLots = DecimalUtils.setDefaultScale(2);

        final Position newPosition = PositionUtils.cloneWithNewQuantity(position, newQuantity, newQuantityLots);

        final Position expectedPosition = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(position.getFigi())
                .setInstrumentType(position.getInstrumentType())
                .setQuantity(newQuantity)
                .setAveragePositionPrice(position.getAveragePositionPrice())
                .setExpectedYield(position.getExpectedYield())
                .setCurrentNkd(position.getCurrentNkd())
                .setAveragePositionPricePt(position.getAveragePositionPricePt())
                .setCurrentPrice(position.getCurrentPrice())
                .setAveragePositionPriceFifo(position.getAveragePositionPriceFifo())
                .setQuantityLots(newQuantityLots)
                .build();

        AssertUtils.assertEquals(expectedPosition, newPosition);
    }

    @Test
    void cloneWithNewValues() {
        final String currency = TestShare1.CURRENCY;
        final Position position = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(TestShare1.FIGI)
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(3)
                .setAveragePositionPrice(10)
                .setExpectedYield(300)
                .setCurrentNkd(123.123)
                .setAveragePositionPricePt(456.456)
                .setCurrentPrice(20)
                .setAveragePositionPriceFifo(789.789)
                .setQuantityLots(4)
                .build();

        final BigDecimal newQuantity = DecimalUtils.setDefaultScale(20);
        final BigDecimal newExpectedYield = DecimalUtils.setDefaultScale(400);
        final BigDecimal newCurrentPrice = DecimalUtils.setDefaultScale(30);
        final BigDecimal newQuantityLots = DecimalUtils.setDefaultScale(2);

        final Position newPosition = PositionUtils.cloneWithNewValues(position, newQuantity, newExpectedYield, newCurrentPrice, newQuantityLots);

        final Position expectedPosition = new PositionBuilder()
                .setCurrency(currency)
                .setFigi(position.getFigi())
                .setInstrumentType(position.getInstrumentType())
                .setQuantity(newQuantity)
                .setAveragePositionPrice(position.getAveragePositionPrice())
                .setExpectedYield(newExpectedYield)
                .setCurrentNkd(position.getCurrentNkd())
                .setAveragePositionPricePt(position.getAveragePositionPricePt())
                .setCurrentPrice(newCurrentPrice)
                .setAveragePositionPriceFifo(position.getAveragePositionPriceFifo())
                .setQuantityLots(newQuantityLots)
                .build();

        AssertUtils.assertEquals(expectedPosition, newPosition);
    }

}