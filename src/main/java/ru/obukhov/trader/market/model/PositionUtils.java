package ru.obukhov.trader.market.model;

import lombok.experimental.UtilityClass;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.tinkoff.piapi.core.models.Money;
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
        return cloneWithNewValues(position, newQuantity, newAveragePositionPriceValue, newExpectedYield, newCurrentPrice, newQuantityLots);
    }

    private Position cloneWithNewValues(
            final Position position,
            final BigDecimal quantity,
            final BigDecimal averagePositionPriceValue,
            final BigDecimal newExpectedYield,
            final BigDecimal newCurrentPriceValue,
            final BigDecimal quantityLots
    ) {
        final String currency = getCurrency(position);
        final Money newAveragePositionPrice = Money.builder()
                .currency(currency)
                .value(averagePositionPriceValue)
                .build();
        final Money newCurrentPrice = Money.builder()
                .currency(currency)
                .value(newCurrentPriceValue)
                .build();
        return Position.builder()
                .figi(position.getFigi())
                .instrumentType(position.getInstrumentType())
                .quantity(quantity)
                .averagePositionPrice(newAveragePositionPrice)
                .expectedYield(newExpectedYield)
                .currentPrice(newCurrentPrice)
                .quantityLots(quantityLots)
                .build();
    }

    /**
     * @return equal position, but with updated quantity, currentPrice and quantityLots
     */
    public Position cloneWithNewValues(
            final Position position,
            final BigDecimal quantity,
            final BigDecimal newExpectedYield,
            final BigDecimal currentPrice,
            final BigDecimal quantityLots
    ) {
        final Money newCurrentPrice = Money.builder()
                .currency(getCurrency(position))
                .value(currentPrice)
                .build();
        return Position.builder()
                .figi(position.getFigi())
                .instrumentType(position.getInstrumentType())
                .quantity(quantity)
                .averagePositionPrice(position.getAveragePositionPrice())
                .expectedYield(newExpectedYield)
                .currentPrice(newCurrentPrice)
                .quantityLots(quantityLots)
                .build();
    }

    /**
     * @return equal position, but with updated quantity and quantityLots
     */
    public Position cloneWithNewQuantity(final Position position, final BigDecimal quantity, final BigDecimal quantityLots) {
        return Position.builder()
                .figi(position.getFigi())
                .instrumentType(position.getInstrumentType())
                .quantity(quantity)
                .averagePositionPrice(position.getAveragePositionPrice())
                .expectedYield(position.getExpectedYield())
                .currentPrice(position.getCurrentPrice())
                .quantityLots(quantityLots)
                .build();
    }

}