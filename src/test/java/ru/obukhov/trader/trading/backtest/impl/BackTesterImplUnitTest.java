package ru.obukhov.trader.trading.backtest.impl;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.config.properties.BackTestProperties;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.matchers.BigDecimalMatcher;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.obukhov.trader.trading.bots.FakeBot;
import ru.obukhov.trader.trading.bots.FakeBotFactory;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private FakeBotFactory fakeBotFactory;

    private BackTesterImpl backTester;

    @BeforeEach
    void setUp() {
        backTester = new BackTesterImpl(excelService, extInstrumentsService, fakeBotFactory, BACK_TEST_PROPERTIES);
    }

    @Test
    void test_throwsIllegalArgumentException_whenFromIsInFuture() {
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(10000.0, 1000.0);

        final List<BotConfig> botConfigs = Collections.emptyList();

        final Timestamp from = TimestampUtils.plusDays(TimestampUtils.now(), 1);
        final Timestamp to = TimestampUtils.plusDays(from, 1);
        final Interval interval = Interval.of(from, to);

        final String expectedMessagePattern = String.format("^'from' \\(%1$s\\) can't be in future. Now is %1$s$", DATE_TIME_REGEX_PATTERN);

        AssertUtils.assertThrowsWithMessagePattern(
                IllegalArgumentException.class,
                () -> backTester.test(botConfigs, balanceConfig, interval, false),
                expectedMessagePattern
        );
    }

    @Test
    void test_throwsIllegalArgumentException_whenToIsInFuture() {
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(10000.0, 1000.0);

        final List<BotConfig> botConfigs = Collections.emptyList();

        final Timestamp from = TimestampUtils.plusDays(TimestampUtils.now(), -1);
        final Timestamp to = TimestampUtils.plusDays(from, 2);
        final Interval interval = Interval.of(from, to);

        final String expectedMessagePattern = String.format("^'to' \\(%1$s\\) can't be in future. Now is %1$s$", DATE_TIME_REGEX_PATTERN);

        AssertUtils.assertThrowsWithMessagePattern(
                RuntimeException.class,
                () -> backTester.test(botConfigs, balanceConfig, interval, false),
                expectedMessagePattern
        );
    }

    @Test
    void test_throwsIllegalArgumentException_whenToIntervalIsShorterThanOneDay() {
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(10000.0, 1000.0);

        final List<BotConfig> botConfigs = Collections.emptyList();

        final Timestamp from = TimestampUtils.plusDays(TimestampUtils.now(), -1);
        final Timestamp to = TimestampUtils.plusNanos(TimestampUtils.plusDays(from, 1), -1);
        final Interval interval = Interval.of(from, to);

        final Executable executable = () -> backTester.test(botConfigs, balanceConfig, interval, false);
        AssertUtils.assertThrowsWithMessage(RuntimeException.class, executable, "interval can't be shorter than 1 day");
    }

    @Test
    void test_returnsResultWithEmptyValues_whenBotConfigProcessingThrowsException() {
        // arrange

        final Timestamp from = TimestampUtils.newTimestamp(2021, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final String accountId = TestData.ACCOUNT_ID1;
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Double commission = 0.003;
        final StrategyType strategyType = StrategyType.CONSERVATIVE;
        final BotConfig botConfig = new BotConfig(accountId, figi, candleInterval, commission, strategyType, Collections.emptyMap());

        final BalanceConfig balanceConfig = TestData.createBalanceConfig(10000.0, 1000.0);
        final List<BotConfig> botConfigs = List.of(botConfig);

        final FakeBot fakeBot = mockFakeBot(botConfig, balanceConfig, from);

        final String exceptionMessage = "exception message";
        Mockito.when(fakeBot.processBotConfig(botConfig, null))
                .thenThrow(new IllegalArgumentException(exceptionMessage));

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());

        final BackTestResult backTestResult = backTestResults.get(0);

        Assertions.assertEquals(botConfigs.get(0), backTestResult.botConfig());
        Assertions.assertEquals(interval, backTestResult.interval());
        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), backTestResult.balances().initialInvestment());
        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), backTestResult.balances().totalInvestment());
        AssertUtils.assertEquals(0, backTestResult.balances().finalTotalSavings());
        AssertUtils.assertEquals(0, backTestResult.balances().finalBalance());
        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), backTestResult.balances().weightedAverageInvestment());
        AssertUtils.assertEquals(0, backTestResult.profits().absolute());
        AssertUtils.assertEquals(0.0, backTestResult.profits().relative());
        AssertUtils.assertEquals(0.0, backTestResult.profits().relativeAnnual());

        final String expectedErrorPattern = String.format(
                Locale.US,
                "^Back test for 'BotConfig\\[accountId=%s, figi=%s, candleInterval=%s, commission=%.3f, strategyType=%s, " +
                        "strategyParams=\\{\\}\\]' failed within 00:00:00.\\d\\d\\d with error: %s$",
                accountId, figi, candleInterval, commission, strategyType, exceptionMessage
        );
        AssertUtils.assertMatchesRegex(backTestResult.error(), expectedErrorPattern);
    }

    @Test
    void test_fillsCommonStatistics() {

        // arrange

        final Timestamp from = TimestampUtils.newTimestamp(2021, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        final BigDecimal finalBalance1 = DecimalUtils.setDefaultScale(2000);
        final int finalQuantityLots1 = 8;

        final Map<Timestamp, Double> prices1 = new LinkedHashMap<>();
        prices1.put(TimestampUtils.plusMinutes(from, 10), 1000.0);
        prices1.put(TimestampUtils.plusMinutes(from, 20), 1001.0);
        prices1.put(TimestampUtils.plusMinutes(from, 30), 1002.0);
        prices1.put(TimestampUtils.plusMinutes(from, 40), 1003.0);
        final double finalPrice1 = 1004.0;
        prices1.put(TimestampUtils.plusMinutes(from, 50), finalPrice1);

        final BotConfig botConfig1 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare1.SHARE,
                0.003,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(2000),
                finalQuantityLots1,
                prices1,
                finalPrice1,
                null
        );

        final BigDecimal finalBalance2 = DecimalUtils.setDefaultScale(100);
        final int finalQuantityLots2 = 50;

        final Map<Timestamp, Double> prices2 = new LinkedHashMap<>();
        prices2.put(TimestampUtils.plusMinutes(from, 100), 100.0);
        prices2.put(TimestampUtils.plusMinutes(from, 200), 100.1);
        prices2.put(TimestampUtils.plusMinutes(from, 300), 100.2);
        prices2.put(TimestampUtils.plusMinutes(from, 400), 100.3);
        final double finalPrice2 = 100.4;
        prices2.put(TimestampUtils.plusMinutes(from, 500), finalPrice2);

        final BotConfig botConfig2 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare2.SHARE,
                0.001,
                balanceConfig,
                interval,
                finalBalance2,
                finalQuantityLots2,
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
                backTestResults.get(0), botConfigs.get(1), // expected sorting of results by profit
                interval, initialInvestment,
                finalPrice2, finalQuantityLots2 * TestShare2.LOT, finalBalance2,
                4.03, 1.768913277123785E256
        );

        assertCommonStatistics(
                backTestResults.get(1), botConfigs.get(0),
                interval, initialInvestment,
                finalPrice1, finalQuantityLots1 * TestShare1.LOT, finalBalance1,
                0.0032, 2.212128816
        );
    }

    @SuppressWarnings("SameParameterValue")
    private void assertCommonStatistics(
            final BackTestResult backTestResult,
            final BotConfig botConfig,
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
        AssertUtils.assertEquals(initialInvestment, backTestResult.balances().initialInvestment());
        AssertUtils.assertEquals(initialInvestment, backTestResult.balances().totalInvestment());

        final BigDecimal positionsPrice = DecimalUtils.setDefaultScale(currentPrice * positionQuantity);
        final BigDecimal expectedFinalTotalSavings = currentBalance.add(positionsPrice);
        AssertUtils.assertEquals(expectedFinalTotalSavings, backTestResult.balances().finalTotalSavings());

        AssertUtils.assertEquals(currentBalance, backTestResult.balances().finalBalance());
        AssertUtils.assertEquals(initialInvestment, backTestResult.balances().weightedAverageInvestment());

        final BigDecimal expectedAbsoluteProfit = DecimalUtils.subtract(currentBalance, initialInvestment).add(positionsPrice);
        AssertUtils.assertEquals(expectedAbsoluteProfit, backTestResult.profits().absolute());
        AssertUtils.assertEquals(expectedRelativeProfit, backTestResult.profits().relative());
        AssertUtils.assertEquals(expectedAnnualProfit, backTestResult.profits().relativeAnnual());
    }

    @Test
    void test_callsAddInvestment() {

        // arrange

        final Timestamp from = TimestampUtils.newTimestamp(2021, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final double balanceIncrement = 1000;

        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, balanceIncrement, BALANCE_INCREMENT_CRON);

        final String accountId1 = TestData.ACCOUNT_ID1;
        final String figi1 = TestShare1.FIGI;
        final String currency1 = TestShare1.CURRENCY;
        final Double commission1 = 0.003;

        final Map<Timestamp, Double> prices1 = new LinkedHashMap<>();
        prices1.put(TimestampUtils.plusMinutes(from, 10), 100.0);
        prices1.put(TimestampUtils.plusMinutes(from, 20), 200.0);
        prices1.put(TimestampUtils.plusMinutes(from, 30), 300.0);
        prices1.put(TimestampUtils.plusMinutes(from, 40), 400.0);
        prices1.put(TimestampUtils.plusMinutes(from, 50), 500.0);

        final BigDecimal currentBalance1 = DecimalUtils.setDefaultScale(2000);
        final int positionLotsCount1 = 2;

        final BotConfig botConfig1 = new BotConfig(accountId1, figi1, null, commission1, null, null);

        final FakeBot fakeBot1 = mockFakeBot(botConfig1, balanceConfig, from);
        Mockito.when(fakeBot1.getShare(figi1)).thenReturn(TestShare1.SHARE);

        mockBotCandles(botConfig1, fakeBot1, prices1);
        mockCurrentPrice(fakeBot1, figi1, 500);
        mockPlusMinuteScheduled(fakeBot1, from);

        mockInvestments(fakeBot1, accountId1, currency1, from, balanceConfig.getInitialBalance());
        Mockito.when(fakeBot1.getCurrentBalance(accountId1, currency1)).thenReturn(currentBalance1);
        mockPortfolioPosition(fakeBot1, accountId1, figi1, 500, 1, positionLotsCount1);

        final String accountId2 = TestData.ACCOUNT_ID2;
        final String figi2 = TestShare2.FIGI;
        final String currency2 = TestShare2.CURRENCY;
        final Double commission2 = 0.001;

        final Map<Timestamp, Double> prices2 = new LinkedHashMap<>();
        prices2.put(TimestampUtils.plusMinutes(from, 100), 10.0);
        prices2.put(TimestampUtils.plusMinutes(from, 200), 20.0);
        prices2.put(TimestampUtils.plusMinutes(from, 300), 30.0);
        prices2.put(TimestampUtils.plusMinutes(from, 400), 40.0);
        prices2.put(TimestampUtils.plusMinutes(from, 500), 50.0);

        final BigDecimal currentBalance2 = DecimalUtils.setDefaultScale(2000);
        final int positionLotsCount2 = 2;

        final BotConfig botConfig2 = new BotConfig(accountId2, figi2, null, commission2, null, null);

        final FakeBot fakeBot2 = mockFakeBot(botConfig2, balanceConfig, from);
        Mockito.when(fakeBot2.getShare(figi2)).thenReturn(TestShare2.SHARE);

        mockBotCandles(botConfig2, fakeBot2, prices2);
        mockCurrentPrice(fakeBot2, figi2, 50);
        mockPlusMinuteScheduled(fakeBot2, from);
        mockInvestments(fakeBot2, accountId2, currency2, from, balanceConfig.getInitialBalance());
        Mockito.when(fakeBot2.getCurrentBalance(accountId2, currency2)).thenReturn(currentBalance2);
        mockPortfolioPosition(fakeBot2, accountId2, figi2, 50, 1, positionLotsCount2);

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        Assertions.assertNull(backTestResults.get(0).error());
        Assertions.assertNull(backTestResults.get(1).error());

        Mockito.verify(fakeBot1, Mockito.times(24))
                .addInvestment(
                        Mockito.eq(accountId1),
                        Mockito.any(Timestamp.class),
                        Mockito.eq(currency1),
                        ArgumentMatchers.argThat(BigDecimalMatcher.of(balanceIncrement))
                );

        Mockito.verify(fakeBot2, Mockito.times(24))
                .addInvestment(
                        Mockito.eq(accountId2),
                        Mockito.any(Timestamp.class),
                        Mockito.eq(currency2),
                        ArgumentMatchers.argThat(BigDecimalMatcher.of(balanceIncrement))
                );
    }

    @Test
    void test_fillsPositions() {

        // arrange

        final Timestamp from = TimestampUtils.newTimestamp(2021, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        final Map<Timestamp, Double> prices1 = new LinkedHashMap<>();
        prices1.put(TimestampUtils.plusMinutes(from, 10), 100.0);
        prices1.put(TimestampUtils.plusMinutes(from, 20), 200.0);
        prices1.put(TimestampUtils.plusMinutes(from, 30), 300.0);
        prices1.put(TimestampUtils.plusMinutes(from, 40), 400.0);
        final double currentPrice1 = 500.0;
        prices1.put(TimestampUtils.plusMinutes(from, 50), currentPrice1);

        final int quantityLots1 = 2;

        final BotConfig botConfig1 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare1.SHARE,
                0.003,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(20000),
                quantityLots1,
                prices1,
                currentPrice1,
                null
        );

        final int quantityLots2 = 1;
        final double currentPrice2 = 5000.0;

        final BotConfig botConfig2 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare2.SHARE,
                0.001,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(10000),
                quantityLots2,
                Collections.emptyMap(),
                currentPrice2,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());
        assertPosition(backTestResults.get(0), TestShare2.FIGI, currentPrice2, quantityLots2 * TestShare2.LOT);
        assertPosition(backTestResults.get(1), TestShare1.FIGI, currentPrice1, quantityLots1 * TestShare1.LOT);
    }

    private void assertPosition(final BackTestResult backTestResult, final String figi, final double currentPrice, final int quantity) {
        Assertions.assertNull(backTestResult.error());
        final List<Position> positions = backTestResult.positions();
        Assertions.assertEquals(1, positions.size());
        final Position backTestPosition = positions.get(0);
        Assertions.assertEquals(figi, backTestPosition.getFigi());
        AssertUtils.assertEquals(currentPrice, backTestPosition.getCurrentPrice().getValue());
        AssertUtils.assertEquals(quantity, backTestPosition.getQuantity());
    }

    @Test
    void test_fillsOperations() {

        // arrange

        final Timestamp from = TimestampUtils.newTimestamp(2021, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        final String figi1 = TestShare1.FIGI;
        final double commission1 = 0.003;

        final BigDecimal currentBalance1 = DecimalUtils.setDefaultScale(20000);

        final int quantityLots1 = 1;

        final Timestamp operationTimestamp1 = TimestampUtils.plusMinutes(from, 2);
        final OperationType operationType1 = OperationType.OPERATION_TYPE_BUY;
        final double operationPrice1 = 100;
        final int operationQuantity1 = 2;
        final Operation operation1 = TestData.createOperation(operationTimestamp1, operationType1, operationPrice1, operationQuantity1, figi1);

        final String figi2 = TestShare2.FIGI;
        final double commission2 = 0.001;

        final BigDecimal currentBalance2 = DecimalUtils.setDefaultScale(10000);

        final int quantityLots2 = 1;

        final Timestamp operationTimestamp2 = TimestampUtils.plusMinutes(from, 3);
        final OperationType operationType2 = OperationType.OPERATION_TYPE_SELL;
        final double operationPrice2 = 1000;
        final int operationQuantity2 = 4;
        final Operation operation2 = TestData.createOperation(operationTimestamp2, operationType2, operationPrice2, operationQuantity2, figi2);

        final BotConfig botConfig1 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare1.SHARE,
                commission1,
                balanceConfig,
                interval,
                currentBalance1,
                quantityLots1,
                Map.of(TimestampUtils.plusMinutes(from, 1), 100.0),
                100,
                operation1
        );

        final BotConfig botConfig2 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare2.SHARE,
                commission2,
                balanceConfig,
                interval,
                currentBalance2,
                quantityLots2,
                Map.of(TimestampUtils.plusMinutes(from, 3), 1000.0),
                1000,
                operation2
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        assertOperation(backTestResults.get(0), figi1, operationTimestamp1, operationType1, operationPrice1, operationQuantity1);

        assertOperation(backTestResults.get(1), figi2, operationTimestamp2, operationType2, operationPrice2, operationQuantity2);
    }

    private void assertOperation(
            final BackTestResult backTestResult,
            final String expectedFigi,
            final Timestamp expectedTimestamp,
            final OperationType expectedOperationType,
            final double expectedOperationPrice,
            final int expectedOperationQuantity
    ) {
        Assertions.assertNull(backTestResult.error());

        final List<Operation> resultOperations = backTestResult.operations();
        Assertions.assertEquals(1, resultOperations.size());

        final Operation backTestOperation = resultOperations.get(0);
        Assertions.assertEquals(expectedFigi, backTestOperation.getFigi());
        Assertions.assertEquals(expectedTimestamp, backTestOperation.getDate());
        Assertions.assertEquals(expectedOperationType, backTestOperation.getOperationType());
        AssertUtils.assertEquals(expectedOperationPrice, backTestOperation.getPrice());
        AssertUtils.assertEquals(expectedOperationQuantity, backTestOperation.getQuantity());
    }

    @Test
    void test_fillsCandles() {

        // arrange

        final Timestamp from = TimestampUtils.newTimestamp(2021, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        final Map<Timestamp, Double> prices1 = new LinkedHashMap<>();
        prices1.put(TimestampUtils.plusMinutes(from, 1), 100.0);
        prices1.put(TimestampUtils.plusMinutes(from, 2), 200.0);
        prices1.put(TimestampUtils.plusMinutes(from, 3), 300.0);
        prices1.put(TimestampUtils.plusMinutes(from, 4), 400.0);
        prices1.put(TimestampUtils.plusMinutes(from, 5), 500.0);

        final BotConfig botConfig1 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare1.SHARE,
                0.003,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(20000),
                1,
                prices1,
                500,
                null
        );

        final Map<Timestamp, Double> prices2 = new LinkedHashMap<>();
        prices2.put(TimestampUtils.plusMinutes(from, 10), 1000.0);
        prices2.put(TimestampUtils.plusMinutes(from, 20), 2000.0);
        prices2.put(TimestampUtils.plusMinutes(from, 30), 3000.0);
        prices2.put(TimestampUtils.plusMinutes(from, 40), 4000.0);

        final BotConfig botConfig2 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare1.SHARE,
                0.001,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(10000),
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
        assertCandles(backTestResults.get(0), prices1);
        assertCandles(backTestResults.get(1), prices2);
    }

    private void assertCandles(final BackTestResult backTestResult, final Map<Timestamp, Double> prices) {
        Assertions.assertNull(backTestResult.error());

        final List<Candle> candles = backTestResult.candles();
        Assertions.assertEquals(prices.size(), candles.size());

        final Iterator<Candle> candlesIterator = candles.iterator();
        for (final Map.Entry<Timestamp, Double> entry : prices.entrySet()) {
            final Candle candle = candlesIterator.next();
            Assertions.assertEquals(entry.getKey(), candle.getTime());
            AssertUtils.assertEquals(entry.getValue(), candle.getClose());
        }
    }

    @Test
    void test_notFillsCandles_whenCurrentCandlesInDecisionDataIsAlwaysNullOrEmpty() {

        // arrange

        final Timestamp from = TimestampUtils.newTimestamp(2021, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        final BotConfig botConfig1 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare1.SHARE,
                0.003,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(20000),
                null,
                Collections.emptyMap(),
                300,
                null
        );

        final BotConfig botConfig2 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare2.SHARE,
                0.001,
                balanceConfig,
                interval,
                DecimalUtils.setDefaultScale(10000),
                null,
                Collections.emptyMap(),
                200,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        final BackTestResult backTestResult1 = backTestResults.get(0);
        Assertions.assertNull(backTestResult1.error());
        Assertions.assertTrue(backTestResult1.candles().isEmpty());

        final BackTestResult backTestResult2 = backTestResults.get(1);
        Assertions.assertNull(backTestResult2.error());
        Assertions.assertTrue(backTestResult2.candles().isEmpty());
    }

    @Test
    void test_callsSaveToFile_whenSaveToFilesIsTrue() {

        // arrange

        final Timestamp from = TimestampUtils.newTimestamp(2021, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);
        final BigDecimal currentBalance = DecimalUtils.setDefaultScale(0);

        final double commission1 = 0.001;
        final Timestamp operationTimestamp = TimestampUtils.plusMinutes(from, 2);
        final Operation operation = TestData.createOperation(operationTimestamp, OperationType.OPERATION_TYPE_BUY, 100, 2, TestShare1.FIGI);

        final BotConfig botConfig1 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare1.SHARE,
                commission1,
                balanceConfig,
                interval,
                currentBalance,
                1,
                Map.of(TimestampUtils.plusMinutes(from, 1), 100.0),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare2.SHARE,
                0.003,
                balanceConfig,
                interval,
                currentBalance,
                null,
                Collections.emptyMap(),
                200,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, true);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        Assertions.assertNull(backTestResults.get(0).error());
        Assertions.assertNull(backTestResults.get(1).error());

        Mockito.verify(excelService, Mockito.only()).saveBackTestResults(Mockito.anyCollection());
    }

    @Test
    void test_neverCallsSaveToFile_whenSaveToFilesIsFalse() {

        // arrange

        final Timestamp from = TimestampUtils.newTimestamp(2021, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);
        final BigDecimal currentBalance = DecimalUtils.setDefaultScale(0);

        final double commission1 = 0.001;
        final Timestamp operationTimestamp = TimestampUtils.plusMinutes(from, 2);
        final Operation operation = TestData.createOperation(operationTimestamp, OperationType.OPERATION_TYPE_BUY, 100, 2, TestShare1.FIGI);

        final BotConfig botConfig1 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare1.SHARE,
                commission1,
                balanceConfig,
                interval,
                currentBalance,
                1,
                Map.of(TimestampUtils.plusMinutes(from, 1), 100.0),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare2.SHARE,
                0.003,
                balanceConfig,
                interval,
                currentBalance,
                null,
                Collections.emptyMap(),
                150,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        Assertions.assertNull(backTestResults.get(0).error());
        Assertions.assertNull(backTestResults.get(1).error());

        Mockito.verify(excelService, Mockito.never()).saveBackTestResults(Mockito.any());
    }

    @Test
    void test_returnsZeroInvestmentsAndProfits_whenNoInvestments() {

        // arrange

        final Timestamp from = TimestampUtils.newTimestamp(2021, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 0;
        final BigDecimal currentBalance = DecimalUtils.setDefaultScale(20000);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment);

        final double commission1 = 0.003;
        final Timestamp operationTimestamp = TimestampUtils.plusMinutes(from, 2);
        final Operation operation = TestData.createOperation(operationTimestamp, OperationType.OPERATION_TYPE_BUY, 100, 2, TestShare1.FIGI);
        final BotConfig botConfig1 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare1.SHARE,
                commission1,
                balanceConfig,
                interval,
                currentBalance,
                10,
                Map.of(TimestampUtils.plusMinutes(from, 1), 100.0),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                null,
                TestShare2.SHARE,
                0.001,
                balanceConfig,
                interval,
                currentBalance,
                null,
                Collections.emptyMap(),
                300,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());
        assertNoInvestmentsAndProfits(backTestResults.get(0));
        assertNoInvestmentsAndProfits(backTestResults.get(1));
    }

    private void assertNoInvestmentsAndProfits(final BackTestResult backTestResult) {
        Assertions.assertNull(backTestResult.error());

        AssertUtils.assertEquals(0, backTestResult.balances().totalInvestment());
        AssertUtils.assertEquals(0, backTestResult.balances().weightedAverageInvestment());
        AssertUtils.assertEquals(0, backTestResult.profits().relative());
        AssertUtils.assertEquals(0, backTestResult.profits().relativeAnnual());
    }

    @Test
    void test_catchesBackTestException() {

        // arrange

        final Timestamp from = TimestampUtils.newTimestamp(2021, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final BalanceConfig balanceConfig = TestData.createBalanceConfig(10000.0, 1000.0);

        final String accountId1 = TestData.ACCOUNT_ID1;
        final String figi1 = TestShare1.FIGI;
        final CandleInterval candleInterval1 = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Double commission1 = 0.003;
        final StrategyType strategyType1 = StrategyType.CONSERVATIVE;

        final BotConfig botConfig1 = new BotConfig(accountId1, figi1, candleInterval1, commission1, strategyType1, Collections.emptyMap());

        mockFakeBot(botConfig1, balanceConfig, from);

        final String mockedExceptionMessage1 = "mocked exception 1";
        Mockito.when(fakeBotFactory.createBot(botConfig1, balanceConfig, from))
                .thenThrow(new IllegalArgumentException(mockedExceptionMessage1));

        final String accountId2 = TestData.ACCOUNT_ID2;
        final String figi2 = TestShare2.FIGI;
        final CandleInterval candleInterval2 = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Double commission2 = 0.001;
        final StrategyType strategyType2 = StrategyType.CROSS;

        final BotConfig botConfig2 = new BotConfig(accountId2, figi2, candleInterval2, commission2, strategyType2, Collections.emptyMap());

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
                "^Back test for 'BotConfig\\[accountId=%s, figi=%s, candleInterval=%s, commission=%s, strategyType=%s, strategyParams=\\{\\}\\]' " +
                        "failed within 00:00:00.\\d\\d\\d with error: %s$",
                accountId1, figi1, candleInterval1, commission1, strategyType1, mockedExceptionMessage1
        );
        AssertUtils.assertMatchesRegex(backTestResults.get(0).error(), expectedErrorPattern1);

        final String expectedErrorPattern2 = String.format(
                "^Back test for 'BotConfig\\[accountId=%s, figi=%s, candleInterval=%s, commission=%s, strategyType=%s, strategyParams=\\{\\}\\]' " +
                        "failed within 00:00:00.\\d\\d\\d with error: %s$",
                accountId2, figi2, candleInterval2, commission2, strategyType2, mockedExceptionMessage2
        );
        AssertUtils.assertMatchesRegex(backTestResults.get(1).error(), expectedErrorPattern2);
    }

    @Test
    void test_catchesSaveToFileException_andFinishesBackTestsSuccessfully() {

        // arrange

        final Timestamp from = TimestampUtils.newTimestamp(2021, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);
        final BigDecimal currentBalance = DecimalUtils.setDefaultScale(0);

        final double commission1 = 0.003;
        final Timestamp operationTimestamp = TimestampUtils.plusMinutes(from, 2);
        final Operation operation = TestData.createOperation(
                operationTimestamp,
                OperationType.OPERATION_TYPE_BUY,
                100,
                2,
                TestShare1.FIGI
        );
        final BotConfig botConfig1 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare1.SHARE,
                commission1,
                balanceConfig,
                interval,
                currentBalance,
                2,
                Map.of(TimestampUtils.plusMinutes(from, 1), 100.0),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                TestData.ACCOUNT_ID1,
                TestShare2.SHARE,
                0.001,
                balanceConfig,
                interval,
                currentBalance,
                null,
                Collections.emptyMap(),
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

        Assertions.assertNull(backTestResults.get(0).error());
        Assertions.assertNull(backTestResults.get(1).error());
    }

    private BotConfig arrangeBackTest(
            final String accountId,
            final Share share,
            final double commission,
            final BalanceConfig balanceConfig,
            final Interval interval,
            final BigDecimal currentBalance,
            final Integer quantityLots,
            final Map<Timestamp, Double> prices,
            final double currentPrice,
            final Operation operation
    ) {
        final String figi = share.getFigi();
        final String currency = share.getCurrency();

        final BotConfig botConfig = new BotConfig(accountId, figi, null, commission, null, null);

        final FakeBot fakeBot = mockFakeBot(botConfig, balanceConfig, interval.getFrom());

        Mockito.when(fakeBot.getShare(figi)).thenReturn(share);
        mockBotCandles(botConfig, fakeBot, prices);
        mockPlusMinuteScheduled(fakeBot, interval.getFrom());
        mockInvestments(fakeBot, accountId, currency, interval.getFrom(), balanceConfig.getInitialBalance());
        Mockito.when(fakeBot.getCurrentBalance(accountId, currency)).thenReturn(currentBalance);
        if (quantityLots != null) {
            final int quantity = share.getLot() * quantityLots;
            mockPortfolioPosition(fakeBot, accountId, figi, currentPrice, quantity, quantityLots);
            mockCurrentPrice(fakeBot, figi, currentPrice);
        }
        if (operation != null) {
            Mocker.mockTinkoffOperations(fakeBot, accountId, figi, interval, operation);
        }

        return botConfig;
    }

    private void mockCurrentPrice(final FakeBot fakeBot, final String figi, final double currentPrice) {
        Mockito.when(fakeBot.getCurrentPrice(figi))
                .thenReturn(DecimalUtils.setDefaultScale(currentPrice));
    }

    private FakeBot mockFakeBot(final BotConfig botConfig, final BalanceConfig balanceConfig, final Timestamp currentTimestamp) {
        final FakeBot fakeBot = Mockito.mock(FakeBot.class);
        Mockito.when(fakeBotFactory.createBot(botConfig, balanceConfig, currentTimestamp)).thenReturn(fakeBot);
        return fakeBot;
    }

    private void mockBotCandles(final BotConfig botConfig, final FakeBot fakeBot, final Map<Timestamp, Double> prices) {
        Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.nullable(Timestamp.class)))
                .thenAnswer(invocation -> {
                    final Timestamp currentTimestamp = fakeBot.getCurrentTimestamp();
                    if (prices.containsKey(currentTimestamp)) {
                        final double close = prices.get(currentTimestamp);
                        final Candle candle = new CandleBuilder().setClose(close).setTime(currentTimestamp).build();
                        return List.of(candle);
                    } else {
                        return Collections.emptyList();
                    }
                });
    }

    private void mockPlusMinuteScheduled(final FakeBot fakeBot, final Timestamp from) {
        Mockito.when(fakeBot.getCurrentTimestamp()).thenReturn(from);

        Mockito.when(fakeBot.nextScheduleMinute(Mockito.anyList())).thenAnswer(invocationOnMock -> {
            final Timestamp currentTimestamp = fakeBot.getCurrentTimestamp();
            final Timestamp nextMinute = TimestampUtils.plusMinutes(currentTimestamp, 1);
            Mockito.when(fakeBot.getCurrentTimestamp()).thenReturn(nextMinute);
            return nextMinute;
        });
    }

    private void mockInvestments(
            final FakeBot fakeBot,
            final String accountId,
            final String currency,
            final Timestamp timestamp,
            final BigDecimal initialInvestment
    ) {
        final SortedMap<Timestamp, BigDecimal> investments = new TreeMap<>(TimestampUtils::compare);
        investments.put(timestamp, initialInvestment);
        Mockito.when(fakeBot.getInvestments(accountId, currency)).thenReturn(investments);
    }

    private void mockPortfolioPosition(
            final FakeBot fakeBot,
            final String accountId,
            final String figi,
            final double currentPrice,
            final int quantity,
            final int quantityLots
    ) {
        final Position portfolioPosition = new PositionBuilder()
                .setFigi(figi)
                .setQuantity(quantity)
                .setCurrentPrice(currentPrice)
                .setQuantityLots(quantityLots)
                .build();

        Mockito.when(fakeBot.getPortfolioPositions(accountId)).thenReturn(List.of(portfolioPosition));
    }

}