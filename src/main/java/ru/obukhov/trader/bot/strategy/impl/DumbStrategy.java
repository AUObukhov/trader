package ru.obukhov.trader.bot.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.bot.strategy.AbstractStrategy;
import ru.obukhov.trader.config.TradingProperties;

/**
 * Strategy which decides to buy paper when possible and sell when {@link AbstractStrategy#MINIMUM_PROFIT} achieved
 */
@Slf4j
public class DumbStrategy extends AbstractStrategy {

    public DumbStrategy(TradingProperties tradingProperties) {
        super("Dumb", tradingProperties);
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

}