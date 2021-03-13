package ru.obukhov.trader.bot.impl;

import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.config.TradingProperties;

/**
 * Decider which decides to buy paper always when possible and never to sell
 */
@Slf4j
public class ConservativeDecider extends AbstractDecider {

    public ConservativeDecider(TradingProperties tradingProperties) {
        super(tradingProperties);
    }

    @Override
    public Decision decide(DecisionData data) {
        Decision decision;
        if (existsOperationInProgress(data)) {
            decision = Decision.WAIT_DECISION;
            log.debug("Exists operation in progress. Decision is {}", decision.toPrettyString());
        } else {
            int availableLots = getAvailableLots(data);
            if (availableLots > 0) {
                decision = new Decision(DecisionAction.BUY, availableLots);
                log.debug("Current balance {} allows to buy {} lots. Decision is {}",
                        data.getBalance(), availableLots, decision.toPrettyString());
            } else {
                decision = Decision.WAIT_DECISION;
                log.debug("Current balance {} is not enough to buy any lots. Decision is {}",
                        data.getBalance(), decision.toPrettyString());
            }
        }

        return decision;
    }

}