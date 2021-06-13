package ru.obukhov.trader.trading.strategy.interfaces;

import ru.obukhov.trader.trading.model.DecisionData;

/**
 * Data, which can be passed between different calls of {@link TradingStrategy#decide(DecisionData, StrategyCache)}
 * to minimize recalculations
 */
public interface StrategyCache {
}