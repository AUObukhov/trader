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
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Collections;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class FakeBotFactoryUnitTest {

    private static final OffsetTime START_TIME = DateTimeTestData.createTime(7, 0, 0);
    private static final OffsetTime END_TIME = DateTimeTestData.createTime(19, 0, 0);

    @Mock
    private TradingStrategyFactory strategyFactory;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private ExtInstrumentsService extInstrumentsService;

    @InjectMocks
    private FakeBotFactory factory;

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateBot_movesCurrentTimestampToCeilingWorkTime() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 12),
                        DateTimeTestData.createDateTime(2020, 10, 5, 12)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 19),
                        DateTimeTestData.createDateTime(2020, 10, 6, START_TIME.getHour())
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 19, 20),
                        DateTimeTestData.createDateTime(2020, 10, 6, START_TIME.getHour())
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 9, 19), // friday
                        DateTimeTestData.createDateTime(2020, 10, 12, START_TIME.getHour()) // monday
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 10, 12), // saturday
                        DateTimeTestData.createDateTime(2020, 10, 12, START_TIME.getHour()) // monday
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 9, 6, 59, 59, 999999999),
                        DateTimeTestData.createDateTime(2020, 10, 9, START_TIME.getHour())
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forCreateBot_movesCurrentTimestampToCeilingWorkTime")
    void createBot_movesCurrentTimestampToCeilingWorkTime(final OffsetDateTime currentDateTime, final OffsetDateTime expectedCurrentDateTime) {
        final Share share = TestShares.APPLE.share();
        final String figi = share.figi();
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                StrategyType.CONSERVATIVE,
                Collections.emptyMap()
        );

        final BalanceConfig balanceConfig = new BalanceConfig();

        mockCurrency(figi, share.currency());
        Mockito.when(strategyFactory.createStrategy(botConfig)).thenReturn(TestData.CONSERVATIVE_STRATEGY);
        mockFakeContext();
        Mocker.mockTradingSchedule(extInstrumentsService, figi, START_TIME, END_TIME);

        final FakeBot bot = factory.createBot(botConfig, balanceConfig, currentDateTime);

        Assertions.assertEquals(expectedCurrentDateTime, bot.getCurrentDateTime());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateBot_initializesBalance() {
        return Stream.of(
                Arguments.of(TestData.newBalanceConfig(1000000.0, 1000.0, "0 0 0 1 * ?"), 1001000.0),
                Arguments.of(TestData.newBalanceConfig(1000000.0, 1000.0, "0 0 0 2 * ?"), 1000000.0),
                Arguments.of(TestData.newBalanceConfig(1000000.0), 1000000.0)
        );
    }

    @ParameterizedTest
    @MethodSource(value = "getData_forCreateBot_initializesBalance")
    void createBot_initializesBalance(final BalanceConfig balanceConfig, final double expectedBalance) {
        final Share share = TestShares.APPLE.share();
        final String figi = share.figi();
        final String currency = share.currency();
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                StrategyType.CONSERVATIVE,
                Collections.emptyMap()
        );

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 10, 1);

        mockCurrency(figi, currency);
        Mockito.when(strategyFactory.createStrategy(botConfig)).thenReturn(TestData.CONSERVATIVE_STRATEGY);
        mockFakeContext();
        Mocker.mockTradingSchedule(extInstrumentsService, figi, START_TIME, END_TIME);

        final FakeBot bot = factory.createBot(botConfig, balanceConfig, currentDateTime);

        AssertUtils.assertEquals(expectedBalance, bot.getCurrentBalance(botConfig.accountId(), currency));
    }

    @Test
    void createBot_throwsIllegalArgumentException_whenShareNotFound() {
        final String figi = TestShares.APPLE.share().figi();
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                StrategyType.CONSERVATIVE,
                Collections.emptyMap()
        );
        final BalanceConfig balanceConfig = TestData.newBalanceConfig(1000000.0);
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        final Executable executable = () -> factory.createBot(botConfig, balanceConfig, currentDateTime);
        final String expectedMessage = "Instrument not found for id " + figi;
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, executable, expectedMessage);
    }

    @SuppressWarnings("SameParameterValue")
    private void mockCurrency(final String figi, final String currency) {
        final Share share = Share.builder().figi(figi).currency(currency).build();
        Mockito.when(extInstrumentsService.getShare(figi)).thenReturn(share);
    }

    private void mockFakeContext() {
        Mockito.when(applicationContext.getBean(
                Mockito.eq("fakeContext"),
                Mockito.any(OffsetDateTime.class),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(BigDecimal.class)
        )).thenAnswer(invocation -> {
            final OffsetDateTime currentDateTime = invocation.getArgument(1);
            final String accountId = invocation.getArgument(2);
            final String currency = invocation.getArgument(3);
            final BigDecimal initialBalance = invocation.getArgument(4);
            return new FakeContext(currentDateTime, accountId, currency, initialBalance);
        });
    }

}