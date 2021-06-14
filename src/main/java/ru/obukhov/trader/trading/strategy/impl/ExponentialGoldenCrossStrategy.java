package ru.obukhov.trader.trading.strategy.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.trading.model.Crossover;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.trading.strategy.model.ExponentialGoldenCrossStrategyParams;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Trading strategy based on idea to buy when fast decreasing exponentially weighted moving average crosses a slow
 * decreasing exponentially weighted moving average from below and to sell when from above.
 *
 * @see <a href="https://www.investopedia.com/terms/g/goldencross.asp">investopedia</a>
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
    public Decision decide(final DecisionData data, final StrategyCache strategyCache) {
        Decision decision;
        if (existsOperationInProgress(data)) {
            decision = new Decision(DecisionAction.WAIT, null, strategyCache);
            log.debug("Exists operation in progress. Decision is {}", decision.toPrettyString());
        } else {
            final List<BigDecimal> values = data.getCurrentCandles().stream()
                    .map(Candle::getOpenPrice)
                    .collect(Collectors.toList());
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

            final int index = (int) (exponentialGoldenCrossStrategyParams.getIndexCoefficient() * (values.size() - 1));
            final Crossover crossover = TrendUtils.getCrossoverIfLast(shortAverages, longAverages, index);
            decision = getDecisionByCrossover(data, crossover, strategyCache);
        }

        return decision;
    }

    @NotNull
    @Override
    public StrategyCache initCache() {
        return new LinearGoldenCrossStrategyCache();
    }

    @Data
    private static class LinearGoldenCrossStrategyCache implements StrategyCache {
    }

}