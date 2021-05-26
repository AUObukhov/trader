package ru.obukhov.trader.bot.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.bot.model.StrategyConfig;
import ru.obukhov.trader.bot.strategy.TradingStrategy;
import ru.obukhov.trader.bot.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.bot.strategy.impl.GoldenCrossStrategy;
import ru.obukhov.trader.config.TradingProperties;

import java.util.Map;

@Service
@AllArgsConstructor
public class TradingStrategyFactory {
    private final TradingProperties tradingProperties;

    public TradingStrategy createStrategy(StrategyConfig strategyConfig) {
        switch (strategyConfig.getType()) {
            case CONSERVATIVE:
                return new ConservativeStrategy(tradingProperties);
            case GOLDEN_CROSS:
                final Map<String, Object> params = strategyConfig.getParams();
                int smallWindow = (int) params.get("smallWindow");
                int bigWindow = (int) params.get("bigWindow");
                float indexCoefficient = ((Double) params.get("indexCoefficient")).floatValue();
                return new GoldenCrossStrategy(tradingProperties, smallWindow, bigWindow, indexCoefficient);
            default:
                throw new IllegalArgumentException("Unknown strategy type " + strategyConfig.getType());
        }
    }
}