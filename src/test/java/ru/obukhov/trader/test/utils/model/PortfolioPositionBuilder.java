package ru.obukhov.trader.test.utils.model;

import lombok.Setter;
import lombok.experimental.Accessors;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.PortfolioPosition;

@Setter
@Accessors(chain = true)
public class PortfolioPositionBuilder {
    private String figi;
    private String ticker;
    private InstrumentType instrumentType;
    private double averagePositionPrice = 0;
    private double expectedYield = 0;
    private double currentPrice;
    private double quantityLots = 0;
    private Currency currency = Currency.RUB;
    private int lotSize = 1;

    public PortfolioPosition build() {
        return new PortfolioPosition(
                figi,
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
