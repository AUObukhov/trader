package ru.obukhov.trader.trading.bots.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.MarketOperationsService;
import ru.obukhov.trader.market.impl.MarketOrdersService;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.core.MarketDataService;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class FakeBotFactory {

    private final MarketProperties marketProperties;
    private final TradingStrategyFactory strategyFactory;
    private final TinkoffServices tinkoffServices;
    private final ApplicationContext applicationContext;
    private final MarketDataService marketDataService;

    public FakeBot createBot(final BotConfig botConfig, final BalanceConfig balanceConfig, final OffsetDateTime currentDateTime) {
        final FakeTinkoffService fakeTinkoffService = (FakeTinkoffService) applicationContext.getBean(
                "fakeTinkoffService",
                marketProperties,
                tinkoffServices,
                botConfig,
                balanceConfig,
                currentDateTime
        );
        final ExtMarketDataService fakeExtMarketDataService = (ExtMarketDataService) applicationContext.getBean(
                "fakeExtMarketDataService",
                marketProperties,
                fakeTinkoffService,
                marketDataService
        );

        final MarketOperationsService fakeOperationsService = new MarketOperationsService(fakeTinkoffService);
        final MarketOrdersService fakeOrdersService = new MarketOrdersService(fakeTinkoffService);
        final PortfolioService fakePortfolioService = new PortfolioService(fakeTinkoffService);
        final AbstractTradingStrategy strategy = strategyFactory.createStrategy(botConfig);

        return new FakeBot(
                fakeExtMarketDataService,
                tinkoffServices.extInstrumentsService(),
                fakeOperationsService,
                fakeOrdersService,
                fakePortfolioService,
                fakeTinkoffService,
                strategy
        );
    }

}