package ru.obukhov.trader.market.model;

import ru.obukhov.trader.common.util.DecimalUtils;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Convenient replacement to {@link ru.tinkoff.piapi.core.models.Position}
 */
public record PortfolioPosition(
        @NotNull String ticker,
        @NotNull InstrumentType instrumentType,
        @NotNull BigDecimal quantity,
        @NotNull MoneyAmount averagePositionPrice,
        @NotNull BigDecimal expectedYield,
        @NotNull MoneyAmount currentPrice,
        @NotNull BigDecimal quantityLots
) {

    public Currency getCurrency() {
        return averagePositionPrice.currency();
    }

    public BigDecimal getTotalPrice() {
        return averagePositionPrice.value().multiply(quantity);
    }

    /**
     * @param additionalQuantity     increase of quantity
     * @param additionalQuantityLots increase of quantityLots
     * @param additionalTotalPrice   increase of total price. Affects averagePositionPriceValue and expectedYield
     * @param newCurrentPrice        new price of position
     * @return new PortfolioPosition with additional quantity
     */
    public PortfolioPosition addQuantities(
            final long additionalQuantity,
            final long additionalQuantityLots,
            final BigDecimal additionalTotalPrice,
            final BigDecimal newCurrentPrice
    ) {
        final BigDecimal newQuantity = DecimalUtils.add(quantity, additionalQuantity);
        final BigDecimal newTotalPrice = getTotalPrice().add(additionalTotalPrice);
        final BigDecimal newAveragePositionPriceValue = DecimalUtils.divide(newTotalPrice, newQuantity);
        final BigDecimal newExpectedYield = newCurrentPrice.subtract(newAveragePositionPriceValue).multiply(newQuantity);
        final BigDecimal newQuantityLots = DecimalUtils.add(quantityLots, additionalQuantityLots);
        return cloneWithNewValues(newQuantity, newAveragePositionPriceValue, newExpectedYield, newCurrentPrice, newQuantityLots);
    }

    private PortfolioPosition cloneWithNewValues(
            final BigDecimal quantity,
            final BigDecimal averagePositionPriceValue,
            final BigDecimal newExpectedYield,
            final BigDecimal newCurrentPriceValue,
            final BigDecimal quantityLots
    ) {
        final MoneyAmount newAveragePositionPrice = new MoneyAmount(getCurrency(), averagePositionPriceValue);
        final MoneyAmount newCurrentPrice = new MoneyAmount(getCurrency(), newCurrentPriceValue);
        return new PortfolioPosition(ticker, instrumentType, quantity, newAveragePositionPrice, newExpectedYield, newCurrentPrice, quantityLots);
    }

    /**
     * @return equal position, but with updated quantity, currentPrice and quantityLots
     */
    public PortfolioPosition cloneWithNewValues(
            final BigDecimal quantity,
            final BigDecimal newExpectedYield,
            final BigDecimal currentPrice,
            final BigDecimal quantityLots
    ) {
        final MoneyAmount newCurrentPrice = new MoneyAmount(getCurrency(), currentPrice);
        return new PortfolioPosition(
                ticker,
                instrumentType,
                quantity,
                averagePositionPrice,
                newExpectedYield,
                newCurrentPrice,
                quantityLots
        );
    }

    /**
     * @return equal position, but with updated quantity and quantityLots
     */
    public PortfolioPosition cloneWithNewQuantity(final BigDecimal quantity, final BigDecimal quantityLots) {
        return new PortfolioPosition(
                ticker,
                instrumentType,
                quantity,
                averagePositionPrice,
                expectedYield,
                currentPrice,
                quantityLots
        );
    }

}