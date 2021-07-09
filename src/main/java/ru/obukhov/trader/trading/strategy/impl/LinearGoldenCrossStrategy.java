package ru.obukhov.trader.trading.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.trading.strategy.model.Averages;
import ru.obukhov.trader.trading.strategy.model.GoldenCrossStrategyParams;

import java.math.BigDecimal;
import java.util.List;

/**
 * Golden Cross strategy based on linear weighted moving average algorithm
 */
@Slf4j
public class LinearGoldenCrossStrategy extends AbstractGoldenCrossStrategy {

    /**
     * Initializes new instance of {@link LinearGoldenCrossStrategy}
     *
     * @param params            params of strategy
     * @param tradingProperties common trading properties
     */
    public LinearGoldenCrossStrategy(
            final GoldenCrossStrategyParams params,
            final TradingProperties tradingProperties
    ) {
        super("linearGoldenCross", params, tradingProperties);
    }

    @Override
    protected Averages getAverages(final List<BigDecimal> values) {
        final GoldenCrossStrategyParams goldenCrossStrategyParams = (GoldenCrossStrategyParams) params;
        final List<BigDecimal> shortAverages = TrendUtils.getLinearWeightedMovingAverages(
                values,
                goldenCrossStrategyParams.getSmallWindow()
        );
        final List<BigDecimal> longAverages = TrendUtils.getLinearWeightedMovingAverages(
                values,
                goldenCrossStrategyParams.getBigWindow()
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