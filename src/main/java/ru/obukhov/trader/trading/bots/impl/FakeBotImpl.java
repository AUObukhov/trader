package ru.obukhov.trader.trading.bots.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.trading.bots.interfaces.FakeBot;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;

@Slf4j
public class FakeBotImpl extends AbstractBot implements FakeBot {

    @Getter
    private final FakeTinkoffService fakeTinkoffService;

    @Override
    public String getStrategyName() {
        return strategy.getName();
    }

    public FakeBotImpl(
            final TradingStrategy strategy,
            final MarketService marketService,
            final OperationsService operationsService,
            final OrdersService ordersService,
            final PortfolioService portfolioService,
            final FakeTinkoffService fakeTinkoffService
    ) {
        super(
                strategy,
                marketService,
                operationsService,
                ordersService,
                portfolioService,
                strategy.initCache()
        );

        this.fakeTinkoffService = fakeTinkoffService;
    }

}