package ru.obukhov.trader.market.model;

import lombok.experimental.UtilityClass;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class PositionUtils {

    public static String getCurrency(final Position position) {
        return position.getAveragePositionPrice().getCurrency();
    }

    public static Quotation getTotalPrice(final Position position) {
        final BigDecimal averagePositionPrice = position.getAveragePositionPrice().getValue();
        final BigDecimal quantity = position.getQuantity();
        return QuotationUtils.newQuotation(averagePositionPrice.multiply(quantity));
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
            final Quotation additionalTotalPrice,
            final Quotation newCurrentPrice
    ) {
        final Quotation newQuantity = QuotationUtils.add(QuotationUtils.newQuotation(position.getQuantity()), additionalQuantity);
        final Quotation newTotalPrice = QuotationUtils.add(getTotalPrice(position), additionalTotalPrice);
        final Quotation newAveragePositionPriceValue = QuotationUtils.divide(newTotalPrice, newQuantity, RoundingMode.HALF_UP);
        final Quotation newExpectedYield = QuotationUtils.multiply(QuotationUtils.subtract(newCurrentPrice, newAveragePositionPriceValue), newQuantity);
        final BigDecimal newQuantityLots = DecimalUtils.add(position.getQuantityLots(), additionalQuantityLots);
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
                .setCurrentPrice(currentPrice)
                .setAveragePositionPriceFifo(position.getAveragePositionPriceFifo())
                .setQuantityLots(quantityLots)
                .build();
    }

    /**
     * @return equal position, but with updated quantity, currentPrice and quantityLots
     */
    public Position cloneWithNewValues(
            final Position position,
            final BigDecimal quantity,
            final Quotation expectedYield,
            final Quotation currentPrice,
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
                .setCurrentPrice(currentPrice)
                .setAveragePositionPriceFifo(position.getAveragePositionPriceFifo())
                .setQuantityLots(quantityLots)
                .build();
    }

    /**
     * @return equal position, but with updated currentPrice
     */
    public Position cloneWithNewCurrentPrice(final Position position, final Quotation currentPrice) {
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
                .setQuantityLots(position.getQuantityLots())
                .build();
    }

}