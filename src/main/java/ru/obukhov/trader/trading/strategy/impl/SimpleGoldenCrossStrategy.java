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
 * Golden Cross strategy based on simple moving average algorithm
 */
@Slf4j
public class SimpleGoldenCrossStrategy extends AbstractGoldenCrossStrategy {

    /**
     * Initializes new instance of {@link SimpleGoldenCrossStrategy}
     *
     * @param params            params of strategy
     * @param tradingProperties common trading properties
     */
    public SimpleGoldenCrossStrategy(
            final GoldenCrossStrategyParams params,
            final TradingProperties tradingProperties
    ) {
        super("simpleGoldenCross", params, tradingProperties);
    }

    @Override
    protected Averages getAverages(final List<BigDecimal> values) {
        final GoldenCrossStrategyParams goldenCrossStrategyParams = (GoldenCrossStrategyParams) params;
        final List<BigDecimal> shortAverages = TrendUtils.getSimpleMovingAverages(
                values,
                goldenCrossStrategyParams.getSmallWindow()
        );
        final List<BigDecimal> longAverages = TrendUtils.getSimpleMovingAverages(
                values,
                goldenCrossStrategyParams.getBigWindow()
        );

        return new Averages(shortAverages, longAverages);
    }

    @NotNull
    @Override
    public StrategyCache initCache() {
        return new SimpleGoldenCrossStrategyCache();
    }

    private static class SimpleGoldenCrossStrategyCache extends AbstractGoldenCrossStrategyCache {
    }

}