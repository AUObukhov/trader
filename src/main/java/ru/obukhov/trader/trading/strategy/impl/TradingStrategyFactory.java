package ru.obukhov.trader.trading.strategy.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.trading.model.StrategyConfig;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;

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
                final boolean greedy = (boolean) params.get("greedy");
                return new GoldenCrossStrategy(
                        strategyConfig.getMinimumProfit(),
                        tradingProperties,
                        smallWindow,
                        bigWindow,
                        indexCoefficient,
                        greedy
                );
            }
            default:
                throw new IllegalArgumentException("Unknown strategy type " + strategyConfig.getType());
        }
    }
}