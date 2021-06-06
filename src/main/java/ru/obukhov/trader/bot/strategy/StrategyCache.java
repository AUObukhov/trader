package ru.obukhov.trader.bot.strategy;

import ru.obukhov.trader.bot.model.DecisionData;

/**
 * Data, which can be passed between different calls of {@link TradingStrategy#decide(DecisionData, StrategyCache)}
 * to minimize recalculations
 */
public interface StrategyCache {
}