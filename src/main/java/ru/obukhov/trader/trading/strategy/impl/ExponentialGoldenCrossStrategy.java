package ru.obukhov.trader.trading.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.trading.strategy.model.Averages;
import ru.obukhov.trader.trading.strategy.model.ExponentialGoldenCrossStrategyParams;

import java.math.BigDecimal;
import java.util.List;

/**
 * Golden Cross strategy based on exponentially weighted moving average algorithm
 */
@Slf4j
public class ExponentialGoldenCrossStrategy extends AbstractGoldenCrossStrategy {

    /**
     * Initializes new instance of {@link ExponentialGoldenCrossStrategy}
     *
     * @param tradingProperties common trading properties
     * @param params            params of strategy
     */
    public ExponentialGoldenCrossStrategy(
            final ExponentialGoldenCrossStrategyParams params,
            final TradingProperties tradingProperties
    ) {
        super("exponentialGoldenCross", params, tradingProperties);
    }

    @Override
    protected Averages getAverages(final List<BigDecimal> values) {
        final ExponentialGoldenCrossStrategyParams exponentialGoldenCrossStrategyParams =
                (ExponentialGoldenCrossStrategyParams) params;
        final List<BigDecimal> shortAverages = TrendUtils.getExponentialWeightedMovingAverages(
                values,
                exponentialGoldenCrossStrategyParams.getFastWeightDecrease()
        );
        final List<BigDecimal> longAverages = TrendUtils.getExponentialWeightedMovingAverages(
                values,
                exponentialGoldenCrossStrategyParams.getSlowWeightDecrease()
        );

        return new Averages(shortAverages, longAverages);
    }

    @NotNull
    @Override
    public StrategyCache initCache() {
        return new LinearGoldenCrossStrategyCache();
    }

    private static class LinearGoldenCrossStrategyCache extends AbstractGoldenCrossStrategyCache {
    }

}