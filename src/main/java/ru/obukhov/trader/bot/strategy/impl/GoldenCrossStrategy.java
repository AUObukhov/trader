package ru.obukhov.trader.bot.strategy.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.bot.model.Crossover;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.bot.strategy.AbstractTradingStrategy;
import ru.obukhov.trader.bot.strategy.StrategyCache;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.model.Candle;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Trading strategy based on idea to buy when short-term moving average crosses above a long-term moving average.
 *
 * @see <a href="https://www.investopedia.com/terms/g/goldencross.asp">investopedia</a>
 */
@Slf4j
public class GoldenCrossStrategy extends AbstractTradingStrategy {

    private final int smallWindow;
    private final int bigWindow;
    private final float indexCoefficient;

    public GoldenCrossStrategy(
            final TradingProperties tradingProperties,
            final int smallWindow,
            final int bigWindow,
            final float indexCoefficient
    ) {
        super(String.format("Golden cross (%s-%s-%s)", smallWindow, bigWindow, indexCoefficient), tradingProperties);

        this.smallWindow = smallWindow;
        this.bigWindow = bigWindow;
        this.indexCoefficient = indexCoefficient;
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
            final List<BigDecimal> shortAverages = TrendUtils.getSimpleMovingAverages(values, smallWindow);
            final List<BigDecimal> longAverages = TrendUtils.getSimpleMovingAverages(values, bigWindow);

            final int index = (int) (indexCoefficient * (values.size() - 1));
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
                log.debug("No crossover in the end. Decision is {}", decision.toPrettyString());
                break;
            case BELOW:
                decision = getBuyOrWaitDecision(data, strategyCache);
                break;
            case ABOVE:
                decision = getSellOrWaitDecision(data, strategyCache);
                if (decision.getAction() == DecisionAction.WAIT) { // todo - really necessary? looks like not but profit is actually greater sometimes
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
        return new GoldenCrossStrategyCache();
    }

    @Data
    private static class GoldenCrossStrategyCache implements StrategyCache {
    }
}