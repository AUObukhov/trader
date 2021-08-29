package ru.obukhov.trader.trading.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.trading.model.Crossover;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.GoldenCrossStrategyParams;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Trading strategy based on idea to buy when short-term moving average crosses a long-term moving averag
 * e from below and to sell when from above.<br/>
 * Exact algorithm of moving averages implemented by descendants.
 *
 * @see <a href="https://www.investopedia.com/terms/g/goldencross.asp">Golden Cross</a>
 * @see <a href="https://en.wikipedia.org/wiki/Moving_average">Moving average</a>
 */
@Slf4j
public class GoldenCrossStrategy extends AbstractTradingStrategy {

    private final MovingAverager averager;

    protected GoldenCrossStrategy(
            final String name,
            final GoldenCrossStrategyParams params,
            final TradingProperties tradingProperties,
            final MovingAverager averager
    ) {
        super(name, params, tradingProperties);

        this.averager = averager;
    }

    @Override
    public Decision decide(@NotNull final DecisionData data, @NotNull final StrategyCache strategyCache) {
        Decision decision;
        if (existsOperationInProgress(data)) {
            decision = new Decision(DecisionAction.WAIT, null, strategyCache);
            log.debug("Exists operation in progress. Decision is {}", decision.toPrettyString());
        } else {
            final List<BigDecimal> values = data.getCurrentCandles().stream()
                    .map(Candle::getOpenPrice)
                    .collect(Collectors.toList());
            final GoldenCrossStrategyParams goldenCrossStrategyParams = (GoldenCrossStrategyParams) params;
            final List<BigDecimal> shortAverages = averager.getAverages(values, goldenCrossStrategyParams.getSmallWindow());
            final List<BigDecimal> longAverages = averager.getAverages(values, goldenCrossStrategyParams.getBigWindow());

            final int index = (int) (goldenCrossStrategyParams.getIndexCoefficient() * (values.size() - 1));
            final Crossover crossover = TrendUtils.getCrossoverIfLast(shortAverages, longAverages, index);
            decision = decide(data, strategyCache, crossover);
        }

        return decision;
    }

    private Decision decide(final DecisionData data, final StrategyCache strategyCache, final Crossover crossover) {
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
                final GoldenCrossStrategyParams goldenCrossStrategyParams = (GoldenCrossStrategyParams) params;
                if (goldenCrossStrategyParams.getGreedy() && decision.getAction() == DecisionAction.WAIT) {
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
        return new GoldenCrossStrategy.GoldenCrossStrategyCache();
    }

    private static class GoldenCrossStrategyCache implements StrategyCache {
    }

}