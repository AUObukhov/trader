package ru.obukhov.trader.trading.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.trading.model.CrossStrategyParams;
import ru.obukhov.trader.trading.model.Crossover;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.util.List;

/**
 * Trading strategy based on idea to buy when short-term moving average crosses a long-term moving average
 * from below and to sell when from above.<br/>
 * Exact algorithm of moving averages implemented by descendants.
 *
 * @see <a href="https://www.investopedia.com/terms/g/goldencross.asp">Golden Cross</a>
 * @see <a href="https://en.wikipedia.org/wiki/Moving_average">Moving average</a>
 */
@Slf4j
public class CrossStrategy extends AbstractTradingStrategy {

    private final MovingAverager averager;

    protected CrossStrategy(final String name, final CrossStrategyParams params, final MovingAverager averager) {
        super(name, params);

        this.averager = averager;
    }

    @Override
    public Decision decide(@NotNull final DecisionData data, @NotNull final StrategyCache strategyCache) {
        Decision decision;
        if (existsOperationStateIsUnspecified(data)) {
            decision = new Decision(DecisionAction.WAIT, null, strategyCache);
            log.debug("Exists operation in progress. Decision is {}", decision.toPrettyString());
        } else {
            final List<Quotation> values = data.getCurrentCandles().stream()
                    .map(Candle::getOpen)
                    .toList();
            final CrossStrategyParams crossStrategyParams = (CrossStrategyParams) params;
            final List<Quotation> shortAverages = averager.getAverages(values, crossStrategyParams.getSmallWindow());
            final List<Quotation> longAverages = averager.getAverages(values, crossStrategyParams.getBigWindow());

            final int index = (int) (crossStrategyParams.getIndexCoefficient() * (values.size() - 1));
            final Crossover crossover = TrendUtils.getCrossoverIfLast(shortAverages, longAverages, index);
            decision = decide(data, strategyCache, crossover);
        }

        return decision;
    }

    private Decision decide(final DecisionData data, final StrategyCache strategyCache, final Crossover crossover) {
        return switch (crossover) {
            case BELOW -> getBuyOrWaitDecision(data, strategyCache);
            case ABOVE -> getDecisionForAboveCrossover(data, strategyCache);
            case NONE -> getDecisionForNoCrossover(strategyCache);
        };
    }

    private Decision getDecisionForAboveCrossover(final DecisionData data, final StrategyCache strategyCache) {
        final CrossStrategyParams crossStrategyParams = (CrossStrategyParams) params;
        final boolean greedy = crossStrategyParams.getGreedy();
        Decision decision = getSellOrWaitDecision(data, crossStrategyParams.getMinimumProfit(), strategyCache);
        if (greedy && decision.getAction() == DecisionAction.WAIT) {
            decision = getBuyOrWaitDecision(data, strategyCache);
        }
        return decision;
    }

    private Decision getDecisionForNoCrossover(final StrategyCache strategyCache) {
        Decision decision = new Decision(DecisionAction.WAIT, null, strategyCache);
        if (log.isDebugEnabled()) {
            log.debug("No crossover at expected position. Decision is {}", decision.toPrettyString());
        }
        return decision;
    }

    @NotNull
    @Override
    public StrategyCache initCache() {
        return new CrossStrategy.CrossStrategyCache();
    }

    private static class CrossStrategyCache implements StrategyCache {
    }

}