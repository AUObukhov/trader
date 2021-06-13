package ru.obukhov.trader.trading.strategy.interfaces;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionData;

public interface TradingStrategy {

    String getName();

    Decision decide(final DecisionData data, final StrategyCache strategyCache);

    @NotNull
    StrategyCache initCache();

}