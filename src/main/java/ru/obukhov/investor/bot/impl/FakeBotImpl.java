package ru.obukhov.investor.bot.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.bot.interfaces.FakeBot;
import ru.obukhov.investor.service.impl.FakeTinkoffService;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.OperationsService;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.obukhov.investor.service.interfaces.PortfolioService;

@Slf4j
public class FakeBotImpl extends AbstractBot implements FakeBot {

    @Getter
    private final String name;
    @Getter
    private final FakeTinkoffService fakeTinkoffService;

    public FakeBotImpl(String name,
                       Decider decider,
                       MarketService marketService,
                       OperationsService operationsService,
                       OrdersService ordersService,
                       PortfolioService portfolioService,
                       FakeTinkoffService fakeTinkoffService) {

        super(decider, marketService, operationsService, ordersService, portfolioService);

        this.name = name;
        this.fakeTinkoffService = fakeTinkoffService;
    }

}