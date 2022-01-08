package ru.obukhov.trader.trading.bots.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.MarketServiceImpl;
import ru.obukhov.trader.market.impl.OperationsServiceImpl;
import ru.obukhov.trader.market.impl.OrdersServiceImpl;
import ru.obukhov.trader.market.impl.PortfolioServiceImpl;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BotConfig;

@Service
@RequiredArgsConstructor
public class FakeBotFactory {

    private final MarketProperties marketProperties;
    private final TradingStrategyFactory strategyFactory;
    private final FakeTinkoffServiceFactory fakeTinkoffServiceFactory;

    public FakeBot createBot(final BotConfig botConfig, final Double commission) {
        final FakeTinkoffService fakeTinkoffService = fakeTinkoffServiceFactory.createService(commission);
        final MarketService fakeMarketService = new MarketServiceImpl(marketProperties, fakeTinkoffService);
        final OperationsService fakeOperationsService = new OperationsServiceImpl(fakeTinkoffService);
        final OrdersService fakeOrdersService = new OrdersServiceImpl(fakeTinkoffService, fakeMarketService);
        final PortfolioService fakePortfolioService = new PortfolioServiceImpl(fakeTinkoffService);
        final AbstractTradingStrategy strategy = strategyFactory.createStrategy(botConfig);

        return new FakeBot(fakeMarketService, fakeOperationsService, fakeOrdersService, fakePortfolioService, strategy, fakeTinkoffService);
    }

}