package ru.obukhov.trader.trading.bots.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.MarketOperationsService;
import ru.obukhov.trader.market.impl.MarketOrdersService;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.Share;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class FakeBotFactory {

    private final MarketProperties marketProperties;
    private final TradingStrategyFactory strategyFactory;
    private final TinkoffServices tinkoffServices;

    public FakeBot createBot(final BotConfig botConfig, final BalanceConfig balanceConfig, final OffsetDateTime currentDateTime) {
        final FakeTinkoffService fakeTinkoffService = createFakeTinkoffService(botConfig, balanceConfig, currentDateTime);
        final MarketService fakeMarketService = new MarketService(marketProperties, fakeTinkoffService);

        final MarketOperationsService fakeOperationsService = new MarketOperationsService(fakeTinkoffService);
        final MarketOrdersService fakeOrdersService = new MarketOrdersService(fakeTinkoffService);
        final PortfolioService fakePortfolioService = new PortfolioService(fakeTinkoffService);
        final AbstractTradingStrategy strategy = strategyFactory.createStrategy(botConfig);

        return new FakeBot(
                fakeMarketService,
                tinkoffServices.marketInstrumentsService(),
                fakeOperationsService,
                fakeOrdersService,
                fakePortfolioService,
                fakeTinkoffService,
                strategy
        );
    }

    private FakeTinkoffService createFakeTinkoffService(
            final BotConfig botConfig,
            final BalanceConfig balanceConfig,
            final OffsetDateTime currentDateTime
    ) {

        final Share share = tinkoffServices.marketInstrumentsService().getShare(botConfig.ticker());
        if (share == null) {
            throw new IllegalArgumentException("Not found share for ticker '" + botConfig.ticker() + "'");
        }

        return new FakeTinkoffService(
                marketProperties,
                tinkoffServices,
                botConfig.accountId(),
                currentDateTime,
                Currency.valueOfIgnoreCase(share.getCurrency()),
                botConfig.commission(),
                balanceConfig
        );
    }

}