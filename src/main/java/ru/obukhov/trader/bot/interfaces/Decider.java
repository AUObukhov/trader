package ru.obukhov.trader.bot.interfaces;

import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionData;

public interface Decider {

    Decision decide(DecisionData data);

}