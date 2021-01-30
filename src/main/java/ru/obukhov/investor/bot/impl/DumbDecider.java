package ru.obukhov.investor.bot.impl;

import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.investor.bot.model.Decision;
import ru.obukhov.investor.bot.model.DecisionAction;
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
            return Decision.WAIT_DECISION;
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
                return Decision.WAIT_DECISION;
            } else {
                Decision decision = new Decision(DecisionAction.BUY, 1);
                log.debug("No position. Decision is {}", decision);
                return decision;
            }
        }

        BigDecimal profit = getProfit(position.averagePositionPrice.value, data.getInstrument().lot, currentPrice);

        Decision decision;
        if (MathUtils.isGreater(profit, MINIMUM_PROFIT)) {
            decision = new Decision(DecisionAction.SELL, 1);
            log.debug("Potential profit {} is greater than minimum profit {}. Decision is {}",
                    profit, MINIMUM_PROFIT, decision);
        } else {
            decision = Decision.WAIT_DECISION;
            log.debug("Potential profit {} is not greater than minimum profit {}. Decision is Wait",
                    profit, MINIMUM_PROFIT);
        }
        return decision;
    }

}