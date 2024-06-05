package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;

class PositionUtilsUnitTest {

    @Test
    void getCurrency() {
        final TestShare share = TestShares.APPLE;
        final Position position = new PositionBuilder()
                .setCurrency(share.getCurrency())
                .setFigi(share.getFigi())
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(1)
                .setAveragePositionPrice(10)
                .setExpectedYield(5)
                .setCurrentPrice(15)
                .build();
        Assertions.assertEquals(share.getCurrency(), PositionUtils.getCurrency(position));
    }

    @Test
    void getTotalPrice() {
        final TestShare share = TestShares.APPLE;
        final Position position = new PositionBuilder()
                .setCurrency(share.getCurrency())
                .setFigi(share.getFigi())
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
        final TestShare share = TestShares.APPLE;
        final Position position = new PositionBuilder()
                .setCurrency(share.getCurrency())
                .setFigi(share.getFigi())
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(3)
                .setAveragePositionPrice(10)
                .setExpectedYield(15)
                .setCurrentNkd(123.123)
                .setCurrentPrice(20)
                .setAveragePositionPriceFifo(789.789)
                .build();
        final Position newPosition = PositionUtils.addQuantities(
                position,
                2,
                DecimalUtils.setDefaultScale(30),
                DecimalUtils.setDefaultScale(15)
        );

        AssertUtils.assertEquals(5, newPosition.getQuantity());

        AssertUtils.assertEquals(12, newPosition.getAveragePositionPrice().getValue());
        Assertions.assertEquals(share.getCurrency(), newPosition.getAveragePositionPrice().getCurrency());

        AssertUtils.assertEquals(15, newPosition.getExpectedYield());

        AssertUtils.assertEquals(123.123, newPosition.getCurrentNkd().getValue());
        Assertions.assertEquals(share.getCurrency(), newPosition.getCurrentNkd().getCurrency());

        AssertUtils.assertEquals(15, newPosition.getCurrentPrice().getValue());
        Assertions.assertEquals(share.getCurrency(), newPosition.getCurrentPrice().getCurrency());

        AssertUtils.assertEquals(789.789, newPosition.getAveragePositionPriceFifo().getValue());
        Assertions.assertEquals(share.getCurrency(), newPosition.getAveragePositionPriceFifo().getCurrency());

        AssertUtils.assertEquals(5, newPosition.getQuantity());
        AssertUtils.assertEquals(60, PositionUtils.getTotalPrice(newPosition));
    }

    @Test
    void cloneWithNewCurrentPrice() {
        final TestShare share = TestShares.APPLE;
        final Position position = new PositionBuilder()
                .setCurrency(share.getCurrency())
                .setFigi(share.getFigi())
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(3)
                .setAveragePositionPrice(10)
                .setExpectedYield(300)
                .setCurrentNkd(123.123)
                .setCurrentPrice(20)
                .setAveragePositionPriceFifo(789.789)
                .build();

        final BigDecimal newCurrentPrice = DecimalUtils.setDefaultScale(30);

        final Position newPosition = PositionUtils.cloneWithNewCurrentPrice(position, newCurrentPrice);

        final Position expectedPosition = new PositionBuilder()
                .setCurrency(share.getCurrency())
                .setFigi(position.getFigi())
                .setInstrumentType(position.getInstrumentType())
                .setQuantity(position.getQuantity())
                .setAveragePositionPrice(position.getAveragePositionPrice())
                .setExpectedYield(position.getExpectedYield())
                .setCurrentNkd(position.getCurrentNkd())
                .setCurrentPrice(newCurrentPrice)
                .setAveragePositionPriceFifo(position.getAveragePositionPriceFifo())
                .build();

        AssertUtils.assertEquals(expectedPosition, newPosition);
    }

    @Test
    void cloneWithNewValues() {
        final TestShare share = TestShares.APPLE;
        final Position position = new PositionBuilder()
                .setCurrency(share.getCurrency())
                .setFigi(share.getFigi())
                .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setQuantity(3)
                .setAveragePositionPrice(10)
                .setExpectedYield(300)
                .setCurrentNkd(123.123)
                .setCurrentPrice(20)
                .setAveragePositionPriceFifo(789.789)
                .build();

        final BigDecimal newQuantity = DecimalUtils.setDefaultScale(20);
        final BigDecimal newExpectedYield = DecimalUtils.setDefaultScale(400);
        final BigDecimal newCurrentPrice = DecimalUtils.setDefaultScale(30);

        final Position newPosition = PositionUtils.cloneWithNewValues(position, newQuantity, newExpectedYield, newCurrentPrice);

        final Position expectedPosition = new PositionBuilder()
                .setCurrency(share.getCurrency())
                .setFigi(position.getFigi())
                .setInstrumentType(position.getInstrumentType())
                .setQuantity(newQuantity)
                .setAveragePositionPrice(position.getAveragePositionPrice())
                .setExpectedYield(newExpectedYield)
                .setCurrentNkd(position.getCurrentNkd())
                .setCurrentPrice(newCurrentPrice)
                .setAveragePositionPriceFifo(position.getAveragePositionPriceFifo())
                .build();

        AssertUtils.assertEquals(expectedPosition, newPosition);
    }

}