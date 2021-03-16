package ru.obukhov.trader.bot.impl;

import lombok.AllArgsConstructor;
import ru.obukhov.trader.bot.interfaces.BotFactory;
import ru.obukhov.trader.bot.interfaces.Strategy;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;

import java.util.Set;

@AllArgsConstructor
public abstract class AbstractBotFactory implements BotFactory {

    protected TradingProperties tradingProperties;

    protected MarketService realMarketService;

    protected RealTinkoffService realTinkoffService;

    protected Set<Strategy> strategies;

}