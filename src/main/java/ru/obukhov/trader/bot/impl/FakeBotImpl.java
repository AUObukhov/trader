package ru.obukhov.trader.bot.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.bot.interfaces.FakeBot;
import ru.obukhov.trader.bot.interfaces.Strategy;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;

@Slf4j
public class FakeBotImpl extends AbstractBot implements FakeBot {

    @Getter
    private final String name;
    @Getter
    private final FakeTinkoffService fakeTinkoffService;

    public FakeBotImpl(String name,
                       Strategy strategy,
                       MarketService marketService,
                       OperationsService operationsService,
                       OrdersService ordersService,
                       PortfolioService portfolioService,
                       FakeTinkoffService fakeTinkoffService) {

        super(strategy, marketService, operationsService, ordersService, portfolioService);

        this.name = name;
        this.fakeTinkoffService = fakeTinkoffService;
    }

}