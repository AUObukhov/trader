package ru.obukhov.trader.trading.bots;

import org.junit.jupiter.api.Assertions;
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
import ru.obukhov.trader.common.exception.InstrumentNotFoundException;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class FakeBotFactoryUnitTest {

    private static final OffsetTime START_TIME = DateTimeTestData.newTime(7, 0, 0);
    private static final OffsetTime END_TIME = DateTimeTestData.newTime(19, 0, 0);

    @Mock
    private TradingStrategyFactory strategyFactory;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private ExtInstrumentsService extInstrumentsService;
    @Mock
    private ExtMarketDataService extMarketDataService;

    @InjectMocks
    private FakeBotFactory factory;

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateBot_movesCurrentTimestampToCeilingWorkTime() {
        return Stream.of(
//                Arguments.of(
//                        DateTimeTestData.newDateTime(2020, 10, 5, 12),
//                        DateTimeTestData.newDateTime(2020, 10, 5, 12)
//                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 19),
                        DateTimeTestData.newDateTime(2020, 10, 6, START_TIME.getHour())
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 19, 20),
                        DateTimeTestData.newDateTime(2020, 10, 6, START_TIME.getHour())
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 9, 19), // friday
                        DateTimeTestData.newDateTime(2020, 10, 12, START_TIME.getHour()) // monday
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 10, 12), // saturday
                        DateTimeTestData.newDateTime(2020, 10, 12, START_TIME.getHour()) // monday
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 9, 6, 59, 59, 999999999),
                        DateTimeTestData.newDateTime(2020, 10, 9, START_TIME.getHour())
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forCreateBot_movesCurrentTimestampToCeilingWorkTime")
    void createBot_movesCurrentTimestampToCeilingWorkTime(final OffsetDateTime currentDateTime, final OffsetDateTime expectedCurrentDateTime) {
        final String accountId = TestAccounts.TINKOFF.getId();

        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final List<String> figies = List.of(figi1, figi2);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BigDecimal commission = DecimalUtils.setDefaultScale(0.003);
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, commission, StrategyType.CONSERVATIVE, Collections.emptyMap());

        final Map<String, BigDecimal> initialBalances = TestData.newDecimalMap(share1.currency(), 0.0, share2.currency(), 0.0);
        final Map<String, BigDecimal> balanceIncrements = TestData.newDecimalMap(share1.currency(), 0.0, share2.currency(), 0.0);
        final BalanceConfig balanceConfig = TestData.newBalanceConfig(initialBalances, balanceIncrements);

        Mockito.when(extInstrumentsService.getShares(figies)).thenReturn(List.of(share1, share2));

        mockStrategy(botConfig);
        mockFakeContext();
        Mocker.mockTradingSchedules(extInstrumentsService, figies, START_TIME, END_TIME);

        final FakeBot bot = factory.createBot(botConfig, balanceConfig, currentDateTime);

        Assertions.assertEquals(expectedCurrentDateTime, bot.getCurrentDateTime());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateBot_initializesBalance() throws ParseException {
        final int initialBalanceUsd = 1000000;
        final int initialBalanceRub = 10000;
        final Map<String, BigDecimal> initialBalances = TestData.newDecimalMap(
                Currencies.USD, initialBalanceUsd,
                Currencies.RUB, initialBalanceRub
        );

        final int balanceIncrementUsd = 1000;
        final int balanceIncrementRub = 5000;
        final Map<String, BigDecimal> balanceIncrements = TestData.newDecimalMap(
                Currencies.USD, balanceIncrementUsd,
                Currencies.RUB, balanceIncrementRub
        );

        return Stream.of(
                Arguments.of(
                        TestData.newBalanceConfig(initialBalances, balanceIncrements, "0 0 0 1 * ?"),
                        DateTimeTestData.newDateTime(2023, 9, 1),
                        TestData.newDecimalMap(
                                Currencies.USD, initialBalanceUsd + balanceIncrementUsd,
                                Currencies.RUB, initialBalanceRub + balanceIncrementRub
                        )
                ),
                Arguments.of(
                        TestData.newBalanceConfig(initialBalances, balanceIncrements, "0 0 0 1 * ?"),
                        DateTimeTestData.newDateTime(2023, 9, 1, 7),
                        TestData.newDecimalMap(Currencies.USD, initialBalanceUsd, Currencies.RUB, initialBalanceRub)
                ),
                Arguments.of(
                        TestData.newBalanceConfig(initialBalances, balanceIncrements, "0 0 0 2 * ?"),
                        DateTimeTestData.newDateTime(2023, 9, 1),
                        TestData.newDecimalMap(Currencies.USD, initialBalanceUsd, Currencies.RUB, initialBalanceRub)
                ),
                Arguments.of(
                        TestData.newBalanceConfig(initialBalances, balanceIncrements),
                        DateTimeTestData.newDateTime(2023, 9, 1),
                        TestData.newDecimalMap(Currencies.USD, initialBalanceUsd, Currencies.RUB, initialBalanceRub)
                )
        );
    }

    @ParameterizedTest
    @MethodSource(value = "getData_forCreateBot_initializesBalance")
    void createBot_initializesBalance(
            final BalanceConfig balanceConfig,
            final OffsetDateTime currentDateTime,
            final Map<String, BigDecimal> expectedBalances
    ) {
        final String accountId = TestAccounts.TINKOFF.getId();

        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final String currency1 = share1.currency();
        final String currency2 = share2.currency();

        final List<String> figies = List.of(figi1, figi2);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BigDecimal commission = DecimalUtils.setDefaultScale(0.003);
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, commission, StrategyType.CONSERVATIVE, Collections.emptyMap());

        Mockito.when(extInstrumentsService.getShares(figies)).thenReturn(List.of(share1, share2));
        mockStrategy(botConfig);
        mockFakeContext();
        Mocker.mockTradingSchedules(extInstrumentsService, figies, START_TIME, END_TIME);

        final FakeBot bot = factory.createBot(botConfig, balanceConfig, currentDateTime);

        AssertUtils.assertEquals(expectedBalances.get(currency1), bot.getCurrentBalance(botConfig.accountId(), currency1));
        AssertUtils.assertEquals(expectedBalances.get(currency2), bot.getCurrentBalance(botConfig.accountId(), currency2));
    }

    @Test
    void createBot_throwsInstrumentNotFoundException_whenShareNotFound() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final List<String> figies = List.of(figi1, figi2);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BigDecimal commission = DecimalUtils.setDefaultScale(0.003);
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, commission, StrategyType.CONSERVATIVE, Collections.emptyMap());

        final BalanceConfig balanceConfig = TestData.newBalanceConfig();
        final OffsetDateTime currentDateTime = DateUtils.now();

        Mocker.mockTradingSchedules(extInstrumentsService, figies, START_TIME, END_TIME);
        Mockito.when(extInstrumentsService.getShares(figies)).thenReturn(List.of(share1));

        final Executable executable = () -> factory.createBot(botConfig, balanceConfig, currentDateTime);
        final String expectedMessage = "Instruments not found for ids [" + figi2 + "]";
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, executable, expectedMessage);
    }

    private void mockStrategy(final BotConfig botConfig) {
        final ConservativeStrategy conservativeStrategy = new ConservativeStrategy(StrategyType.CONSERVATIVE.name(), extMarketDataService);
        Mockito.when(strategyFactory.createStrategy(botConfig)).thenReturn(conservativeStrategy);
    }

    private void mockFakeContext() {
        Mockito.when(applicationContext.getBean(
                Mockito.eq("fakeContext"),
                Mockito.anyString(),
                Mockito.any(OffsetDateTime.class),
                Mockito.any(Map.class)
        )).thenAnswer(invocation -> {
            final String accountId = invocation.getArgument(1);
            final OffsetDateTime currentDateTime = invocation.getArgument(2);
            final Map<String, BigDecimal> initialBalances = invocation.getArgument(3);
            return new FakeContext(accountId, currentDateTime, initialBalances);
        });
    }

}