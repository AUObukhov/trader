package ru.obukhov.trader.bot.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.bot.strategy.AbstractTradingStrategy;
import ru.obukhov.trader.bot.strategy.StrategyCache;
import ru.obukhov.trader.config.TradingProperties;

/**
 * Strategy which decides to buy paper always when possible and never to sell
 */
@Slf4j
public class ConservativeStrategy extends AbstractTradingStrategy {

    public ConservativeStrategy(final TradingProperties tradingProperties) {
        super("Conservative", tradingProperties);
    }

    @Override
    public Decision decide(final DecisionData data, final StrategyCache strategyCache) {
        Decision decision;
        if (existsOperationInProgress(data)) {
            decision = new Decision(DecisionAction.WAIT, null, strategyCache);
            log.debug("Exists operation in progress. Decision is {}", decision.toPrettyString());
        } else {
            decision = getBuyOrWaitDecision(data, strategyCache);
        }

        return decision;
    }

    @NotNull
    @Override
    public StrategyCache initCache() {
        return new ConservativeStrategyCache();
    }

    /**
     *
     */
    private static class ConservativeStrategyCache implements StrategyCache {
    }

}