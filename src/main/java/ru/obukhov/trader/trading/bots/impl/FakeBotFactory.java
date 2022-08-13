package ru.obukhov.trader.trading.bots.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.ExtOperationsService;
import ru.obukhov.trader.market.impl.ExtOrdersService;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
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
                tinkoffServices.extInstrumentsService(),
                marketDataService
        );

        final ExtOperationsService fakeOperationsService = new ExtOperationsService(fakeTinkoffService);
        final ExtOrdersService fakeOrdersService = new ExtOrdersService(fakeTinkoffService, tinkoffServices.extInstrumentsService());
        final AbstractTradingStrategy strategy = strategyFactory.createStrategy(botConfig);

        return new FakeBot(
                fakeExtMarketDataService,
                tinkoffServices.extInstrumentsService(),
                fakeOperationsService,
                fakeOrdersService,
                fakeTinkoffService,
                strategy
        );
    }

}