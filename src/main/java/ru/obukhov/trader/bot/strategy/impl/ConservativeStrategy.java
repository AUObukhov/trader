package ru.obukhov.trader.bot.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.bot.strategy.AbstractStrategy;
import ru.obukhov.trader.config.TradingProperties;

/**
 * Strategy which decides to buy paper always when possible and never to sell
 */
@Slf4j
public class ConservativeStrategy extends AbstractStrategy {

    public ConservativeStrategy(TradingProperties tradingProperties) {
        super(tradingProperties);
    }

    @Override
    public Decision decide(DecisionData data) {
        Decision decision;
        if (existsOperationInProgress(data)) {
            decision = Decision.WAIT_DECISION;
            log.debug("Exists operation in progress. Decision is {}", decision.toPrettyString());
        } else {
            decision = getBuyOrWaitDecision(data);
        }

        return decision;
    }

}