package ru.obukhov.trader.trading.bots.impl;

import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.MarketServiceImpl;
import ru.obukhov.trader.market.impl.OperationsServiceImpl;
import ru.obukhov.trader.market.impl.OrdersServiceImpl;
import ru.obukhov.trader.market.impl.PortfolioServiceImpl;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.trading.bots.interfaces.Bot;
import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;

@Service
public class FakeBotFactory extends AbstractBotFactory {

    public FakeBotFactory(
            final MarketProperties marketProperties,
            final MarketService realMarketService,
            final RealTinkoffService realTinkoffService
    ) {
        super(marketProperties, realMarketService, realTinkoffService);
    }

    @Override
    public Bot createBot(final AbstractTradingStrategy strategy, final double commission) {
        final FakeTinkoffService fakeTinkoffService = new FakeTinkoffService(marketProperties, commission, realMarketService, realTinkoffService);
        final MarketService fakeMarketService = new MarketServiceImpl(marketProperties, fakeTinkoffService);
        final OperationsService fakeOperationsService = new OperationsServiceImpl(fakeTinkoffService);
        final OrdersService fakeOrdersService = new OrdersServiceImpl(fakeTinkoffService, fakeMarketService);
        final PortfolioService fakePortfolioService = new PortfolioServiceImpl(fakeTinkoffService);

        return new FakeBotImpl(fakeMarketService, fakeOperationsService, fakeOrdersService, fakePortfolioService, strategy, fakeTinkoffService);
    }

}