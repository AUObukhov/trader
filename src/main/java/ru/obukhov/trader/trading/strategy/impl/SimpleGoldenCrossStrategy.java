package ru.obukhov.trader.trading.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.trading.strategy.model.Averages;
import ru.obukhov.trader.trading.strategy.model.SimpleGoldenCrossStrategyParams;

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
     * @param tradingProperties common trading properties
     * @param params            params of strategy
     */
    public SimpleGoldenCrossStrategy(
            final SimpleGoldenCrossStrategyParams params,
            final TradingProperties tradingProperties
    ) {
        super("simpleGoldenCross", params, tradingProperties);
    }

    @Override
    protected Averages getAverages(final List<BigDecimal> values) {
        final SimpleGoldenCrossStrategyParams simpleGoldenCrossStrategyParams =
                (SimpleGoldenCrossStrategyParams) params;
        final List<BigDecimal> shortAverages = TrendUtils.getSimpleMovingAverages(
                values,
                simpleGoldenCrossStrategyParams.getSmallWindow()
        );
        final List<BigDecimal> longAverages = TrendUtils.getSimpleMovingAverages(
                values,
                simpleGoldenCrossStrategyParams.getBigWindow()
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