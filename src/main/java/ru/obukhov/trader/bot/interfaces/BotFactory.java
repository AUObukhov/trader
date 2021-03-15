package ru.obukhov.trader.bot.interfaces;

import java.util.Set;

public interface BotFactory {

    Set<Bot> createBots();

}