package ru.obukhov.trader.trading.strategy.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.trading.model.Crossover;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;

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
public class ExponentialGoldenCrossStrategy extends AbstractTradingStrategy {

    private final ExponentialGoldenCrossStrategyParams params;

    /**
     * Initializes new instance of {@link ExponentialGoldenCrossStrategy}
     *
     * @param minimumProfit     minimum value of profit in percent, which allows to sell papers
     * @param tradingProperties common trading properties
     * @param params            params of strategy
     */
    public ExponentialGoldenCrossStrategy(
            final float minimumProfit,
            final TradingProperties tradingProperties,
            final ExponentialGoldenCrossStrategyParams params
    ) {
        super(getName(minimumProfit, params), minimumProfit, tradingProperties);

        this.params = params;
    }

    private static String getName(final float minimumProfit, final ExponentialGoldenCrossStrategyParams params) {
        final String greedyString = BooleanUtils.toString(params.getGreedy(), "Greedy", "Plain");
        return String.format(
                "%s Exponential Golden Cross (%s, %s-%s-%s)",
                greedyString,
                minimumProfit,
                params.getFastWeightDecrease(),
                params.getSlowWeightDecrease(),
                params.getIndexCoefficient()
        );
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
            final List<BigDecimal> shortAverages =
                    TrendUtils.getExponentialWeightedMovingAverages(values, params.getFastWeightDecrease());
            final List<BigDecimal> longAverages =
                    TrendUtils.getExponentialWeightedMovingAverages(values, params.getSlowWeightDecrease());

            final int index = (int) (params.getIndexCoefficient() * (values.size() - 1));
            final Crossover crossover = TrendUtils.getCrossoverIfLast(shortAverages, longAverages, index);
            decision = getDecisionByCrossover(data, crossover, strategyCache);
        }

        return decision;
    }

    private Decision getDecisionByCrossover(DecisionData data, Crossover crossover, StrategyCache strategyCache) {
        Decision decision;
        switch (crossover) {
            case NONE:
                decision = new Decision(DecisionAction.WAIT, null, strategyCache);
                log.debug("No crossover at expected position. Decision is {}", decision.toPrettyString());
                break;
            case BELOW:
                decision = getBuyOrWaitDecision(data, strategyCache);
                break;
            case ABOVE:
                decision = getSellOrWaitDecision(data, strategyCache);
                if (params.getGreedy() && decision.getAction() == DecisionAction.WAIT) {
                    decision = getBuyOrWaitDecision(data, strategyCache);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown crossover type: " + crossover);
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