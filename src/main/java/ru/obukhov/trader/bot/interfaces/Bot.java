package ru.obukhov.trader.bot.interfaces;

import ru.obukhov.trader.bot.model.DecisionData;

public interface Bot {

    DecisionData processTicker(String ticker);

}