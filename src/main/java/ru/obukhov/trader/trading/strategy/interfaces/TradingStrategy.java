package ru.obukhov.trader.trading.strategy.interfaces;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionsData;
import ru.obukhov.trader.web.model.BotConfig;

import java.util.Map;

public interface TradingStrategy {

    String getName();

    /**
     * @param data          data about current market and portfolio situation
     * @param strategyCache data, calculated on previous calls of the method. Can be updated by the method.
     * @return decision about current action
     */
    Map<String, Decision> decide(@NotNull final DecisionsData data, @NotNull final StrategyCache strategyCache);

    StrategyCache initCache(final BotConfig botConfig, final Interval interval);

}