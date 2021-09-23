package ru.obukhov.trader.trading.bots.impl;

import lombok.AllArgsConstructor;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.trading.bots.interfaces.BotFactory;

@AllArgsConstructor
public abstract class AbstractBotFactory implements BotFactory {

    protected MarketProperties marketProperties;

    protected MarketService realMarketService;

    protected RealTinkoffService realTinkoffService;

}