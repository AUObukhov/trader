package ru.obukhov.trader.bot.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.bot.interfaces.FakeBot;
import ru.obukhov.trader.bot.strategy.TradingStrategy;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;

@Slf4j
public class FakeBotImpl extends AbstractBot implements FakeBot {

    @Getter
    private final FakeTinkoffService fakeTinkoffService;

    @Override
    public String getStrategyName() {
        return strategy.getName();
    }

    public static FakeBotImpl create(
            TradingStrategy strategy,
            MarketService marketService,
            OperationsService operationsService,
            OrdersService ordersService,
            PortfolioService portfolioService,
            FakeTinkoffService fakeTinkoffService
    ) {
        return new FakeBotImpl(
                strategy,
                marketService,
                operationsService,
                ordersService,
                portfolioService,
                fakeTinkoffService
        );
    }

    private FakeBotImpl(
            TradingStrategy strategy,
            MarketService marketService,
            OperationsService operationsService,
            OrdersService ordersService,
            PortfolioService portfolioService,
            FakeTinkoffService fakeTinkoffService
    ) {
        super(strategy, marketService, operationsService, ordersService, portfolioService);

        this.fakeTinkoffService = fakeTinkoffService;
    }

}