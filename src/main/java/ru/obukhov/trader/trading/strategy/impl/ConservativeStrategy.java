package ru.obukhov.trader.trading.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.DecisionsData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.web.model.BotConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Strategy which decides to buy security always when possible and never to sell
 */
@Slf4j
public class ConservativeStrategy extends AbstractTradingStrategy {

    private final ExtMarketDataService extMarketDataService;

    public ConservativeStrategy(final String name, final ExtMarketDataService extMarketDataService) {
        super(name, null);

        this.extMarketDataService = extMarketDataService;
    }

    @Override
    public Map<String, Decision> decide(@NotNull final DecisionsData data, @NotNull final StrategyCache strategyCache) {
        Assert.isTrue(data.getDecisionDataList().size() == 1, "Conservative strategy supports 1 instrument only");

        if (existsOperationStateIsUnspecified(data)) {
            Decision decision = new Decision(DecisionAction.WAIT, null, strategyCache);
            log.debug("Exists operation in progress. Decision is {}", decision.toPrettyString());
            return data.getDecisionDataList().stream()
                    .collect(Collectors.toMap(decisionData -> decisionData.getShare().figi(), decisionData -> decision));
        } else {
            final DecisionData decisionData = data.getDecisionDataList().get(0);

            if (decisionData.getAvailableLots() == 0) {
                final Decision decision = new Decision(DecisionAction.WAIT, null, strategyCache);
                return Map.of(decisionData.getShare().figi(), decision);
            }

            final Decision decision = getBuyOrWaitDecision(decisionData, decisionData.getAvailableLots(), strategyCache);
            return Map.of(decisionData.getShare().figi(), decision);
        }
    }

    @Override
    public StrategyCache initCache(final BotConfig botConfig, final Interval interval) {
        final Map<String, List<Candle>> candlesByFigies = new HashMap<>(botConfig.figies().size(), 1);
        for (final String figi : botConfig.figies()) {
            final List<Candle> candles = extMarketDataService.getCandles(figi, interval, botConfig.candleInterval());
            candlesByFigies.put(figi, candles);
        }

        return new ConservativeStrategyCache(candlesByFigies);
    }

    @Getter
    @AllArgsConstructor
    private static class ConservativeStrategyCache implements StrategyCache {
        private final Map<String, List<Candle>> candlesByFigies;
    }

}