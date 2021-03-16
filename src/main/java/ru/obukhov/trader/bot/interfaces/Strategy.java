package ru.obukhov.trader.bot.interfaces;

import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionData;

public interface Strategy {

    Decision decide(DecisionData data);

}