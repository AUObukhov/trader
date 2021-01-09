package ru.obukhov.investor.bot.impl;

import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.investor.Decision;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.util.MathUtils;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;

/**
 * Decider which decider to by paper  as soon as possible and sell when {@link AbstractDecider#MINIMUM_PROFIT} achieved
 */
@Slf4j
public class DumbDecider extends AbstractDecider {

    public DumbDecider(TradingProperties tradingProperties) {
        super(tradingProperties);
    }

    @Override
    public Decision decide(DecisionData data) {
        if (existsOperationInProgress(data)) {
            log.debug("Exists operation in progress. Decision is Wait");
            return Decision.WAIT;
        }

        final Portfolio.PortfolioPosition position = data.getPosition();
        final BigDecimal currentPrice = Iterables.getLast(data.getCurrentCandles()).getClosePrice();
        final BigDecimal currentPriceWithCommission =
                MathUtils.addFraction(currentPrice, tradingProperties.getCommission());
        if (position == null) {
            if (MathUtils.isGreater(currentPriceWithCommission, data.getBalance())) {
                log.debug("No position, but current price with commission = {} is greater than balance {}." +
                                " Decision is Wait",
                        currentPriceWithCommission, data.getBalance());
                return Decision.WAIT;
            } else {
                log.debug("No position. Decision is Buy");
                return Decision.BUY;
            }
        }

        BigDecimal profit = getProfit(position.averagePositionPrice.value, data.getInstrument().lot, currentPrice);

        return MathUtils.isGreater(profit, MINIMUM_PROFIT) ? Decision.SELL : Decision.WAIT;
    }

}