package ru.obukhov.trader.trading.strategy.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionsData;
import ru.obukhov.trader.web.model.BotConfig;

import java.util.Map;

public interface TradingStrategy {

    String getName();

    Map<String, Decision> decide(final DecisionsData data, final BotConfig botConfig, final Interval interval);

}