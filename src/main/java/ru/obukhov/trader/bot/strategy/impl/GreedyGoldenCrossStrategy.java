package ru.obukhov.trader.bot.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.bot.model.Crossover;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.bot.strategy.StrategyCache;
import ru.obukhov.trader.config.TradingProperties;

/**
 * Same as {@link GoldenCrossStrategy}, but can buy paper
 * when short-term moving average crosses a long-term moving average from above
 * and profit from selling is too small.<br/>
 * It exists only because somehow it is more profitable than GoldenCrossStrategy ¯\_(ツ)_/¯
 */
@Slf4j
public class GreedyGoldenCrossStrategy extends GoldenCrossStrategy {

    public GreedyGoldenCrossStrategy(
            float minimumProfit,
            final TradingProperties tradingProperties,
            final int smallWindow,
            final int bigWindow,
            final float indexCoefficient
    ) {
        super(
                String.format("Greedy GC (%s-%s-%s)", smallWindow, bigWindow, indexCoefficient), minimumProfit,
                tradingProperties,
                smallWindow,
                bigWindow,
                indexCoefficient
        );
    }

    @Override
    protected Decision getDecisionByCrossover(DecisionData data, Crossover crossover, StrategyCache strategyCache) {
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
                if (decision.getAction() == DecisionAction.WAIT) {
                    decision = getBuyOrWaitDecision(data, strategyCache);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown crossover type: " + crossover);
        }
        return decision;
    }

}