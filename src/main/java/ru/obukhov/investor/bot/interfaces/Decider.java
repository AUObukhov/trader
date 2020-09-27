package ru.obukhov.investor.bot.interfaces;

import ru.obukhov.investor.Decision;
import ru.obukhov.investor.bot.model.DecisionData;

public interface Decider {

    Decision decide(DecisionData data);

}