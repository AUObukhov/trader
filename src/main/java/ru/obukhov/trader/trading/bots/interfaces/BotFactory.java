package ru.obukhov.trader.trading.bots.interfaces;

import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

public interface BotFactory {

    Bot createBot(final AbstractTradingStrategy strategy, final CandleResolution candleResolution, final double commission);

}