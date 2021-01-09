package ru.obukhov.investor.bot.interfaces;

import ru.obukhov.investor.bot.model.DecisionData;

public interface Bot {

    DecisionData processTicker(String ticker);

}