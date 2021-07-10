package ru.obukhov.trader.trading.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.trading.strategy.model.TradingStrategyParams;

/**
 * Strategy which decides to buy paper always when possible and never to sell
 */
@Slf4j
public class ConservativeStrategy extends AbstractTradingStrategy {

    public ConservativeStrategy(final String name, final TradingStrategyParams params, final TradingProperties tradingProperties) {
        super(name, params, tradingProperties);
    }

    @Override
    public Decision decide(@NotNull final DecisionData data, @NotNull final StrategyCache strategyCache) {
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