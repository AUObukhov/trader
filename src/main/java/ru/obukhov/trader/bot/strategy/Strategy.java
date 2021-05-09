package ru.obukhov.trader.bot.strategy;

import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionData;

public interface Strategy {

    String getName();

    Decision decide(DecisionData data);

}