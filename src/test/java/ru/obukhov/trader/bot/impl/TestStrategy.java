package ru.obukhov.trader.bot.impl;

import org.apache.commons.lang3.NotImplementedException;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.bot.strategy.TradingStrategy;

public class TestStrategy implements TradingStrategy {

    @Override
    public String getName() {
        throw new NotImplementedException();
    }

    @Override
    public Decision decide(DecisionData data) {
        throw new NotImplementedException();
    }
}