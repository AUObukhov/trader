package ru.obukhov.trader.bot.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.bot.model.StrategyConfig;
import ru.obukhov.trader.bot.strategy.TradingStrategy;
import ru.obukhov.trader.bot.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.bot.strategy.impl.GoldenCrossStrategy;
import ru.obukhov.trader.bot.strategy.impl.GreedyGoldenCrossStrategy;
import ru.obukhov.trader.config.TradingProperties;

import java.util.Map;

@Service
@AllArgsConstructor
public class TradingStrategyFactory {
    private final TradingProperties tradingProperties;

    public TradingStrategy createStrategy(final StrategyConfig strategyConfig) {
        switch (strategyConfig.getType()) {
            case CONSERVATIVE:
                return new ConservativeStrategy(strategyConfig.getMinimumProfit(), tradingProperties);
            case GOLDEN_CROSS: {
                final Map<String, Object> params = strategyConfig.getParams();
                final int smallWindow = (int) params.get("smallWindow");
                final int bigWindow = (int) params.get("bigWindow");
                final float indexCoefficient = ((Double) params.get("indexCoefficient")).floatValue();
                return new GoldenCrossStrategy(
                        strategyConfig.getMinimumProfit(),
                        tradingProperties,
                        smallWindow,
                        bigWindow,
                        indexCoefficient
                );
            }
            case GREEDY_GOLDEN_CROSS: {
                final Map<String, Object> params = strategyConfig.getParams();
                final int smallWindow = (int) params.get("smallWindow");
                final int bigWindow = (int) params.get("bigWindow");
                final float indexCoefficient = ((Double) params.get("indexCoefficient")).floatValue();
                return new GreedyGoldenCrossStrategy(
                        strategyConfig.getMinimumProfit(),
                        tradingProperties,
                        smallWindow,
                        bigWindow,
                        indexCoefficient
                );
            }
            default:
                throw new IllegalArgumentException("Unknown strategy type " + strategyConfig.getType());
        }
    }
}