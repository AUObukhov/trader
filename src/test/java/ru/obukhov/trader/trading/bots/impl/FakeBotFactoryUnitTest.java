package ru.obukhov.trader.trading.bots.impl;

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
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;

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

    @InjectMocks
    private FakeBotFactory factory;

    @BeforeEach
    void setUp() {
        factory = new FakeBotFactory(MARKET_PROPERTIES, strategyFactory, realMarketService, realTinkoffService);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void createBot_returnsNotNull(final String brokerAccountId) {
        final String ticker = "ticker";
        final BotConfig botConfig = BotConfig.builder()
                .brokerAccountId(brokerAccountId)
                .ticker(ticker)
                .commission(0.003)
                .build();

        final BalanceConfig balanceConfig = new BalanceConfig();
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        Mockito.when(strategyFactory.createStrategy(botConfig)).thenReturn(TestData.CONSERVATIVE_STRATEGY);
        Mocker.createAndMockInstrument(realTinkoffService, ticker, 10);

        final FakeBot bot = factory.createBot(botConfig, balanceConfig, currentDateTime);

        Assertions.assertNotNull(bot);
    }

}