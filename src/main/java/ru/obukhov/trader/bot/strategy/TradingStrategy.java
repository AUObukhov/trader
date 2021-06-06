package ru.obukhov.trader.bot.strategy;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionData;

public interface TradingStrategy {

    String getName();

    Decision decide(final DecisionData data, final StrategyCache strategyCache);

    @NotNull
    StrategyCache initCache();

}