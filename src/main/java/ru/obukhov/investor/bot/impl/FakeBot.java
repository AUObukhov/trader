package ru.obukhov.investor.bot.impl;

import lombok.extern.slf4j.Slf4j;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.service.impl.FakeTinkoffService;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.OperationsService;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.obukhov.investor.service.interfaces.PortfolioService;

@Slf4j
public class FakeBot extends SimpleBot {

    protected final FakeTinkoffService fakeTinkoffService;

    public FakeBot(Decider decider,
                   MarketService marketService,
                   OperationsService operationsService,
                   OrdersService ordersService,
                   PortfolioService portfolioService,
                   FakeTinkoffService fakeTinkoffService) {

        super(decider, marketService, operationsService, ordersService, portfolioService);

        this.fakeTinkoffService = fakeTinkoffService;
    }

}