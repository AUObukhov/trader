package ru.obukhov.trader.trading.bots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
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
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class FakeBotFactoryUnitTest {

    @Mock
    private MarketProperties marketProperties;
    @Mock
    private TradingStrategyFactory strategyFactory;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private ExtInstrumentsService extInstrumentsService;

    @InjectMocks
    private FakeBotFactory factory;

    @BeforeEach
    void setUp() {
        Mocker.mockWorkSchedule(marketProperties);
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
        final String ticker = TestShare1.TICKER;
        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                StrategyType.CONSERVATIVE,
                Collections.emptyMap()
        );

        final BalanceConfig balanceConfig = new BalanceConfig();

        mockCurrency(ticker, TestShare1.CURRENCY);
        Mockito.when(strategyFactory.createStrategy(botConfig)).thenReturn(TestData.CONSERVATIVE_STRATEGY);
        mockFakeContext();

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
        final String ticker = TestShare1.TICKER;
        final Currency currency = TestShare1.CURRENCY;
        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                StrategyType.CONSERVATIVE,
                Collections.emptyMap()
        );

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 10, 1);

        mockCurrency(ticker, currency);
        Mockito.when(strategyFactory.createStrategy(botConfig)).thenReturn(TestData.CONSERVATIVE_STRATEGY);
        mockFakeContext();

        final FakeBot bot = factory.createBot(botConfig, balanceConfig, currentDateTime);

        AssertUtils.assertEquals(expectedBalance, bot.getCurrentBalance(botConfig.accountId(), currency));
    }

    @Test
    void createBot_throwIllegalArgumentException_whenShareNotFound() {
        final String ticker = TestShare1.TICKER;
        final BotConfig botConfig = new BotConfig(
                TestData.ACCOUNT_ID1,
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                StrategyType.CONSERVATIVE,
                Collections.emptyMap()
        );
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(1000000.0);
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        final Executable executable = () -> factory.createBot(botConfig, balanceConfig, currentDateTime);
        final String expectedMessage = "Not found share for ticker '" + ticker + "'";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    private void mockCurrency(final String ticker, final Currency currency) {
        final Share share = Share.builder().ticker(ticker).currency(currency).build();
        Mockito.when(extInstrumentsService.getSingleShare(ticker)).thenReturn(share);
    }

    private void mockFakeContext() {
        Mockito.when(applicationContext.getBean(
                Mockito.eq("fakeContext"),
                Mockito.any(MarketProperties.class),
                Mockito.any(OffsetDateTime.class),
                Mockito.any(String.class),
                Mockito.any(Currency.class),
                Mockito.any(BigDecimal.class)
        )).thenAnswer(invocation -> {
            final MarketProperties marketProperties = invocation.getArgument(1);
            final OffsetDateTime currentDateTime = invocation.getArgument(2);
            final String accountId = invocation.getArgument(3);
            final Currency currency = invocation.getArgument(4);
            final BigDecimal initialBalance = invocation.getArgument(5);
            return new FakeContext(marketProperties, currentDateTime, accountId, currency, initialBalance);
        });
    }

}