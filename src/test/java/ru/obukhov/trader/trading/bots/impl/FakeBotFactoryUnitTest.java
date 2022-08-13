package ru.obukhov.trader.trading.bots.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.FakeContext;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.MarketDataService;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class FakeBotFactoryUnitTest {

    private static final MarketProperties MARKET_PROPERTIES = TestData.createMarketProperties();

    @Mock
    private TradingStrategyFactory strategyFactory;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private MarketDataService marketDataService;
    @Mock
    private ExtInstrumentsService extInstrumentsService;

    @InjectMocks
    private TinkoffServices tinkoffServices;

    private FakeBotFactory factory;

    @BeforeEach
    void setUp() {
        factory = new FakeBotFactory(MARKET_PROPERTIES, strategyFactory, tinkoffServices, applicationContext, marketDataService);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateBot_movesCurrentDateTimeToCeilingWorkTime() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 12),
                        DateTimeTestData.createDateTime(2020, 10, 5, 12)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 19),
                        DateTimeTestData.createDateTime(2020, 10, 6, 10)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 19, 20),
                        DateTimeTestData.createDateTime(2020, 10, 6, 10)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 9, 19), // friday
                        DateTimeTestData.createDateTime(2020, 10, 12, 10) // monday
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 10, 12), // saturday
                        DateTimeTestData.createDateTime(2020, 10, 12, 10) // monday
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 9, 9),
                        DateTimeTestData.createDateTime(2020, 10, 9, 10)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forCreateBot_movesCurrentDateTimeToCeilingWorkTime")
    void createBot_movesCurrentDateTimeToCeilingWorkTime(final OffsetDateTime currentDateTime, final OffsetDateTime expectedCurrentDateTime) {
        final String ticker = "ticker";
        final Currency currency = Currency.RUB;
        final BotConfig botConfig = new BotConfig(
                "2000124699",
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                StrategyType.CONSERVATIVE,
                Collections.emptyMap()
        );

        final BalanceConfig balanceConfig = new BalanceConfig();

        mockCurrency(ticker, currency);
        Mockito.when(strategyFactory.createStrategy(botConfig)).thenReturn(TestData.CONSERVATIVE_STRATEGY);
        mockFakeTinkoffService();

        final FakeBot bot = factory.createBot(botConfig, balanceConfig, currentDateTime);

        Assertions.assertEquals(expectedCurrentDateTime, bot.getCurrentDateTime());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateBot_initializesBalance() {
        return Stream.of(
                Arguments.of(TestData.createBalanceConfig(1000000.0, 1000.0, "0 0 0 1 * ?"), 1001000.0),
                Arguments.of(TestData.createBalanceConfig(1000000.0, 1000.0, "0 0 0 2 * ?"), 1000000.0),
                Arguments.of(TestData.createBalanceConfig(1000000.0), 1000000.0)
        );
    }

    @ParameterizedTest
    @MethodSource(value = "getData_forCreateBot_initializesBalance")
    void createBot_initializesBalance(final BalanceConfig balanceConfig, final double expectedBalance) {
        final String ticker = "ticker";
        final Currency currency = Currency.RUB;
        final BotConfig botConfig = new BotConfig(
                "2000124699",
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                StrategyType.CONSERVATIVE,
                Collections.emptyMap()
        );

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 10, 1);

        mockCurrency(ticker, currency);
        Mockito.when(strategyFactory.createStrategy(botConfig)).thenReturn(TestData.CONSERVATIVE_STRATEGY);
        mockFakeTinkoffService();

        final FakeBot bot = factory.createBot(botConfig, balanceConfig, currentDateTime);

        AssertUtils.assertEquals(expectedBalance, bot.getCurrentBalance(botConfig.accountId(), currency));
    }

    @Test
    void createBot_throwIllegalArgumentException_whenShareNotFound() {
        final String ticker = "ticker";
        final BotConfig botConfig = new BotConfig(
                "2000124699",
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                StrategyType.CONSERVATIVE,
                Collections.emptyMap()
        );
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> factory.createBot(botConfig, balanceConfig, currentDateTime),
                "Not found share for ticker '" + ticker + "'"
        );
    }

    private void mockCurrency(final String ticker, final Currency currency) {
        final Share share = Share.newBuilder().setCurrency(currency.name()).build();
        Mockito.when(extInstrumentsService.getShare(ticker)).thenReturn(share);
    }

    private void mockFakeTinkoffService() {
        Mockito.when(applicationContext.getBean(
                Mockito.eq("fakeTinkoffService"),
                Mockito.any(MarketProperties.class),
                Mockito.any(TinkoffServices.class),
                Mockito.any(FakeContext.class),
                Mockito.anyDouble())
        ).thenAnswer(invocation -> {
            final MarketProperties marketProperties = invocation.getArgument(1);
            final TinkoffServices tinkoffServices = invocation.getArgument(2);
            final FakeContext fakeContext = invocation.getArgument(3);
            final double commission = invocation.getArgument(4);
            return new FakeTinkoffService(marketProperties, tinkoffServices, fakeContext, commission);
        });
    }

}