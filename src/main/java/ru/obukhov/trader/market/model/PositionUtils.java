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
        final BigDecimal averagePositionPrice = position.getAveragePositionPrice().getValue();
        final BigDecimal quantity = position.getQuantity();
        return averagePositionPrice.multiply(quantity);
    }

    /**
     * @param additionalQuantity     increase of quantity
     * @param additionalTotalPrice   increase of total price. Affects averagePositionPriceValue and expectedYield
     * @param newCurrentPrice        new price of position
     * @return new PortfolioPosition with additional quantity
     */
    public Position addQuantities(
            final Position position,
            final long additionalQuantity,
            final BigDecimal additionalTotalPrice,
            final BigDecimal newCurrentPrice
    ) {
        final BigDecimal newQuantity = position.getQuantity().add(BigDecimal.valueOf(additionalQuantity));
        final BigDecimal newTotalPrice = getTotalPrice(position).add(additionalTotalPrice);
        final BigDecimal newAveragePositionPriceValue = DecimalUtils.divide(newTotalPrice, newQuantity);
        final BigDecimal newExpectedYield = DecimalUtils.multiply(newCurrentPrice.subtract(newAveragePositionPriceValue), newQuantity);
        return new PositionBuilder()
                .setCurrency(getCurrency(position))
                .setFigi(position.getFigi())
                .setInstrumentType(position.getInstrumentType())
                .setQuantity(newQuantity)
                .setAveragePositionPrice(newAveragePositionPriceValue)
                .setExpectedYield(newExpectedYield)
                .setCurrentNkd(position.getCurrentNkd())
                .setCurrentPrice(newCurrentPrice)
                .setAveragePositionPriceFifo(position.getAveragePositionPriceFifo())
                .build();
    }

    /**
     * @return equal position, but with updated quantity and currentPrice
     */
    public Position cloneWithNewValues(
            final Position position,
            final BigDecimal quantity,
            final BigDecimal expectedYield,
            final BigDecimal currentPrice
    ) {
        return new PositionBuilder()
                .setCurrency(getCurrency(position))
                .setFigi(position.getFigi())
                .setInstrumentType(position.getInstrumentType())
                .setQuantity(quantity)
                .setAveragePositionPrice(position.getAveragePositionPrice())
                .setExpectedYield(expectedYield)
                .setCurrentNkd(position.getCurrentNkd())
                .setCurrentPrice(currentPrice)
                .setAveragePositionPriceFifo(position.getAveragePositionPriceFifo())
                .build();
    }

    /**
     * @return equal position, but with updated currentPrice
     */
    public Position cloneWithNewCurrentPrice(final Position position, final BigDecimal currentPrice) {
        return new PositionBuilder()
                .setCurrency(getCurrency(position))
                .setFigi(position.getFigi())
                .setInstrumentType(position.getInstrumentType())
                .setQuantity(position.getQuantity())
                .setAveragePositionPrice(position.getAveragePositionPrice())
                .setExpectedYield(position.getExpectedYield())
                .setCurrentNkd(position.getCurrentNkd())
                .setCurrentPrice(currentPrice)
                .setAveragePositionPriceFifo(position.getAveragePositionPriceFifo())
                .build();
    }

}