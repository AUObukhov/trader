package ru.obukhov.trader.trading.bots.interfaces;

import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;

public interface BotFactory {

    Bot createBot(final TradingStrategy strategy);

}