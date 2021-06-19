package ru.obukhov.trader.trading.bots.interfaces;

import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

public interface BotFactory {

    Bot createBot(final TradingStrategy strategy, CandleResolution candleResolution);

}