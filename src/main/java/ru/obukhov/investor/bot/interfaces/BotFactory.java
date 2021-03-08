package ru.obukhov.investor.bot.interfaces;

import java.util.Set;

public interface BotFactory {

    Bot createConservativeBot();

    Bot createDumbBot();

    Set<Bot> createTrendReversalBots();

}