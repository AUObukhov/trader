package ru.obukhov.trader.trading.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.trading.model.CrossStrategyParams;
import ru.obukhov.trader.trading.model.Crossover;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.DecisionsData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.web.model.BotConfig;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final ExtMarketDataService extMarketDataService;
    private final MovingAverager averager;

    protected CrossStrategy(
            final String name,
            final CrossStrategyParams params,
            final ExtMarketDataService extMarketDataService,
            final MovingAverager averager
    ) {
        super(name, params);

        this.extMarketDataService = extMarketDataService;
        this.averager = averager;
    }

    @Override
    public Map<String, Decision> decide(@NotNull final DecisionsData data, @NotNull final StrategyCache strategyCache) {
        Assert.isTrue(data.getDecisionDataList().size() == 1, "Cross strategy supports 1 instrument only");

        if (existsOperationStateIsUnspecified(data)) {
            Decision decision = new Decision(DecisionAction.WAIT, null, strategyCache);
            log.debug("Exists operation in progress. Decision is {}", decision.toPrettyString());
            return data.getDecisionDataList().stream()
                    .collect(Collectors.toMap(decisionData -> decisionData.getShare().figi(), decisionData -> decision));
        } else {
            final DecisionData decisionData = data.getDecisionDataList().get(0);
            final String figi = decisionData.getShare().figi();

            if (decisionData.getAvailableLots() == 0) {
                final Decision decision = new Decision(DecisionAction.WAIT, null, strategyCache);
                return Map.of(figi, decision);
            }

            final List<BigDecimal> values = ((CrossStrategyCache) strategyCache)
                    .getCandlesByFigies()
                    .get(figi)
                    .stream()
                    .map(Candle::getOpen)
                    .toList();
            final CrossStrategyParams crossStrategyParams = (CrossStrategyParams) params;
            final List<BigDecimal> shortAverages = averager.getAverages(values, crossStrategyParams.getSmallWindow());
            final List<BigDecimal> longAverages = averager.getAverages(values, crossStrategyParams.getBigWindow());

            final int index = (int) (crossStrategyParams.getIndexCoefficient() * (values.size() - 1));
            final Crossover crossover = TrendUtils.getCrossoverIfLast(shortAverages, longAverages, index);
            final Decision decision = decide(decisionData, data.getCommission(), crossover, strategyCache);
            return Map.of(figi, decision);
        }
    }

    private Decision decide(
            final DecisionData data,
            final BigDecimal commission,
            final Crossover crossover,
            final StrategyCache strategyCache
    ) {
        return switch (crossover) {
            case BELOW -> getBuyOrWaitDecision(data, data.getAvailableLots(), strategyCache);
            case ABOVE -> getDecisionForAboveCrossover(data, commission, strategyCache);
            case NONE -> getDecisionForNoCrossover(strategyCache);
        };
    }

    private Decision getDecisionForAboveCrossover(final DecisionData data, final BigDecimal commission, final StrategyCache strategyCache) {
        final CrossStrategyParams crossStrategyParams = (CrossStrategyParams) params;
        final boolean greedy = crossStrategyParams.getGreedy();
        final List<Candle> candles = ((CrossStrategyCache) strategyCache).getCandlesByFigies().get(data.getShare().figi());
        final BigDecimal currentPrice = CollectionsUtils.getLast(candles).getClose();
        Decision decision = getSellOrWaitDecision(data, currentPrice, commission, crossStrategyParams.getMinimumProfit(), strategyCache);
        if (greedy && decision.getAction() == DecisionAction.WAIT) {
            decision = getBuyOrWaitDecision(data, 1, strategyCache);
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

    @Override
    public StrategyCache initCache(final BotConfig botConfig, final Interval interval) {
        final Map<String, List<Candle>> candlesByFigies = new HashMap<>(botConfig.figies().size(), 1);
        for (final String figi : botConfig.figies()) {
            final List<Candle> candles = extMarketDataService.getCandles(figi, interval, botConfig.candleInterval());
            candlesByFigies.put(figi, candles);
        }

        return new CrossStrategyCache(candlesByFigies);
    }

    @Getter
    @AllArgsConstructor
    private static class CrossStrategyCache implements StrategyCache {
        private final Map<String, List<Candle>> candlesByFigies;
    }

}