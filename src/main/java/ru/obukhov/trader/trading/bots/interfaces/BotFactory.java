package ru.obukhov.trader.trading.bots.interfaces;

import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;

public interface BotFactory {

    Bot createBot(final AbstractTradingStrategy strategy, final double commission);

}