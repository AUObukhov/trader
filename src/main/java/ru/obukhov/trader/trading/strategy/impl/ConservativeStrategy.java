package ru.obukhov.trader.trading.strategy.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.web.model.BotConfig;

import java.util.List;

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
    public Decision decide(@NotNull final DecisionData data, final long availableLots, @NotNull final StrategyCache strategyCache) {
        Decision decision;
        if (existsOperationStateIsUnspecified(data)) {
            decision = new Decision(DecisionAction.WAIT, null, strategyCache);
            log.debug("Exists operation in progress. Decision is {}", decision.toPrettyString());
        } else {
            decision = getBuyOrWaitDecision(data, availableLots, strategyCache);
        }

        return decision;
    }

    @Override
    public StrategyCache initCache(final BotConfig botConfig, final Interval interval) {
        final List<Candle> candles = extMarketDataService.getCandles(botConfig.figi(), interval, botConfig.candleInterval());
        return new ConservativeStrategyCache(candles);
    }

    @Getter
    @AllArgsConstructor
    private static class ConservativeStrategyCache implements StrategyCache {
        private final List<Candle> candles;
    }

}