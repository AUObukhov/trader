package ru.obukhov.trader.bot.interfaces;

import ru.obukhov.trader.bot.model.DecisionData;

import java.time.OffsetDateTime;

public interface Bot {

    DecisionData processTicker(String ticker, OffsetDateTime previousStartTime);

}