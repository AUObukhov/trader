package ru.obukhov.trader.trading.strategy.impl;

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
import ru.obukhov.trader.trading.strategy.model.Averages;
import ru.obukhov.trader.trading.strategy.model.GoldenCrossStrategyParams;

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
public abstract class AbstractGoldenCrossStrategy extends AbstractTradingStrategy {

    protected AbstractGoldenCrossStrategy(
            final String name,
            final GoldenCrossStrategyParams params,
            final TradingProperties tradingProperties
    ) {
        super(name, params, tradingProperties);
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
            final GoldenCrossStrategyParams goldenCrossStrategyParams =
                    (GoldenCrossStrategyParams) params;
            final Averages averages = getAverages(values);

            final int index = (int) (goldenCrossStrategyParams.getIndexCoefficient() * (values.size() - 1));
            final Crossover crossover = TrendUtils.getCrossoverIfLast(
                    averages.getShortAverages(),
                    averages.getLongAverages(),
                    index
            );
            decision = decide(data, strategyCache, crossover);
        }

        return decision;
    }

    private Decision decide(
            final DecisionData data,
            final StrategyCache strategyCache,
            final Crossover crossover
    ) {
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

    /**
     * @return pair of lists of moving averages of given {@code values}.
     * Left is short-term moving averages, right is long-term
     */
    protected abstract Averages getAverages(final List<BigDecimal> values);

    protected abstract static class AbstractGoldenCrossStrategyCache implements StrategyCache {
    }

}
