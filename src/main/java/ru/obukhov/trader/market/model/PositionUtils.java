package ru.obukhov.trader.market.model;

import lombok.experimental.UtilityClass;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;

@UtilityClass
public class PositionUtils {

    public static String getCurrency(final Position position) {
        return position.getAveragePositionPrice().getCurrency();
    }

    public static BigDecimal getTotalPrice(final Position position) {
        return position.getAveragePositionPrice().getValue().multiply(position.getQuantity());
    }

    /**
     * @param additionalQuantity     increase of quantity
     * @param additionalQuantityLots increase of quantityLots
     * @param additionalTotalPrice   increase of total price. Affects averagePositionPriceValue and expectedYield
     * @param newCurrentPrice        new price of position
     * @return new PortfolioPosition with additional quantity
     */
    public Position addQuantities(
            final Position position,
            final long additionalQuantity,
            final long additionalQuantityLots,
            final BigDecimal additionalTotalPrice,
            final BigDecimal newCurrentPrice
    ) {
        final BigDecimal newQuantity = DecimalUtils.add(position.getQuantity(), additionalQuantity);
        final BigDecimal newTotalPrice = getTotalPrice(position).add(additionalTotalPrice);
        final BigDecimal newAveragePositionPriceValue = DecimalUtils.divide(newTotalPrice, newQuantity);
        final BigDecimal newExpectedYield = newCurrentPrice.subtract(newAveragePositionPriceValue).multiply(newQuantity);
        final BigDecimal newQuantityLots = DecimalUtils.add(position.getQuantityLots(), additionalQuantityLots);
        return new PositionBuilder()
                .setCurrency(getCurrency(position))
                .setFigi(position.getFigi())
                .setInstrumentType(position.getInstrumentType())
                .setQuantity(newQuantity)
                .setAveragePositionPrice(newAveragePositionPriceValue)
                .setExpectedYield(newExpectedYield)
                .setCurrentNkd(position.getCurrentNkd())
                .setAveragePositionPricePt(position.getAveragePositionPricePt())
                .setCurrentPrice(newCurrentPrice)
                .setAveragePositionPriceFifo(position.getAveragePositionPriceFifo())
                .setQuantityLots(newQuantityLots)
                .build();
    }

    /**
     * @return equal position, but with updated quantity, currentPrice and quantityLots
     */
    public Position cloneWithNewValues(
            final Position position,
            final BigDecimal quantity,
            final BigDecimal expectedYield,
            final BigDecimal currentPrice,
            final BigDecimal quantityLots
    ) {
        return new PositionBuilder()
                .setCurrency(getCurrency(position))
                .setFigi(position.getFigi())
                .setInstrumentType(position.getInstrumentType())
                .setQuantity(quantity)
                .setAveragePositionPrice(position.getAveragePositionPrice())
                .setExpectedYield(expectedYield)
                .setCurrentNkd(position.getCurrentNkd())
                .setAveragePositionPricePt(position.getAveragePositionPricePt())
                .setCurrentPrice(currentPrice)
                .setAveragePositionPriceFifo(position.getAveragePositionPriceFifo())
                .setQuantityLots(quantityLots)
                .build();
    }

    /**
     * @return equal position, but with updated quantity and quantityLots
     */
    public Position cloneWithNewQuantity(final Position position, final BigDecimal quantity, final BigDecimal quantityLots) {
        return new PositionBuilder()
                .setCurrency(getCurrency(position))
                .setFigi(position.getFigi())
                .setInstrumentType(position.getInstrumentType())
                .setQuantity(quantity)
                .setAveragePositionPrice(position.getAveragePositionPrice())
                .setExpectedYield(position.getExpectedYield())
                .setCurrentNkd(position.getCurrentNkd())
                .setAveragePositionPricePt(position.getAveragePositionPricePt())
                .setCurrentPrice(position.getCurrentPrice())
                .setAveragePositionPriceFifo(position.getAveragePositionPriceFifo())
                .setQuantityLots(quantityLots)
                .build();
    }

}