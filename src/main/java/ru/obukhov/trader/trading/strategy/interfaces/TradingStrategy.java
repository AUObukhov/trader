package ru.obukhov.trader.trading.strategy.interfaces;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.web.model.BotConfig;

public interface TradingStrategy {

    String getName();

    /**
     * @param data          data about current market and portfolio situation
     * @param strategyCache data, calculated on previous calls of the method. Can be updated by the method.
     * @return decision about current action
     */
    Decision decide(@NotNull final DecisionData data, final long availableLots, @NotNull final StrategyCache strategyCache);

    StrategyCache initCache(final BotConfig botConfig, final Interval interval);

}