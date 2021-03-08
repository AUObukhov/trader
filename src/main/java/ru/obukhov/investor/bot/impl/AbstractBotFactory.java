package ru.obukhov.investor.bot.impl;

import lombok.AllArgsConstructor;
import ru.obukhov.investor.bot.interfaces.BotFactory;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.market.impl.RealTinkoffService;
import ru.obukhov.investor.market.interfaces.MarketService;

import java.util.Set;

@AllArgsConstructor
public abstract class AbstractBotFactory implements BotFactory {

    protected TradingProperties tradingProperties;

    protected MarketService realMarketService;

    protected RealTinkoffService realTinkoffService;

    protected Decider conservativeDecider;

    protected Decider dumbDecider;

    protected Set<TrendReversalDecider> trendReversalDeciders;

}