package ru.obukhov.trader.trading.bots.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.MarketInstrumentsService;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Share;

import java.time.OffsetDateTime;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class FakeBotFactoryUnitTest {

    private static final MarketProperties MARKET_PROPERTIES = TestData.createMarketProperties();

    @Mock
    private TradingStrategyFactory strategyFactory;
    @Mock
    private MarketService realMarketService;
    @Mock
    private RealTinkoffService realTinkoffService;
    @Mock
    private MarketInstrumentsService marketInstrumentsService;

    @InjectMocks
    private TinkoffServices tinkoffServices;

    private FakeBotFactory factory;

    @BeforeEach
    void setUp() {
        factory = new FakeBotFactory(MARKET_PROPERTIES, strategyFactory, tinkoffServices);
    }

    @Test
    void createBot_returnsNotNull() {
        final String ticker = "ticker";
        final BotConfig botConfig = new BotConfig(
                "2000124699",
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                StrategyType.CONSERVATIVE,
                Collections.emptyMap()
        );

        final BalanceConfig balanceConfig = new BalanceConfig();
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        Mockito.when(strategyFactory.createStrategy(botConfig)).thenReturn(TestData.CONSERVATIVE_STRATEGY);
        final Share share = TestData.createShare(ticker, Currency.RUB, 10);
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        final FakeBot bot = factory.createBot(botConfig, balanceConfig, currentDateTime);

        Assertions.assertNotNull(bot);
    }

}