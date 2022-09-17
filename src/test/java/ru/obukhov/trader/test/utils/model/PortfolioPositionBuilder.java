package ru.obukhov.trader.test.utils.model;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.PortfolioPosition;

public class PortfolioPositionBuilder {
    private String ticker;
    private InstrumentType instrumentType;
    private double averagePositionPrice = 0;
    private double expectedYield = 0;
    private double currentPrice;
    private double quantityLots = 0;
    private Currency currency = Currency.RUB;
    private int lotSize = 1;

    public PortfolioPositionBuilder setTicker(final String ticker) {
        this.ticker = ticker;
        return this;
    }

    public PortfolioPositionBuilder setInstrumentType(final InstrumentType instrumentType) {
        this.instrumentType = instrumentType;
        return this;
    }

    public PortfolioPositionBuilder setAveragePositionPrice(final double averagePositionPrice) {
        this.averagePositionPrice = averagePositionPrice;
        return this;
    }

    public PortfolioPositionBuilder setExpectedYield(final double expectedYield) {
        this.expectedYield = expectedYield;
        return this;
    }

    public PortfolioPositionBuilder setCurrentPrice(final double currentPrice) {
        this.currentPrice = currentPrice;
        return this;
    }

    public PortfolioPositionBuilder setQuantityLots(final double quantityLots) {
        this.quantityLots = quantityLots;
        return this;
    }

    public PortfolioPositionBuilder setCurrency(final Currency currency) {
        this.currency = currency;
        return this;
    }

    public PortfolioPositionBuilder setLotSize(final int lotSize) {
        this.lotSize = lotSize;
        return this;
    }

    public PortfolioPosition build() {
        return new PortfolioPosition(
                ticker,
                instrumentType,
                TestData.createIntegerDecimal(quantityLots * lotSize),
                TestData.createMoney(currency, averagePositionPrice),
                DecimalUtils.setDefaultScale(expectedYield),
                TestData.createMoney(currency, currentPrice),
                TestData.createIntegerDecimal(quantityLots)
        );
    }

}
