package ru.obukhov.trader.trading.bots.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.trading.bots.interfaces.Bot;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;

@Slf4j
public class FakeBot extends AbstractBot implements Bot {

    @Getter
    private final FakeTinkoffService fakeTinkoffService;

    public FakeBot(
            final MarketService marketService,
            final OperationsService operationsService,
            final OrdersService ordersService,
            final PortfolioService portfolioService,
            final TradingStrategy strategy,
            final FakeTinkoffService fakeTinkoffService
    ) {
        super(marketService, operationsService, ordersService, portfolioService, strategy, strategy.initCache());

        this.fakeTinkoffService = fakeTinkoffService;
    }

}