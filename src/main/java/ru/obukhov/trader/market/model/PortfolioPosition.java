package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.obukhov.trader.common.model.transform.BigDecimalSerializer;
import ru.obukhov.trader.common.util.DecimalUtils;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Convenient replacement to {@link ru.tinkoff.piapi.core.models.Position}
 */
public record PortfolioPosition(
        @NotNull String ticker,
        @NotNull InstrumentType instrumentType,
        @NotNull @JsonSerialize(using = BigDecimalSerializer.class) BigDecimal quantity,
        @NotNull MoneyAmount averagePositionPrice,
        @NotNull @JsonSerialize(using = BigDecimalSerializer.class) BigDecimal expectedYield,
        @NotNull MoneyAmount currentPrice,
        @NotNull @JsonSerialize(using = BigDecimalSerializer.class) BigDecimal quantityLots
) {

    public String getCurrency() {
        return averagePositionPrice.currency();
    }

    public BigDecimal getTotalPrice() {
        return averagePositionPrice.value().multiply(quantity);
    }

    /**
     * @param additionalQuantity     increase of quantity
     * @param additionalQuantityLots increase of quantityLots
     * @param additionalTotalPrice   increase of total price. Affects averagePositionPriceValue and expectedYield
     * @return new PortfolioPosition with additional quantity
     */
    public PortfolioPosition addQuantities(
            final long additionalQuantity,
            final long additionalQuantityLots,
            final BigDecimal additionalTotalPrice
    ) {
        final BigDecimal newQuantity = DecimalUtils.add(quantity, additionalQuantity);
        final BigDecimal newTotalPrice = getTotalPrice().add(additionalTotalPrice);
        final BigDecimal newAveragePositionPriceValue = DecimalUtils.divide(newTotalPrice, newQuantity);
        final BigDecimal newExpectedYield = currentPrice.value().multiply(newQuantity).subtract(newTotalPrice);
        final BigDecimal newQuantityLots = DecimalUtils.add(quantityLots, additionalQuantityLots);
        return clonePositionWithNewValues(newQuantity, newAveragePositionPriceValue, newExpectedYield, newQuantityLots);
    }

    private PortfolioPosition clonePositionWithNewValues(
            final BigDecimal quantity,
            final BigDecimal averagePositionPriceValue,
            final BigDecimal newExpectedYield,
            final BigDecimal quantityLots
    ) {
        final MoneyAmount averagePositionPrice = new MoneyAmount(getCurrency(), averagePositionPriceValue);
        return new PortfolioPosition(ticker, instrumentType, quantity, averagePositionPrice, newExpectedYield, currentPrice, quantityLots);
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