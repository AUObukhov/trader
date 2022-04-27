package ru.obukhov.trader.trading.bots.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.MarketOperationsService;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.OrdersService;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;

import java.io.IOException;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class FakeBotFactory {

    private final MarketProperties marketProperties;
    private final TradingStrategyFactory strategyFactory;
    private final TinkoffServices tinkoffServices;

    public FakeBot createBot(final BotConfig botConfig, final BalanceConfig balanceConfig, final OffsetDateTime currentDateTime) throws IOException {
        final FakeTinkoffService fakeTinkoffService = createFakeTinkoffService(botConfig, balanceConfig, currentDateTime);
        final MarketService fakeMarketService = new MarketService(marketProperties, fakeTinkoffService);
        final MarketOperationsService fakeOperationsService = new MarketOperationsService(fakeTinkoffService);
        final OrdersService fakeOrdersService = new OrdersService(fakeTinkoffService, fakeMarketService);
        final PortfolioService fakePortfolioService = new PortfolioService(fakeTinkoffService);
        final AbstractTradingStrategy strategy = strategyFactory.createStrategy(botConfig);

        return new FakeBot(fakeMarketService, fakeOperationsService, fakeOrdersService, fakePortfolioService, fakeTinkoffService, strategy);
    }

    private FakeTinkoffService createFakeTinkoffService(
            final BotConfig botConfig,
            final BalanceConfig balanceConfig,
            final OffsetDateTime currentDateTime
    ) throws IOException {
        final MarketInstrument marketInstrument = tinkoffServices.realTinkoffService().searchMarketInstrument(botConfig.getTicker());
        if (marketInstrument == null) {
            throw new IllegalArgumentException("Not found instrument for ticker '" + botConfig.getTicker() + "'");
        }

        return new FakeTinkoffService(
                marketProperties,
                tinkoffServices,
                botConfig.getBrokerAccountId(),
                currentDateTime,
                marketInstrument.currency(),
                botConfig.getCommission(),
                balanceConfig
        );
    }

}