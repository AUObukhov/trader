package ru.obukhov.trader.trading.strategy.interfaces;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionData;

public interface TradingStrategy {

    String getName();

    /**
     * @param data          data about current market and portfolio situation
     * @param strategyCache data, calculated on previous calls of the method. Can be updated by the method.
     * @return decision about current action
     */
    Decision decide(@NotNull final DecisionData data, @NotNull final StrategyCache strategyCache);

    @NotNull
    StrategyCache initCache();

}