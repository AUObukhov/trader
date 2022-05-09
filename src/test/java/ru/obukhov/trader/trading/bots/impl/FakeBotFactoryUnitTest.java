package ru.obukhov.trader.trading.bots.impl;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.MarketInstrumentsService;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.Share;

import java.io.IOException;
import java.time.OffsetDateTime;

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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void createBot_returnsNotNull(@Nullable final String brokerAccountId) throws IOException {
        final String ticker = "ticker";
        final BotConfig botConfig = BotConfig.builder()
                .brokerAccountId(brokerAccountId)
                .ticker(ticker)
                .commission(0.003)
                .build();

        final BalanceConfig balanceConfig = new BalanceConfig();
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        Mockito.when(strategyFactory.createStrategy(botConfig)).thenReturn(TestData.CONSERVATIVE_STRATEGY);
        final Share share = Share.newBuilder().setTicker(ticker).setLot(10).build();
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        final FakeBot bot = factory.createBot(botConfig, balanceConfig, currentDateTime);

        Assertions.assertNotNull(bot);
    }

}