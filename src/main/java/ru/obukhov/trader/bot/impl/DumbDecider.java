package ru.obukhov.trader.bot.impl;

import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.config.TradingProperties;

/**
 * Decider which decides to buy paper when possible and sell when {@link AbstractDecider#MINIMUM_PROFIT} achieved
 */
@Slf4j
public class DumbDecider extends AbstractDecider {

    public DumbDecider(TradingProperties tradingProperties) {
        super(tradingProperties);
    }

    @Override
    public Decision decide(DecisionData data) {
        Decision decision;
        if (existsOperationInProgress(data)) {
            decision = Decision.WAIT_DECISION;
            log.debug("Exists operation in progress. Decision is {}", decision.toPrettyString());
        } else {
            if (data.getPosition() == null) {
                decision = getBuyOrWaitDecision(data);
            } else {
                decision = getSellOrWaitDecision(data);
                if (decision.getAction() == DecisionAction.WAIT) {
                    decision = getBuyOrWaitDecision(data);
                }
            }
        }

        return decision;
    }

    private Decision getBuyOrWaitDecision(DecisionData data) {
        Decision decision;
        int availableLots = getAvailableLots(data);
        if (availableLots > 0) {
            decision = new Decision(DecisionAction.BUY, availableLots);
            log.debug("No position and current balance {} allows to buy {} lots. Decision is {}",
                    data.getBalance(), availableLots, decision.toPrettyString());

        } else {
            decision = Decision.WAIT_DECISION;
            log.debug("No position and current balance {} is not enough to buy any lots. Decision is {}",
                    data.getBalance(), decision.toPrettyString());
        }
        return decision;
    }

    private Decision getSellOrWaitDecision(DecisionData data) {
        double profit = getProfit(data);

        Decision decision;
        if (profit < MINIMUM_PROFIT) {
            decision = Decision.WAIT_DECISION;
            log.debug("Potential profit {} is lower than minimum profit {}. Decision is {}",
                    profit, MINIMUM_PROFIT, decision.toPrettyString());
        } else {
            decision = new Decision(DecisionAction.SELL, data.getPositionLotsCount());
            log.debug("Potential profit {} is greater than minimum profit {}. Decision is {}",
                    profit, MINIMUM_PROFIT, decision.toPrettyString());
        }

        return decision;
    }

}