package ru.obukhov.trader.trading.bots.interfaces;

import ru.obukhov.trader.market.impl.FakeTinkoffService;

public interface FakeBot extends Bot {

    String getStrategyName();

    FakeTinkoffService getFakeTinkoffService();

}