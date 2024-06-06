package ru.obukhov.trader.trading.backtest.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.config.properties.BackTestProperties;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.bots.FakeBot;
import ru.obukhov.trader.trading.bots.FakeBotFactory;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.trading.model.Balances;
import ru.obukhov.trader.trading.model.Profits;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SequencedMap;
import java.util.SortedMap;
import java.util.TreeMap;

@ExtendWith(MockitoExtension.class)
class BackTesterImplUnitTest {

    private static final String DATE_TIME_REGEX_PATTERN = "[\\d\\-\\+\\.:T]+";

    private static final String BALANCE_INCREMENT_CRON = "0 0 * * * ?";
    private static final BackTestProperties BACK_TEST_PROPERTIES = new BackTestProperties(2);

    @Mock
    private ExcelService excelService;
    @Mock
    private ExtInstrumentsService extInstrumentsService;
    @Mock
    private ExtMarketDataService extMarketDataService;
    @Mock
    private FakeBotFactory fakeBotFactory;

    private BackTesterImpl backTester;

    @BeforeEach
    void setUp() {
        backTester = new BackTesterImpl(excelService, extInstrumentsService, extMarketDataService, fakeBotFactory, BACK_TEST_PROPERTIES);
    }

    @Test
    void test_throwsIllegalArgumentException_whenFromIsInFuture() {
        final BalanceConfig balanceConfig = TestData.newBalanceConfig(Currencies.RUB, 10000.0, 1000.0);

        final List<BotConfig> botConfigs = Collections.emptyList();

        final OffsetDateTime from = DateUtils.now().plusDays(1);
        final OffsetDateTime to = from.plusDays(1);
        final Interval interval = Interval.of(from, to);

        final String expectedMessagePattern = String.format("^'from' \\(%1$s\\) can't be in future. Now is %1$s$", DATE_TIME_REGEX_PATTERN);

        final Executable executable = () -> backTester.test(botConfigs, balanceConfig, interval, false);
        AssertUtils.assertThrowsWithMessagePattern(IllegalArgumentException.class, executable, expectedMessagePattern);
    }

    @Test
    void test_throwsIllegalArgumentException_whenToIsInFuture() {
        final BalanceConfig balanceConfig = TestData.newBalanceConfig(Currencies.RUB, 10000.0, 1000.0);

        final List<BotConfig> botConfigs = Collections.emptyList();

        final OffsetDateTime from = DateUtils.now().minusDays(1);
        final OffsetDateTime to = from.plusDays(2);
        final Interval interval = Interval.of(from, to);

        final String expectedMessagePattern = String.format("^'to' \\(%1$s\\) can't be in future. Now is %1$s$", DATE_TIME_REGEX_PATTERN);

        final Executable executable = () -> backTester.test(botConfigs, balanceConfig, interval, false);
        AssertUtils.assertThrowsWithMessagePattern(RuntimeException.class, executable, expectedMessagePattern);
    }

    @Test
    void test_throwsIllegalArgumentException_whenToIntervalIsShorterThanOneDay() {
        final BalanceConfig balanceConfig = TestData.newBalanceConfig(Currencies.RUB, 10000.0, 1000.0);

        final List<BotConfig> botConfigs = Collections.emptyList();

        final OffsetDateTime from = DateUtils.now().minusDays(1);
        final OffsetDateTime to = from.plusDays(1).minusNanos(1);
        final Interval interval = Interval.of(from, to);

        final Executable executable = () -> backTester.test(botConfigs, balanceConfig, interval, false);
        AssertUtils.assertThrowsWithMessage(RuntimeException.class, executable, "interval can't be shorter than 1 day");
    }

    @Test
    void test_returnsResultWithEmptyValues_whenBotConfigProcessingThrowsException() {
        // arrange

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final String accountId = TestAccounts.TINKOFF.getId();
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.getFigi();
        final String currency = testShare.getCurrency();

        Mocker.mockInstrument(extInstrumentsService, testShare.instrument());

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BigDecimal commission = DecimalUtils.setDefaultScale(0.003);
        final StrategyType strategyType = StrategyType.CONSERVATIVE;
        final BotConfig botConfig = new BotConfig(accountId, List.of(figi), candleInterval, commission, strategyType, Collections.emptyMap());

        final BalanceConfig balanceConfig = TestData.newBalanceConfig(currency, 10000.0, 1000.0);
        final List<BotConfig> botConfigs = List.of(botConfig);

        final FakeBot fakeBot = mockFakeBot(botConfig, balanceConfig, from);

        final String exceptionMessage = "exception message";
        Mockito.doThrow(new IllegalArgumentException(exceptionMessage))
                .when(fakeBot).processBotConfig(Mockito.eq(botConfig), Mockito.any(Interval.class));

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());

        final BackTestResult backTestResult = backTestResults.getFirst();

        Assertions.assertEquals(botConfigs.getFirst(), backTestResult.botConfig());
        Assertions.assertEquals(interval, backTestResult.interval());

        AssertUtils.assertEquals(1, backTestResult.balances().size());
        final Balances actualBalance = backTestResult.balances().get(currency);

        final BigDecimal expectedBalance = balanceConfig.getInitialBalances().get(currency);
        AssertUtils.assertEquals(expectedBalance, actualBalance.initialInvestment());
        AssertUtils.assertEquals(expectedBalance, actualBalance.totalInvestment());
        AssertUtils.assertEquals(0, actualBalance.finalTotalSavings());
        AssertUtils.assertEquals(0, actualBalance.finalBalance());
        AssertUtils.assertEquals(expectedBalance, actualBalance.weightedAverageInvestment());

        Assertions.assertTrue(backTestResult.profits().isEmpty());

        final String expectedErrorPattern = String.format(
                Locale.US,
                "^Back test for 'BotConfig\\{accountId=%s, figies=\\[%s\\], candleInterval=%s, commission=%s, strategyType=%s, " +
                        "strategyParams=\\{\\}\\}' failed within 00:00:00.\\d\\d\\d with error: %s$",
                accountId, figi, candleInterval, commission, strategyType, exceptionMessage
        );
        AssertUtils.assertMatchesRegex(backTestResult.error(), expectedErrorPattern);
    }

    @Test
    void test_fillsCommonStatistics() {

        // arrange

        final TestShare testShare1 = TestShares.APPLE;
        final TestShare testShare2 = TestShares.SBER;

        final String currency1 = testShare1.getCurrency();
        final String currency2 = testShare2.getCurrency();

        Mocker.mockInstrument(extInstrumentsService, testShare1.instrument());
        Mocker.mockInstrument(extInstrumentsService, testShare2.instrument());

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BigDecimal initialInvestmentDecimal = DecimalUtils.setDefaultScale(10000);
        final BigDecimal balanceIncrement = DecimalUtils.setDefaultScale(1000);
        final Map<String, BigDecimal> initialBalances = Map.of(currency1, initialInvestmentDecimal, currency2, initialInvestmentDecimal);
        final Map<String, BigDecimal> balanceIncrements = Map.of(currency1, balanceIncrement, currency2, balanceIncrement);
        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, balanceIncrements, TestData.newCronExpression(BALANCE_INCREMENT_CRON));

        final BigDecimal finalBalance1 = DecimalUtils.setDefaultScale(2000);
        final int finalQuantity1 = 8;

        final SequencedMap<OffsetDateTime, Double> prices1 = new LinkedHashMap<>();
        prices1.put(from.plusMinutes(10), 1000.0);
        prices1.put(from.plusMinutes(20), 1001.0);
        prices1.put(from.plusMinutes(30), 1002.0);
        prices1.put(from.plusMinutes(40), 1003.0);
        final double finalPrice1 = 1004.0;
        prices1.put(from.plusMinutes(50), finalPrice1);

        final BotConfig botConfig1 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare1.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(2000),
                finalQuantity1,
                prices1,
                finalPrice1,
                null
        );

        final BigDecimal finalBalance2 = DecimalUtils.setDefaultScale(100);
        final int finalQuantity2 = 500;

        final SequencedMap<OffsetDateTime, Double> prices2 = new LinkedHashMap<>();
        prices2.put(from.plusMinutes(100), 100.0);
        prices2.put(from.plusMinutes(200), 100.1);
        prices2.put(from.plusMinutes(300), 100.2);
        prices2.put(from.plusMinutes(400), 100.3);
        final double finalPrice2 = 100.4;
        prices2.put(from.plusMinutes(500), finalPrice2);

        final BotConfig botConfig2 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare2.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.001,
                balanceConfig,
                interval,
                finalBalance2,
                finalQuantity2,
                prices2,
                finalPrice2,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        assertCommonStatistics(
                backTestResults.getFirst(), botConfigs.getFirst(), currency1,
                interval, initialInvestment,
                finalPrice1, finalQuantity1 * testShare1.getLot(), finalBalance1,
                0.0032, 2.212128816
        );

        assertCommonStatistics(
                backTestResults.get(1), botConfigs.get(1), currency2,
                interval, initialInvestment,
                finalPrice2, finalQuantity2, finalBalance2,
                4.03, 1.768913277123785E256
        );
    }

    @SuppressWarnings("SameParameterValue")
    private void assertCommonStatistics(
            final BackTestResult backTestResult,
            final BotConfig botConfig,
            final String currency,
            final Interval interval,
            final double initialInvestment,
            final Double currentPrice,
            final int positionQuantity,
            final BigDecimal currentBalance,
            final double expectedRelativeProfit,
            final double expectedAnnualProfit
    ) {
        Assertions.assertNull(backTestResult.error());

        Assertions.assertEquals(botConfig, backTestResult.botConfig());
        Assertions.assertEquals(interval, backTestResult.interval());

        Assertions.assertEquals(1, backTestResult.balances().size());

        final Balances balances = backTestResult.balances().get(currency);
        AssertUtils.assertEquals(initialInvestment, balances.initialInvestment());
        AssertUtils.assertEquals(initialInvestment, balances.totalInvestment());

        final BigDecimal positionsPrice = DecimalUtils.setDefaultScale(currentPrice * positionQuantity);
        final BigDecimal expectedFinalTotalSavings = positionsPrice.add(currentBalance);
        AssertUtils.assertEquals(expectedFinalTotalSavings, balances.finalTotalSavings());

        AssertUtils.assertEquals(currentBalance, balances.finalBalance());
        AssertUtils.assertEquals(initialInvestment, balances.weightedAverageInvestment());

        final BigDecimal expectedAbsoluteProfit = DecimalUtils.subtract(currentBalance, initialInvestment).add(positionsPrice);
        Assertions.assertEquals(1, backTestResult.profits().size());
        final Profits profits = backTestResult.profits().get(currency);
        AssertUtils.assertEquals(expectedAbsoluteProfit, profits.absolute());
        AssertUtils.assertEquals(expectedRelativeProfit, profits.relative());
        AssertUtils.assertEquals(expectedAnnualProfit, profits.relativeAnnual());
    }

    @Test
    void test_callsAddInvestment() {
        // arrange

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final String accountId1 = TestAccounts.TINKOFF.getId();
        final TestShare testShare1 = TestShares.APPLE;
        final String figi1 = testShare1.getFigi();
        final String currency1 = testShare1.getCurrency();
        final BigDecimal commission1 = DecimalUtils.setDefaultScale(0.003);

        final String accountId2 = TestAccounts.IIS.getId();
        final TestShare testShare2 = TestShares.SBER;
        final String figi2 = testShare2.getFigi();
        final String currency2 = testShare2.getCurrency();
        final BigDecimal commission2 = DecimalUtils.setDefaultScale(0.001);

        final BigDecimal initialInvestment = DecimalUtils.setDefaultScale(10000);
        final BigDecimal balanceIncrement = DecimalUtils.setDefaultScale(1000);
        final Map<String, BigDecimal> initialBalances = Map.of(currency1, initialInvestment, currency2, initialInvestment);
        final Map<String, BigDecimal> balanceIncrements = Map.of(currency1, balanceIncrement, currency2, balanceIncrement);
        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, balanceIncrements, TestData.newCronExpression(BALANCE_INCREMENT_CRON));

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final double currentPrice1 = 500;
        final double currentPrice2 = 50;

        final SequencedMap<OffsetDateTime, Double> prices1 = new LinkedHashMap<>();
        prices1.put(from.plusMinutes(10), 100.0);
        prices1.put(from.plusMinutes(20), 200.0);
        prices1.put(from.plusMinutes(30), 300.0);
        prices1.put(from.plusMinutes(40), 400.0);
        prices1.put(from.plusMinutes(50), currentPrice1);

        final SequencedMap<OffsetDateTime, Double> prices2 = new LinkedHashMap<>();
        prices2.put(from.plusMinutes(100), 10.0);
        prices2.put(from.plusMinutes(200), 20.0);
        prices2.put(from.plusMinutes(300), 30.0);
        prices2.put(from.plusMinutes(400), 40.0);
        prices2.put(from.plusMinutes(500), currentPrice2);

        final int quantity1 = 1;
        final int quantity2 = 20;

        final BigDecimal currentBalance1 = DecimalUtils.setDefaultScale(2000);
        final BigDecimal currentBalance2 = DecimalUtils.setDefaultScale(2000);

        final BotConfig botConfig1 = new BotConfig(accountId1, List.of(figi1), candleInterval, commission1, null, null);
        final BotConfig botConfig2 = new BotConfig(accountId2, List.of(figi2), candleInterval, commission2, null, null);

        final FakeBot fakeBot1 = mockFakeBot(botConfig1, balanceConfig, from);
        final FakeBot fakeBot2 = mockFakeBot(botConfig2, balanceConfig, from);

        Mocker.mockInstrument(extInstrumentsService, testShare1.instrument());
        Mockito.when(extInstrumentsService.getShare(figi1)).thenReturn(testShare1.share());
        mockBotCandles(botConfig1, fakeBot1, prices1);
        mockCurrentPrice(fakeBot1, figi1, currentPrice1);
        mockPlusMinuteScheduled(fakeBot1, from);
        mockInvestments(fakeBot1, accountId1, from, currency1, initialInvestment);
        Mockito.when(fakeBot1.getCurrentBalance(accountId1, currency1)).thenReturn(currentBalance1);
        mockPortfolioPosition(fakeBot1, accountId1, figi1, currentPrice1, quantity1);

        Mocker.mockInstrument(extInstrumentsService, testShare2.instrument());
        Mockito.when(extInstrumentsService.getShare(figi2)).thenReturn(testShare2.share());
        mockBotCandles(botConfig2, fakeBot2, prices2);
        mockCurrentPrice(fakeBot2, figi2, currentPrice2);
        mockPlusMinuteScheduled(fakeBot2, from);
        mockInvestments(fakeBot2, accountId2, from, currency2, initialInvestment);
        Mockito.when(fakeBot2.getCurrentBalance(accountId2, currency2)).thenReturn(currentBalance2);
        mockPortfolioPosition(fakeBot2, accountId2, figi2, currentPrice2, quantity2);

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        Assertions.assertNull(backTestResults.getFirst().error());
        Assertions.assertNull(backTestResults.get(1).error());

        final Map<String, BigDecimal> investments = Map.of(currency1, balanceIncrement, currency2, balanceIncrement);
        Mockito.verify(fakeBot1, Mockito.times(24))
                .addInvestments(
                        Mockito.eq(accountId1),
                        Mockito.any(OffsetDateTime.class),
                        Mockito.eq(investments)
                );

        Mockito.verify(fakeBot2, Mockito.times(24))
                .addInvestments(
                        Mockito.eq(accountId2),
                        Mockito.any(OffsetDateTime.class),
                        Mockito.eq(investments)
                );
    }

    @Test
    void test_fillsPositions() {
        // arrange

        final TestShare testShare1 = TestShares.APPLE;
        final TestShare testShare2 = TestShares.SBER;

        final String currency1 = testShare1.getCurrency();
        final String currency2 = testShare2.getCurrency();

        Mocker.mockInstrument(extInstrumentsService, testShare1.instrument());
        Mocker.mockInstrument(extInstrumentsService, testShare2.instrument());

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final BigDecimal initialInvestment = DecimalUtils.setDefaultScale(10000);
        final BigDecimal balanceIncrement = DecimalUtils.setDefaultScale(1000);

        final Map<String, BigDecimal> initialBalances = Map.of(currency1, initialInvestment, currency2, initialInvestment);
        final Map<String, BigDecimal> balanceIncrements = Map.of(currency1, balanceIncrement, currency2, balanceIncrement);
        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, balanceIncrements, TestData.newCronExpression(BALANCE_INCREMENT_CRON));

        final SequencedMap<OffsetDateTime, Double> prices1 = new LinkedHashMap<>();
        prices1.put(from.plusMinutes(10), 100.0);
        prices1.put(from.plusMinutes(20), 200.0);
        prices1.put(from.plusMinutes(30), 300.0);
        prices1.put(from.plusMinutes(40), 400.0);
        final double currentPrice1 = 500.0;
        prices1.put(from.plusMinutes(50), currentPrice1);

        final int quantity1 = 2;

        final BotConfig botConfig1 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare1.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(20000L),
                quantity1,
                prices1,
                currentPrice1,
                null
        );

        final int quantity2 = 10;
        final double currentPrice2 = 5000.0;
        final BotConfig botConfig2 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare2.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.001,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(10000L),
                quantity2,
                Collections.emptyNavigableMap(),
                currentPrice2,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());
        assertPosition(backTestResults.getFirst(), testShare1.getFigi(), currentPrice1, quantity1);
        assertPosition(backTestResults.get(1), testShare2.getFigi(), currentPrice2, quantity2);
    }

    private void assertPosition(final BackTestResult backTestResult, final String figi, final double currentPrice, final int quantity) {
        Assertions.assertNull(backTestResult.error());
        final List<Position> positions = backTestResult.positions();
        Assertions.assertEquals(1, positions.size());
        final Position backTestPosition = positions.getFirst();
        Assertions.assertEquals(figi, backTestPosition.getFigi());
        AssertUtils.assertEquals(currentPrice, backTestPosition.getCurrentPrice().getValue());
        AssertUtils.assertEquals(quantity, backTestPosition.getQuantity());
    }

    @Test
    void test_fillsOperations() {
        // arrange

        final TestShare testShare1 = TestShares.APPLE;
        final TestShare testShare2 = TestShares.SBER;

        final String currency1 = testShare1.getCurrency();
        final String currency2 = testShare2.getCurrency();

        Mocker.mockInstrument(extInstrumentsService, testShare1.instrument());
        Mocker.mockInstrument(extInstrumentsService, testShare2.instrument());

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final BigDecimal initialInvestment = DecimalUtils.setDefaultScale(10000);
        final BigDecimal balanceIncrement = DecimalUtils.setDefaultScale(1000);
        final Map<String, BigDecimal> initialBalances = Map.of(currency1, initialInvestment, currency2, initialInvestment);
        final Map<String, BigDecimal> balanceIncrements = Map.of(currency1, balanceIncrement, currency2, balanceIncrement);
        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, balanceIncrements, TestData.newCronExpression(BALANCE_INCREMENT_CRON));

        final String figi1 = testShare1.getFigi();
        final double commission1 = 0.003;

        final BigDecimal currentBalance1 = DecimalUtils.setDefaultScale(20000);

        final int quantity1 = 1;

        final OffsetDateTime operationDateTime1 = from.plusMinutes(2);
        final OperationType operationType1 = OperationType.OPERATION_TYPE_BUY;
        final double operationPrice1 = 100;
        final int operationQuantity1 = 2;
        final Operation operation1 = TestData.newOperation(operationDateTime1, operationType1, operationPrice1, operationQuantity1, figi1);

        final String figi2 = testShare2.getFigi();
        final double commission2 = 0.001;

        final BigDecimal currentBalance2 = DecimalUtils.setDefaultScale(10000);

        final int quantity2 = 10;

        final OffsetDateTime operationDateTime2 = from.plusMinutes(3);
        final OperationType operationType2 = OperationType.OPERATION_TYPE_SELL;
        final double operationPrice2 = 1000;
        final int operationQuantity2 = 4;
        final Operation operation2 = TestData.newOperation(operationDateTime2, operationType2, operationPrice2, operationQuantity2, figi2);

        final BotConfig botConfig1 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare1.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                commission1,
                balanceConfig,
                interval,
                currentBalance1,
                quantity1,
                new LinkedHashMap<>(Map.of(from.plusMinutes(1), 100.0)),
                100,
                operation1
        );

        final BotConfig botConfig2 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare2.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                commission2,
                balanceConfig,
                interval,
                currentBalance2,
                quantity2,
                new LinkedHashMap<>(Map.of(from.plusMinutes(3), 1000.0)),
                1000,
                operation2
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        assertOperation(backTestResults.getFirst(), figi1, operationDateTime1, operationType1, operationPrice1, operationQuantity1);
        assertOperation(backTestResults.get(1), figi2, operationDateTime2, operationType2, operationPrice2, operationQuantity2);
    }

    private void assertOperation(
            final BackTestResult backTestResult,
            final String figi,
            final OffsetDateTime expectedDateTime,
            final OperationType expectedOperationType,
            final double expectedOperationPrice,
            final int expectedOperationQuantity
    ) {
        Assertions.assertNull(backTestResult.error());

        final Map<String, List<Operation>> resultOperations = backTestResult.operations();
        Assertions.assertEquals(1, resultOperations.size());

        final List<Operation> operations = resultOperations.get(figi);
        final Operation backTestOperation = operations.getFirst();
        Assertions.assertEquals(figi, backTestOperation.getFigi());
        Assertions.assertEquals(expectedDateTime, TimestampUtils.toOffsetDateTime(backTestOperation.getDate()));
        Assertions.assertEquals(expectedOperationType, backTestOperation.getOperationType());
        AssertUtils.assertEquals(expectedOperationPrice, backTestOperation.getPrice());
        AssertUtils.assertEquals(expectedOperationQuantity, backTestOperation.getQuantity());
    }

    @Test
    void test_fillsCandles() {
        // arrange

        final TestShare testShare1 = TestShares.APPLE;
        final TestShare testShare2 = TestShares.SBER;

        final String currency1 = testShare1.getCurrency();
        final String currency2 = testShare2.getCurrency();

        Mocker.mockInstrument(extInstrumentsService, testShare1.instrument());
        Mocker.mockInstrument(extInstrumentsService, testShare2.instrument());

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final BigDecimal initialInvestment = DecimalUtils.setDefaultScale(10000);
        final BigDecimal balanceIncrement = DecimalUtils.setDefaultScale(1000);
        final Map<String, BigDecimal> initialBalances = Map.of(currency1, initialInvestment, currency2, initialInvestment);
        final Map<String, BigDecimal> balanceIncrements = Map.of(currency1, balanceIncrement, currency2, balanceIncrement);
        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, balanceIncrements, TestData.newCronExpression(BALANCE_INCREMENT_CRON));

        final SequencedMap<OffsetDateTime, Double> prices1 = new LinkedHashMap<>();
        prices1.put(from.plusMinutes(1), 100.0);
        prices1.put(from.plusMinutes(2), 200.0);
        prices1.put(from.plusMinutes(3), 300.0);
        prices1.put(from.plusMinutes(4), 400.0);
        prices1.put(from.plusMinutes(5), 500.0);

        final BotConfig botConfig1 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare1.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(20000L),
                1,
                prices1,
                500,
                null
        );

        final SequencedMap<OffsetDateTime, Double> prices2 = new LinkedHashMap<>();
        prices2.put(from.plusMinutes(10), 1000.0);
        prices2.put(from.plusMinutes(20), 2000.0);
        prices2.put(from.plusMinutes(30), 3000.0);
        prices2.put(from.plusMinutes(40), 4000.0);

        final BotConfig botConfig2 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare2.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.001,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(10000L),
                2,
                prices2,
                4000,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());
        assertCandles(backTestResults.getFirst(), testShare1.getFigi(), prices1);
        assertCandles(backTestResults.get(1), testShare2.getFigi(), prices2);
    }

    private void assertCandles(final BackTestResult backTestResult, final String figi, final Map<OffsetDateTime, Double> prices) {
        Assertions.assertNull(backTestResult.error());

        final Map<String, List<Candle>> candlesMap = backTestResult.candles();
        final List<Candle> candles = candlesMap.get(figi);
        Assertions.assertEquals(prices.size(), candles.size());

        final Iterator<Candle> candlesIterator = candles.iterator();
        for (final Map.Entry<OffsetDateTime, Double> entry : prices.entrySet()) {
            final Candle candle = candlesIterator.next();
            Assertions.assertEquals(entry.getKey(), candle.getTime());
            AssertUtils.assertEquals(entry.getValue(), candle.getOpen());
        }
    }

    @Test
    void test_notFillsCandles_whenCurrentCandlesInDecisionDataIsAlwaysNullOrEmpty() {
        // arrange

        final TestShare testShare1 = TestShares.APPLE;
        final TestShare testShare2 = TestShares.SBER;

        final String currency1 = testShare1.getCurrency();
        final String currency2 = testShare2.getCurrency();

        Mocker.mockInstrument(extInstrumentsService, testShare1.instrument());
        Mocker.mockInstrument(extInstrumentsService, testShare2.instrument());

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final BigDecimal initialInvestment = DecimalUtils.setDefaultScale(10000);
        final BigDecimal balanceIncrement = DecimalUtils.setDefaultScale(1000);
        final Map<String, BigDecimal> initialBalances = Map.of(currency1, initialInvestment, currency2, initialInvestment);
        final Map<String, BigDecimal> balanceIncrements = Map.of(currency1, balanceIncrement, currency2, balanceIncrement);
        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, balanceIncrements, TestData.newCronExpression(BALANCE_INCREMENT_CRON));

        final BotConfig botConfig1 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare1.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(20000L),
                null,
                Collections.emptyNavigableMap(),
                300,
                null
        );

        final BotConfig botConfig2 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare2.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.001,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(10000L),
                null,
                Collections.emptyNavigableMap(),
                200,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        final BackTestResult backTestResult1 = backTestResults.getFirst();
        Assertions.assertNull(backTestResult1.error());
        Assertions.assertEquals(Map.of(testShare1.share().figi(), Collections.emptyList()), backTestResult1.candles());

        final BackTestResult backTestResult2 = backTestResults.get(1);
        Assertions.assertNull(backTestResult2.error());
        Assertions.assertEquals(Map.of(testShare2.share().figi(), Collections.emptyList()), backTestResult2.candles());
    }

    @Test
    void test_adjustsInterval_whenFirstCandleIsAfterFrom_andCandleIntervalIs1Min() {
        // arrange

        final TestShare testShare = TestShares.APPLE;

        Mocker.mockInstrument(extInstrumentsService, testShare.instrument());

        final OffsetDateTime from = testShare.getFirst1MinCandleDate().minusDays(1);
        final OffsetDateTime to = testShare.getFirst1MinCandleDate().plusDays(1);
        final Interval interval = Interval.of(from, to);
        final Interval effectiveInterval = Interval.of(testShare.getFirst1MinCandleDate(), to);

        final double initialInvestment = 10000;
        final double balanceIncrement = 1000;
        final String currency = testShare.getCurrency();
        final BalanceConfig balanceConfig = TestData.newBalanceConfig(currency, initialInvestment, balanceIncrement, BALANCE_INCREMENT_CRON);

        final SequencedMap<OffsetDateTime, Double> prices1 = new LinkedHashMap<>();
        prices1.put(from.plusMinutes(1), 100.0);
        prices1.put(from.plusMinutes(2), 200.0);
        prices1.put(from.plusMinutes(3), 300.0);
        prices1.put(from.plusMinutes(4), 400.0);
        prices1.put(from.plusMinutes(5), 500.0);

        final BotConfig botConfig1 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                balanceConfig,
                effectiveInterval,
                DecimalUtils.setDefaultScale(20000L),
                1,
                prices1,
                500,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());
        Assertions.assertNull(backTestResults.getFirst().error());

        final Interval expectedInterval = Interval.of(testShare.getFirst1MinCandleDate(), to);
        Assertions.assertEquals(expectedInterval, backTestResults.getFirst().interval());
    }

    @Test
    void test_adjustsInterval_whenFirstCandleIsAfterFrom_andCandleIntervalIs1Day() {
        // arrange

        final TestShare testShare = TestShares.APPLE;

        Mocker.mockInstrument(extInstrumentsService, testShare.instrument());

        final OffsetDateTime first1DayCandleMinimumDate = testShare.getFirst1DayCandleDate().minusHours(3);
        final OffsetDateTime from = first1DayCandleMinimumDate.minusDays(1);
        final OffsetDateTime to = first1DayCandleMinimumDate.plusDays(1);
        final Interval interval = Interval.of(from, to);
        final Interval effectiveInterval = Interval.of(first1DayCandleMinimumDate, to);

        final double initialInvestment = 10000;

        final String currency = testShare.getCurrency();
        final BalanceConfig balanceConfig = TestData.newBalanceConfig(currency, initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        final SequencedMap<OffsetDateTime, Double> prices1 = new LinkedHashMap<>();
        prices1.put(from.plusMinutes(1), 100.0);
        prices1.put(from.plusMinutes(2), 200.0);
        prices1.put(from.plusMinutes(3), 300.0);
        prices1.put(from.plusMinutes(4), 400.0);
        prices1.put(from.plusMinutes(5), 500.0);

        final BotConfig botConfig1 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare.share(),
                CandleInterval.CANDLE_INTERVAL_DAY,
                0.003,
                balanceConfig,
                effectiveInterval,
                DecimalUtils.setDefaultScale(20000L),
                1,
                prices1,
                500,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());
        Assertions.assertNull(backTestResults.getFirst().error());

        final Interval expectedInterval = Interval.of(first1DayCandleMinimumDate, to);
        Assertions.assertEquals(expectedInterval, backTestResults.getFirst().interval());
    }

    @Test
    void test_callsSaveToFile_whenSaveToFilesIsTrue() {

        // arrange

        final TestShare testShare1 = TestShares.APPLE;
        final TestShare testShare2 = TestShares.SBER;

        final String currency1 = testShare1.getCurrency();
        final String currency2 = testShare2.getCurrency();

        Mocker.mockInstrument(extInstrumentsService, testShare1.instrument());
        Mocker.mockInstrument(extInstrumentsService, testShare2.instrument());

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final BigDecimal initialInvestment = DecimalUtils.setDefaultScale(10000);
        final BigDecimal balanceIncrement = DecimalUtils.setDefaultScale(1000);
        final Map<String, BigDecimal> initialBalances = Map.of(currency1, initialInvestment, currency2, initialInvestment);
        final Map<String, BigDecimal> balanceIncrements = Map.of(currency1, balanceIncrement, currency2, balanceIncrement);
        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, balanceIncrements, TestData.newCronExpression(BALANCE_INCREMENT_CRON));

        final BigDecimal currentBalance = DecimalUtils.ZERO;

        final double commission1 = 0.001;
        final Operation operation = TestData.newOperation(
                from.plusMinutes(2),
                OperationType.OPERATION_TYPE_BUY,
                100,
                2,
                testShare1.getFigi()
        );

        final BotConfig botConfig1 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare1.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                commission1,
                balanceConfig,
                interval,
                currentBalance,
                1,
                new LinkedHashMap<>(Map.of(from.plusMinutes(1), 100.0)),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare2.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                balanceConfig,
                interval,
                currentBalance,
                null,
                Collections.emptyNavigableMap(),
                200,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, true);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        Assertions.assertNull(backTestResults.getFirst().error());
        Assertions.assertNull(backTestResults.get(1).error());

        Mockito.verify(excelService, Mockito.only()).saveBackTestResults(Mockito.anyCollection());
    }

    @Test
    void test_neverCallsSaveToFile_whenSaveToFilesIsFalse() {

        // arrange

        final TestShare testShare1 = TestShares.APPLE;
        final TestShare testShare2 = TestShares.SBER;

        final String currency1 = testShare1.getCurrency();
        final String currency2 = testShare2.getCurrency();

        Mocker.mockInstrument(extInstrumentsService, testShare1.instrument());
        Mocker.mockInstrument(extInstrumentsService, testShare2.instrument());

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final BigDecimal initialInvestment = DecimalUtils.setDefaultScale(10000);
        final BigDecimal balanceIncrement = DecimalUtils.setDefaultScale(1000);
        final Map<String, BigDecimal> initialBalances = Map.of(currency1, initialInvestment, currency2, initialInvestment);
        final Map<String, BigDecimal> balanceIncrements = Map.of(currency1, balanceIncrement, currency2, balanceIncrement);
        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, balanceIncrements, TestData.newCronExpression(BALANCE_INCREMENT_CRON));

        final BigDecimal currentBalance = DecimalUtils.ZERO;

        final double commission1 = 0.001;
        final Operation operation = TestData.newOperation(
                from.plusMinutes(2),
                OperationType.OPERATION_TYPE_BUY,
                100,
                2,
                testShare1.getFigi()
        );

        final BotConfig botConfig1 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare1.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                commission1,
                balanceConfig,
                interval,
                currentBalance,
                1,
                new LinkedHashMap<>(Map.of(from.plusMinutes(1), 100.0)),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare2.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                balanceConfig,
                interval,
                currentBalance,
                null,
                Collections.emptyNavigableMap(),
                150,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        Assertions.assertNull(backTestResults.getFirst().error());
        Assertions.assertNull(backTestResults.get(1).error());

        Mockito.verify(excelService, Mockito.never()).saveBackTestResults(Mockito.any());
    }

    @Test
    void test_returnsZeroInvestmentsAndProfits_whenNoInvestments() {
        // arrange

        final TestShare testShare1 = TestShares.APPLE;
        final TestShare testShare2 = TestShares.SBER;

        final String currency1 = testShare1.getCurrency();
        final String currency2 = testShare2.getCurrency();

        Mocker.mockInstrument(extInstrumentsService, testShare1.instrument());
        Mocker.mockInstrument(extInstrumentsService, testShare2.instrument());

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final BigDecimal currentBalance = DecimalUtils.setDefaultScale(20000);
        final BigDecimal initialInvestment = DecimalUtils.setDefaultScale(0);
        final BigDecimal balanceIncrement = DecimalUtils.setDefaultScale(1000);
        final Map<String, BigDecimal> initialBalances = Map.of(currency1, initialInvestment, currency2, initialInvestment);
        final Map<String, BigDecimal> balanceIncrements = Map.of(currency1, balanceIncrement, currency2, balanceIncrement);
        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, balanceIncrements, TestData.newCronExpression(BALANCE_INCREMENT_CRON));

        final double commission1 = 0.003;
        final Operation operation = TestData.newOperation(
                from.plusMinutes(2),
                OperationType.OPERATION_TYPE_BUY,
                100,
                2,
                testShare1.getFigi()
        );
        final BotConfig botConfig1 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare1.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                commission1,
                balanceConfig,
                interval,
                currentBalance,
                10,
                new LinkedHashMap<>(Map.of(from.plusMinutes(1), 100.0)),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                null,
                testShare2.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.001,
                balanceConfig,
                interval,
                currentBalance,
                null,
                Collections.emptyNavigableMap(),
                300,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());
        assertNoInvestmentsAndProfits(backTestResults.getFirst());
        assertNoInvestmentsAndProfits(backTestResults.get(1));
    }

    private void assertNoInvestmentsAndProfits(final BackTestResult backTestResult) {
        Assertions.assertNull(backTestResult.error());

        for (final Map.Entry<String, Balances> entry : backTestResult.balances().entrySet()) {
            AssertUtils.assertEquals(0, entry.getValue().totalInvestment());
            AssertUtils.assertEquals(0, entry.getValue().weightedAverageInvestment());
        }
        for (final Map.Entry<String, Profits> entry : backTestResult.profits().entrySet()) {
            AssertUtils.assertEquals(0, entry.getValue().relative());
            AssertUtils.assertEquals(0, entry.getValue().relativeAnnual());
        }
    }

    @Test
    void test_catchesBackTestException() {
        // arrange

        final TestShare testShare1 = TestShares.APPLE;
        final TestShare testShare2 = TestShares.SBER;

        final String currency1 = testShare1.getCurrency();
        final String currency2 = testShare2.getCurrency();

        Mocker.mockInstrument(extInstrumentsService, testShare1.instrument());
        Mocker.mockInstrument(extInstrumentsService, testShare2.instrument());

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final BigDecimal initialInvestment = DecimalUtils.setDefaultScale(1000);
        final BigDecimal balanceIncrement = DecimalUtils.setDefaultScale(10000);
        final Map<String, BigDecimal> initialBalances = Map.of(currency1, initialInvestment, currency2, initialInvestment);
        final Map<String, BigDecimal> balanceIncrements = Map.of(currency1, balanceIncrement, currency2, balanceIncrement);
        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, balanceIncrements, TestData.newCronExpression(BALANCE_INCREMENT_CRON));

        final String accountId1 = TestAccounts.TINKOFF.getId();
        final String figi1 = testShare1.getFigi();
        final CandleInterval candleInterval1 = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BigDecimal commission1 = DecimalUtils.setDefaultScale(0.003);
        final StrategyType strategyType1 = StrategyType.CONSERVATIVE;

        final BotConfig botConfig1 = new BotConfig(accountId1, List.of(figi1), candleInterval1, commission1, strategyType1, Collections.emptyMap());

        mockFakeBot(botConfig1, balanceConfig, from);

        final String mockedExceptionMessage1 = "mocked exception 1";
        Mockito.when(fakeBotFactory.createBot(botConfig1, balanceConfig, from))
                .thenThrow(new IllegalArgumentException(mockedExceptionMessage1));

        final String accountId2 = TestAccounts.IIS.getId();
        final String figi2 = testShare2.getFigi();
        final CandleInterval candleInterval2 = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BigDecimal commission2 = DecimalUtils.setDefaultScale(0.001);
        final StrategyType strategyType2 = StrategyType.CROSS;

        final BotConfig botConfig2 = new BotConfig(accountId2, List.of(figi2), candleInterval2, commission2, strategyType2, Collections.emptyMap());

        mockFakeBot(botConfig2, balanceConfig, from);

        final String mockedExceptionMessage2 = "mocked exception 2";
        Mockito.when(fakeBotFactory.createBot(botConfig2, balanceConfig, from))
                .thenThrow(new IllegalArgumentException(mockedExceptionMessage2));

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        final String expectedErrorPattern1 = String.format(
                "^Back test for " +
                        "'BotConfig\\{accountId=%s, figies=\\[%s\\], candleInterval=%s, commission=%s, strategyType=%s, strategyParams=\\{\\}\\}' " +
                        "failed within 00:00:00.\\d\\d\\d with error: %s$",
                accountId1, figi1, candleInterval1, commission1, strategyType1, mockedExceptionMessage1
        );
        AssertUtils.assertMatchesRegex(backTestResults.getFirst().error(), expectedErrorPattern1);

        final String expectedErrorPattern2 = String.format(
                "^Back test for " +
                        "'BotConfig\\{accountId=%s, figies=\\[%s\\], candleInterval=%s, commission=%s, strategyType=%s, strategyParams=\\{\\}\\}' " +
                        "failed within 00:00:00.\\d\\d\\d with error: %s$",
                accountId2, figi2, candleInterval2, commission2, strategyType2, mockedExceptionMessage2
        );
        AssertUtils.assertMatchesRegex(backTestResults.get(1).error(), expectedErrorPattern2);
    }

    @Test
    void test_catchesSaveToFileException_andFinishesBackTestsSuccessfully() {
        // arrange

        final TestShare testShare1 = TestShares.APPLE;
        final TestShare testShare2 = TestShares.SBER;

        final String currency1 = testShare1.getCurrency();
        final String currency2 = testShare2.getCurrency();

        Mocker.mockInstrument(extInstrumentsService, testShare1.instrument());
        Mocker.mockInstrument(extInstrumentsService, testShare2.instrument());

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final BigDecimal initialInvestment = DecimalUtils.setDefaultScale(10000);
        final BigDecimal balanceIncrement = DecimalUtils.setDefaultScale(1000);
        final Map<String, BigDecimal> initialBalances = Map.of(currency1, initialInvestment, currency2, initialInvestment);
        final Map<String, BigDecimal> balanceIncrements = Map.of(currency1, balanceIncrement, currency2, balanceIncrement);
        final BalanceConfig balanceConfig = new BalanceConfig(initialBalances, balanceIncrements, TestData.newCronExpression(BALANCE_INCREMENT_CRON));

        final BigDecimal currentBalance = DecimalUtils.ZERO;

        final double commission1 = 0.003;
        final Operation operation = TestData.newOperation(
                from.plusMinutes(2),
                OperationType.OPERATION_TYPE_BUY,
                100,
                2,
                testShare1.getFigi()
        );
        final BotConfig botConfig1 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare1.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                commission1,
                balanceConfig,
                interval,
                currentBalance,
                2,
                new LinkedHashMap<>(Map.of(from.plusMinutes(1), 100.0)),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                TestAccounts.TINKOFF.getId(),
                testShare2.share(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.001,
                balanceConfig,
                interval,
                currentBalance,
                null,
                Collections.emptyNavigableMap(),
                50,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        Mockito.doThrow(new IllegalArgumentException())
                .when(excelService)
                .saveBackTestResults(Mockito.anyCollection());

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, true);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        Assertions.assertNull(backTestResults.getFirst().error());
        Assertions.assertNull(backTestResults.get(1).error());
    }

    private BotConfig arrangeBackTest(
            final String accountId,
            final Share share,
            final CandleInterval candleInterval,
            final double commission,
            final BalanceConfig balanceConfig,
            final Interval interval,
            final BigDecimal currentBalance,
            final Integer quantity,
            final SequencedMap<OffsetDateTime, Double> prices,
            final double currentPrice,
            final Operation operation
    ) {
        final String figi = share.figi();
        final String currency = share.currency();

        final BigDecimal decimalCommission = DecimalUtils.setDefaultScale(commission);
        final BotConfig botConfig = new BotConfig(accountId, List.of(figi), candleInterval, decimalCommission, null, null);

        final FakeBot fakeBot = mockFakeBot(botConfig, balanceConfig, interval.getFrom());

        Mockito.when(extInstrumentsService.getShare(figi)).thenReturn(share);
        mockMarketCandles(figi, prices);
        mockPlusMinuteScheduled(fakeBot, interval.getFrom());
        mockInvestments(fakeBot, accountId, interval.getFrom(), currency, balanceConfig.getInitialBalances().get(currency));
        Mockito.when(fakeBot.getCurrentBalance(accountId, currency)).thenReturn(currentBalance);
        if (quantity != null) {
            mockPortfolioPosition(fakeBot, accountId, figi, currentPrice, quantity);
            mockCurrentPrice(fakeBot, figi, currentPrice);
        }
        if (operation == null) {
            Mocker.mockTinkoffOperations(fakeBot, accountId, figi, interval);
        } else {
            Mocker.mockTinkoffOperations(fakeBot, accountId, figi, interval, operation);
        }

        return botConfig;
    }

    private void mockCurrentPrice(final FakeBot fakeBot, final String figi, final double currentPrice) {
        Mockito.when(fakeBot.getCurrentPrice(Mockito.eq(figi), Mockito.any(OffsetDateTime.class)))
                .thenReturn(DecimalUtils.setDefaultScale(currentPrice));
    }

    private FakeBot mockFakeBot(final BotConfig botConfig, final BalanceConfig balanceConfig, final OffsetDateTime currentDateTime) {
        final FakeBot fakeBot = Mockito.mock(FakeBot.class);
        Mockito.when(fakeBotFactory.createBot(botConfig, balanceConfig, currentDateTime)).thenReturn(fakeBot);
        return fakeBot;
    }

    private void mockBotCandles(final BotConfig botConfig, final FakeBot fakeBot, final Map<OffsetDateTime, Double> prices) {
        Mockito.doAnswer(invocation -> {
            final OffsetDateTime currentDateTime = fakeBot.getCurrentDateTime();
            if (prices.containsKey(currentDateTime)) {
                final double close = prices.get(currentDateTime);
                final Candle candle = new CandleBuilder().setClose(close).setTime(currentDateTime.minusMinutes(1)).build();
                return List.of(candle);
            } else {
                return Collections.emptyList();
            }
        }).when(fakeBot).processBotConfig(Mockito.eq(botConfig), Mockito.any(Interval.class));
    }

    private void mockMarketCandles(final String figi, final Map<OffsetDateTime, Double> prices) {
        final List<Candle> candles = prices.entrySet().stream()
                .map(entry -> new CandleBuilder().setOpen(entry.getValue()).setTime(entry.getKey()).build())
                .toList();
        Mockito.when(extMarketDataService.getCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.nullable(CandleInterval.class)))
                .thenReturn(candles);
    }

    private void mockPlusMinuteScheduled(final FakeBot fakeBot, final OffsetDateTime from) {
        Mockito.when(fakeBot.getCurrentDateTime()).thenReturn(from);

        Mockito.when(fakeBot.nextScheduleMinute(Mockito.anyList())).thenAnswer(invocationOnMock -> {
            final OffsetDateTime currentTimestamp = fakeBot.getCurrentDateTime();
            final OffsetDateTime nextMinute = currentTimestamp.plusMinutes(1);
            Mockito.when(fakeBot.getCurrentDateTime()).thenReturn(nextMinute);
            return nextMinute;
        });
    }

    private void mockInvestments(
            final FakeBot fakeBot,
            final String accountId,
            final OffsetDateTime dateTime,
            final String currency,
            final BigDecimal initialInvestment
    ) {
        final SortedMap<OffsetDateTime, BigDecimal> investments = new TreeMap<>();
        investments.put(dateTime, initialInvestment);
        Mockito.when(fakeBot.getInvestments(accountId, currency)).thenReturn(investments);
    }

    private void mockPortfolioPosition(
            final FakeBot fakeBot,
            final String accountId,
            final String figi,
            final double currentPrice,
            final int quantity
    ) {
        final Position portfolioPosition = new PositionBuilder()
                .setFigi(figi)
                .setQuantity(quantity)
                .setCurrentPrice(currentPrice)
                .build();

        Mockito.when(fakeBot.getPortfolioPositions(accountId)).thenReturn(List.of(portfolioPosition));
    }

}