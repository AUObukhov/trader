package ru.obukhov.trader.bot.interfaces;

import ru.obukhov.trader.bot.strategy.TradingStrategy;

public interface BotFactory {

    Bot createBot(TradingStrategy strategy);

}