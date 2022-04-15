package ru.obukhov.trader.trading.backtest.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.properties.BackTestProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.CandleInterval;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.Operation;
import ru.obukhov.trader.market.model.OperationType;
import ru.obukhov.trader.market.model.OperationTypeWithCommission;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.OperationTypeMapper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.test.utils.matchers.BigDecimalMatcher;
import ru.obukhov.trader.trading.bots.impl.FakeBot;
import ru.obukhov.trader.trading.bots.impl.FakeBotFactory;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.obukhov.trader.trading.model.BackTestPosition;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
    private static final OperationTypeMapper operationMapper = Mappers.getMapper(OperationTypeMapper.class);

    @Mock
    private ExcelService excelService;
    @Mock
    private FakeBotFactory fakeBotFactory;

    private BackTesterImpl backTester;

    @BeforeEach
    void setUp() {
        backTester = new BackTesterImpl(excelService, fakeBotFactory, BACK_TEST_PROPERTIES);
    }

    @Test
    void test_throwsIllegalArgumentException_whenFromIsInFuture() {
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(10000.0, 1000.0);

        final List<BotConfig> botConfigs = Collections.emptyList();

        final OffsetDateTime from = OffsetDateTime.now().plusDays(1);
        final OffsetDateTime to = from.plusDays(1);
        final Interval interval = Interval.of(from, to);

        final String expectedMessagePattern = String.format("^'from' \\(%1$s\\) can't be in future. Now is %1$s$", DATE_TIME_REGEX_PATTERN);

        AssertUtils.assertThrowsWithMessagePattern(
                () -> backTester.test(botConfigs, balanceConfig, interval, false),
                IllegalArgumentException.class,
                expectedMessagePattern
        );
    }

    @Test
    void test_throwsIllegalArgumentException_whenToIsInFuture() {
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(10000.0, 1000.0);

        final List<BotConfig> botConfigs = Collections.emptyList();

        final OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        final OffsetDateTime to = from.plusDays(2);
        final Interval interval = Interval.of(from, to);

        final String expectedMessagePattern = String.format("^'to' \\(%1$s\\) can't be in future. Now is %1$s$", DATE_TIME_REGEX_PATTERN);

        AssertUtils.assertThrowsWithMessagePattern(
                () -> backTester.test(botConfigs, balanceConfig, interval, false),
                RuntimeException.class,
                expectedMessagePattern
        );
    }

    @Test
    void test_throwsIllegalArgumentException_whenToIntervalIsShorterThanOneDay() {
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(10000.0, 1000.0);

        final List<BotConfig> botConfigs = Collections.emptyList();

        final OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        final OffsetDateTime to = from.plusDays(1).minusNanos(1);
        final Interval interval = Interval.of(from, to);

        final Executable executable = () -> backTester.test(botConfigs, balanceConfig, interval, false);
        Assertions.assertThrows(RuntimeException.class, executable, "interval can't be shorter than 1 day");
    }

    @Test
    void test_returnsResultWithEmptyValues_whenBotConfigProcessingThrowsException() throws IOException {
        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final String brokerAccountId = "2000124699";
        final String ticker = "ticker";
        final CandleInterval candleInterval = CandleInterval._1MIN;
        final Double commission = 0.003;
        final StrategyType strategyType = StrategyType.CONSERVATIVE;
        final BotConfig botConfig = BotConfig.builder()
                .brokerAccountId(brokerAccountId)
                .ticker(ticker)
                .candleInterval(candleInterval)
                .commission(commission)
                .strategyType(strategyType)
                .strategyParams(Collections.emptyMap())
                .build();
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

        Assertions.assertEquals(botConfigs.get(0), backTestResult.getBotConfig());
        Assertions.assertEquals(interval, backTestResult.getInterval());
        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), backTestResult.getBalances().getInitialInvestment());
        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), backTestResult.getBalances().getTotalInvestment());
        AssertUtils.assertEquals(0, backTestResult.getBalances().getFinalTotalSavings());
        AssertUtils.assertEquals(0, backTestResult.getBalances().getFinalBalance());
        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), backTestResult.getBalances().getWeightedAverageInvestment());
        AssertUtils.assertEquals(0, backTestResult.getProfits().getAbsolute());
        AssertUtils.assertEquals(0.0, backTestResult.getProfits().getRelative());
        AssertUtils.assertEquals(0.0, backTestResult.getProfits().getRelativeAnnual());

        final String expectedErrorPattern = String.format(
                Locale.US,
                "^Back test for '\\[brokerAccountId=%s, ticker=%s, candleInterval=%s, commission=%.3f, strategyType=%s, " +
                        "strategyParams=\\{\\}\\]' failed within 00:00:00.\\d\\d\\d with error: %s$",
                brokerAccountId, ticker, candleInterval, commission, strategyType, exceptionMessage
        );
        AssertUtils.assertMatchesRegex(backTestResult.getError(), expectedErrorPattern);
    }

    @Test
    void test_fillsCommonStatistics() throws IOException {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        final BigDecimal finalBalance1 = BigDecimal.valueOf(2000);
        final int finalPositionLotsCount1 = 8;

        final Map<OffsetDateTime, Double> prices1 = new LinkedHashMap<>();
        prices1.put(from.plusMinutes(10), 1000.0);
        prices1.put(from.plusMinutes(20), 1001.0);
        prices1.put(from.plusMinutes(30), 1002.0);
        prices1.put(from.plusMinutes(40), 1003.0);
        final double finalPrice1 = 1004.0;
        prices1.put(from.plusMinutes(50), finalPrice1);

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                0.003,
                balanceConfig,
                interval,
                BigDecimal.valueOf(2000),
                finalPositionLotsCount1,
                prices1,
                finalPrice1,
                null
        );

        final BigDecimal finalBalance2 = BigDecimal.valueOf(100);
        final int finalPositionLotsCount2 = 5;

        final Map<OffsetDateTime, Double> prices2 = new LinkedHashMap<>();
        prices2.put(from.plusMinutes(100), 2000.0);
        prices2.put(from.plusMinutes(200), 2001.0);
        prices2.put(from.plusMinutes(300), 2002.0);
        prices2.put(from.plusMinutes(400), 2003.0);
        final double finalPrice2 = 2004.0;
        prices2.put(from.plusMinutes(500), finalPrice2);

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                "ticker2",
                0.001,
                balanceConfig,
                interval,
                finalBalance2,
                finalPositionLotsCount2,
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
                finalPrice2, finalPositionLotsCount2, finalBalance2,
                0.012, 77.015733
        );

        assertCommonStatistics(
                backTestResults.get(1), botConfigs.get(0),
                interval, initialInvestment,
                finalPrice1, finalPositionLotsCount1, finalBalance1,
                0.0032, 2.212129
        );
    }

    private void assertCommonStatistics(
            final BackTestResult backTestResult,
            final BotConfig botConfig,
            final Interval interval,
            final double initialInvestment,
            final Double currentPrice,
            final int positionLotsCount,
            final BigDecimal currentBalance,
            final double expectedRelativeProfit,
            final double expectedAnnualProfit
    ) {
        Assertions.assertNull(backTestResult.getError());

        Assertions.assertEquals(botConfig, backTestResult.getBotConfig());
        Assertions.assertEquals(interval, backTestResult.getInterval());
        AssertUtils.assertEquals(initialInvestment, backTestResult.getBalances().getInitialInvestment());
        AssertUtils.assertEquals(initialInvestment, backTestResult.getBalances().getTotalInvestment());

        final BigDecimal positionsPrice2 = DecimalUtils.setDefaultScale(currentPrice * positionLotsCount);
        final BigDecimal expectedFinalTotalSavings2 = currentBalance.add(positionsPrice2);
        AssertUtils.assertEquals(expectedFinalTotalSavings2, backTestResult.getBalances().getFinalTotalSavings());

        AssertUtils.assertEquals(currentBalance, backTestResult.getBalances().getFinalBalance());
        AssertUtils.assertEquals(initialInvestment, backTestResult.getBalances().getWeightedAverageInvestment());

        final BigDecimal expectedAbsoluteProfit2 = DecimalUtils.subtract(currentBalance, initialInvestment).add(positionsPrice2);
        AssertUtils.assertEquals(expectedAbsoluteProfit2, backTestResult.getProfits().getAbsolute());
        AssertUtils.assertEquals(expectedRelativeProfit, backTestResult.getProfits().getRelative());
        AssertUtils.assertEquals(expectedAnnualProfit, backTestResult.getProfits().getRelativeAnnual());
    }

    @Test
    void test_callsAddInvestment() throws IOException {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final double balanceIncrement = 1000;

        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, balanceIncrement, BALANCE_INCREMENT_CRON);

        final String brokerAccountId1 = null;
        final String ticker1 = "ticker1";
        final Double commission1 = 0.003;

        final Map<OffsetDateTime, Double> prices1 = new LinkedHashMap<>();
        prices1.put(from.plusMinutes(10), 100.0);
        prices1.put(from.plusMinutes(20), 200.0);
        prices1.put(from.plusMinutes(30), 300.0);
        prices1.put(from.plusMinutes(40), 400.0);
        prices1.put(from.plusMinutes(50), 500.0);

        final BigDecimal currentBalance1 = BigDecimal.valueOf(2000);
        final int positionLotsCount1 = 2;

        final BotConfig botConfig1 = BotConfig.builder()
                .brokerAccountId(brokerAccountId1)
                .ticker(ticker1)
                .commission(commission1)
                .build();

        final FakeBot fakeBot1 = mockFakeBot(botConfig1, balanceConfig, from);
        final MarketInstrument marketInstrument1 = TestData.createMarketInstrument(ticker1, 10);
        Mockito.when(fakeBot1.searchMarketInstrument(ticker1)).thenReturn(marketInstrument1);

        mockBotCandles(botConfig1, fakeBot1, prices1);
        mockCurrentPrice(fakeBot1, ticker1, 500);
        mockNextMinute(fakeBot1, from);
        mockInvestments(fakeBot1, brokerAccountId1, marketInstrument1.currency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeBot1.getCurrentBalance(brokerAccountId1, marketInstrument1.currency())).thenReturn(currentBalance1);
        mockPortfolioPosition(fakeBot1, brokerAccountId1, ticker1, positionLotsCount1);

        final String brokerAccountId2 = "2000124699";
        final String ticker2 = "ticker1";
        final Double commission2 = 0.001;

        final Map<OffsetDateTime, Double> prices2 = new LinkedHashMap<>();
        prices2.put(from.plusMinutes(100), 10.0);
        prices2.put(from.plusMinutes(200), 20.0);
        prices2.put(from.plusMinutes(300), 30.0);
        prices2.put(from.plusMinutes(400), 40.0);
        prices2.put(from.plusMinutes(500), 50.0);

        final BigDecimal currentBalance2 = BigDecimal.valueOf(2000);
        final int positionLotsCount2 = 2;

        final BotConfig botConfig2 = BotConfig.builder()
                .brokerAccountId(brokerAccountId2)
                .ticker(ticker2)
                .commission(commission2)
                .build();

        final FakeBot fakeBot2 = mockFakeBot(botConfig2, balanceConfig, from);
        final MarketInstrument marketInstrument2 = TestData.createMarketInstrument(ticker2, 10);
        Mockito.when(fakeBot2.searchMarketInstrument(ticker1)).thenReturn(marketInstrument2);

        mockBotCandles(botConfig2, fakeBot2, prices2);
        mockCurrentPrice(fakeBot2, ticker2, 50);
        mockNextMinute(fakeBot2, from);
        mockInvestments(fakeBot2, brokerAccountId2, marketInstrument2.currency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeBot2.getCurrentBalance(brokerAccountId2, marketInstrument2.currency())).thenReturn(currentBalance2);
        mockPortfolioPosition(fakeBot2, brokerAccountId2, ticker2, positionLotsCount2);

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        Assertions.assertNull(backTestResults.get(0).getError());
        Assertions.assertNull(backTestResults.get(1).getError());

        Mockito.verify(fakeBot1, Mockito.times(24))
                .addInvestment(
                        Mockito.eq(brokerAccountId1),
                        Mockito.any(OffsetDateTime.class),
                        Mockito.eq(marketInstrument1.currency()),
                        ArgumentMatchers.argThat(BigDecimalMatcher.of(balanceIncrement))
                );

        Mockito.verify(fakeBot2, Mockito.times(24))
                .addInvestment(
                        Mockito.eq(brokerAccountId2),
                        Mockito.any(OffsetDateTime.class),
                        Mockito.eq(marketInstrument2.currency()),
                        ArgumentMatchers.argThat(BigDecimalMatcher.of(balanceIncrement))
                );
    }

    @Test
    void test_fillsPositions() throws IOException {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        final String ticker1 = "ticker1";

        final Map<OffsetDateTime, Double> prices1 = new LinkedHashMap<>();
        prices1.put(from.plusMinutes(10), 100.0);
        prices1.put(from.plusMinutes(20), 200.0);
        prices1.put(from.plusMinutes(30), 300.0);
        prices1.put(from.plusMinutes(40), 400.0);
        final double currentPrice1 = 500.0;
        prices1.put(from.plusMinutes(50), currentPrice1);

        final int positionLotsCount1 = 2;

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                ticker1,
                0.003,
                balanceConfig,
                interval,
                BigDecimal.valueOf(20000),
                positionLotsCount1,
                prices1,
                currentPrice1,
                null
        );

        final String ticker2 = "ticker2";
        final int positionLotsCount2 = 1;
        final double currentPrice2 = 5000.0;

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                ticker2,
                0.001,
                balanceConfig,
                interval,
                BigDecimal.valueOf(10000),
                positionLotsCount2,
                Collections.emptyMap(),
                currentPrice2,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());
        assertPosition(backTestResults.get(0), ticker1, currentPrice1, positionLotsCount1);
        assertPosition(backTestResults.get(1), ticker2, currentPrice2, positionLotsCount2);
    }

    private void assertPosition(final BackTestResult backTestResult, final String ticker, final double currentPrice, final int positionLotsCount) {
        Assertions.assertNull(backTestResult.getError());
        final List<BackTestPosition> positions = backTestResult.getPositions();
        Assertions.assertEquals(1, positions.size());
        final BackTestPosition backTestPosition = positions.get(0);
        Assertions.assertEquals(ticker, backTestPosition.getTicker());
        AssertUtils.assertEquals(currentPrice, backTestPosition.getPrice());
        Assertions.assertEquals(positionLotsCount, backTestPosition.getQuantity());
    }

    @Test
    void test_fillsOperations() throws IOException {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        final String ticker1 = "ticker1";
        final double commission1 = 0.003;

        final BigDecimal currentBalance1 = BigDecimal.valueOf(20000);

        final int positionLotsCount1 = 1;

        final OffsetDateTime operationDateTime1 = from.plusMinutes(2);
        final OperationTypeWithCommission operationType1 = OperationTypeWithCommission.BUY;
        final double operationPrice1 = 100;
        final int operationQuantity1 = 2;
        final Operation operation1 = TestData.createOperation(
                operationDateTime1,
                operationType1,
                operationPrice1,
                operationQuantity1,
                commission1
        );

        final String ticker2 = "ticker2";
        final double commission2 = 0.001;

        final BigDecimal currentBalance2 = BigDecimal.valueOf(10000);

        final int positionLotsCount2 = 1;

        final OffsetDateTime operationDateTime2 = from.plusMinutes(3);
        final OperationTypeWithCommission operationType2 = OperationTypeWithCommission.SELL;
        final double operationPrice2 = 1000;
        final int operationQuantity2 = 4;
        final Operation operation2 = TestData.createOperation(
                operationDateTime2,
                operationType2,
                operationPrice2,
                operationQuantity2,
                commission2
        );

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                ticker1,
                commission1,
                balanceConfig,
                interval,
                currentBalance1,
                positionLotsCount1,
                Map.of(from.plusMinutes(1), 100.0),
                100,
                operation1
        );

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                ticker2,
                commission2,
                balanceConfig,
                interval,
                currentBalance2,
                positionLotsCount2,
                Map.of(from.plusMinutes(3), 1000.0),
                1000,
                operation2
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        assertOperation(
                backTestResults.get(0),
                ticker1,
                operationDateTime1,
                operationMapper.map(operationType1),
                operationPrice1,
                operationQuantity1,
                commission1
        );

        assertOperation(
                backTestResults.get(1),
                ticker2,
                operationDateTime2,
                operationMapper.map(operationType2),
                operationPrice2,
                operationQuantity2,
                commission2
        );
    }

    private void assertOperation(
            final BackTestResult backTestResult,
            final String expectedTicker,
            final OffsetDateTime expectedOperationDateTime,
            final OperationType expectedOperationType,
            final double expectedOperationPrice,
            final int expectedOperationQuantity,
            final double expectedOperationCommission
    ) {
        Assertions.assertNull(backTestResult.getError());

        final List<BackTestOperation> resultOperations = backTestResult.getOperations();
        Assertions.assertEquals(1, resultOperations.size());

        final BackTestOperation backTestOperation = resultOperations.get(0);
        Assertions.assertEquals(expectedTicker, backTestOperation.ticker());
        Assertions.assertEquals(expectedOperationDateTime, backTestOperation.dateTime());
        Assertions.assertEquals(expectedOperationType, backTestOperation.operationType());
        AssertUtils.assertEquals(expectedOperationPrice, backTestOperation.price());
        Assertions.assertEquals(expectedOperationQuantity, backTestOperation.quantity());
        AssertUtils.assertEquals(expectedOperationCommission, backTestOperation.commission());
    }

    @Test
    void test_fillsCandles() throws IOException {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        final Map<OffsetDateTime, Double> prices1 = new LinkedHashMap<>();
        prices1.put(from.plusMinutes(1), 100.0);
        prices1.put(from.plusMinutes(2), 200.0);
        prices1.put(from.plusMinutes(3), 300.0);
        prices1.put(from.plusMinutes(4), 400.0);
        prices1.put(from.plusMinutes(5), 500.0);

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                0.003,
                balanceConfig,
                interval,
                BigDecimal.valueOf(20000),
                1,
                prices1,
                500,
                null
        );

        final Map<OffsetDateTime, Double> prices2 = new LinkedHashMap<>();
        prices2.put(from.plusMinutes(10), 1000.0);
        prices2.put(from.plusMinutes(20), 2000.0);
        prices2.put(from.plusMinutes(30), 3000.0);
        prices2.put(from.plusMinutes(40), 4000.0);

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                "ticker2",
                0.001,
                balanceConfig,
                interval,
                BigDecimal.valueOf(10000),
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

    private void assertCandles(final BackTestResult backTestResult, final Map<OffsetDateTime, Double> prices) {
        Assertions.assertNull(backTestResult.getError());

        final List<Candle> candles = backTestResult.getCandles();
        Assertions.assertEquals(prices.size(), candles.size());

        final Iterator<Candle> candlesIterator = candles.iterator();
        for (final Map.Entry<OffsetDateTime, Double> entry : prices.entrySet()) {
            final Candle candle = candlesIterator.next();
            Assertions.assertEquals(entry.getKey(), candle.getTime());
            AssertUtils.assertEquals(entry.getValue(), candle.getClosePrice());
        }
    }

    @Test
    void test_notFillsCandles_whenCurrentCandlesInDecisionDataIsAlwaysNullOrEmpty() throws IOException {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                0.003,
                balanceConfig,
                interval,
                BigDecimal.valueOf(20000),
                null,
                Collections.emptyMap(),
                300,
                null
        );

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                "ticker2",
                0.001,
                balanceConfig,
                interval,
                BigDecimal.valueOf(10000),
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
        Assertions.assertNull(backTestResult1.getError());
        Assertions.assertTrue(backTestResult1.getCandles().isEmpty());

        final BackTestResult backTestResult2 = backTestResults.get(1);
        Assertions.assertNull(backTestResult2.getError());
        Assertions.assertTrue(backTestResult2.getCandles().isEmpty());
    }

    @Test
    void test_callsSaveToFile_whenSaveToFilesIsTrue() throws IOException {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);
        final BigDecimal currentBalance = BigDecimal.ZERO;

        final double commission1 = 0.001;
        final Operation operation = TestData.createOperation(
                from.plusMinutes(2),
                OperationTypeWithCommission.BUY,
                100,
                2,
                commission1
        );

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                commission1,
                balanceConfig,
                interval,
                currentBalance,
                1,
                Map.of(from.plusMinutes(1), 100.0),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                "ticker2",
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

        Assertions.assertNull(backTestResults.get(0).getError());
        Assertions.assertNull(backTestResults.get(1).getError());

        Mockito.verify(excelService, Mockito.only()).saveBackTestResults(Mockito.anyCollection());
    }

    @Test
    void test_neverCallsSaveToFile_whenSaveToFilesIsFalse() throws IOException {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);
        final BigDecimal currentBalance = BigDecimal.ZERO;

        final double commission1 = 0.001;
        final Operation operation = TestData.createOperation(
                from.plusMinutes(2),
                OperationTypeWithCommission.BUY,
                100,
                2,
                commission1
        );

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                commission1,
                balanceConfig,
                interval,
                currentBalance,
                1,
                Map.of(from.plusMinutes(1), 100.0),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                "ticker2",
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

        Assertions.assertNull(backTestResults.get(0).getError());
        Assertions.assertNull(backTestResults.get(1).getError());

        Mockito.verify(excelService, Mockito.never()).saveBackTestResults(Mockito.any());
    }

    @Test
    void test_returnsZeroInvestmentsAndProfits_whenNoInvestments() throws IOException {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 0;
        final BigDecimal currentBalance = BigDecimal.valueOf(20000);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment);

        final double commission1 = 0.003;
        final Operation operation = TestData.createOperation(
                from.plusMinutes(2),
                OperationTypeWithCommission.BUY,
                100,
                2,
                commission1
        );
        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                commission1,
                balanceConfig,
                interval,
                currentBalance,
                10,
                Map.of(from.plusMinutes(1), 100.0),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                null,
                "ticker2",
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
        Assertions.assertNull(backTestResult.getError());

        AssertUtils.assertEquals(0, backTestResult.getBalances().getTotalInvestment());
        AssertUtils.assertEquals(0, backTestResult.getBalances().getWeightedAverageInvestment());
        AssertUtils.assertEquals(0, backTestResult.getProfits().getRelative());
        AssertUtils.assertEquals(0, backTestResult.getProfits().getRelativeAnnual());
    }

    @Test
    void test_catchesBackTestException() throws IOException {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final BalanceConfig balanceConfig = TestData.createBalanceConfig(10000.0, 1000.0);

        final String brokerAccountId1 = null;
        final String ticker1 = "ticker1";
        final CandleInterval candleInterval1 = CandleInterval._1MIN;
        final Double commission1 = 0.003;
        final StrategyType strategyType1 = StrategyType.CONSERVATIVE;

        final BotConfig botConfig1 = BotConfig.builder()
                .brokerAccountId(brokerAccountId1)
                .ticker(ticker1)
                .candleInterval(candleInterval1)
                .commission(commission1)
                .strategyType(strategyType1)
                .strategyParams(Collections.emptyMap())
                .build();

        mockFakeBot(botConfig1, balanceConfig, from);

        final String mockedExceptionMessage1 = "mocked exception 1";
        Mockito.when(fakeBotFactory.createBot(botConfig1, balanceConfig, from))
                .thenThrow(new IllegalArgumentException(mockedExceptionMessage1));

        final String brokerAccountId2 = "2000124699";
        final String ticker2 = "ticker2";
        final CandleInterval candleInterval2 = CandleInterval._1MIN;
        final Double commission2 = 0.001;
        final StrategyType strategyType2 = StrategyType.CROSS;

        final BotConfig botConfig2 = BotConfig.builder()
                .brokerAccountId(brokerAccountId2)
                .ticker(ticker2)
                .candleInterval(candleInterval2)
                .commission(commission2)
                .strategyType(strategyType2)
                .strategyParams(Collections.emptyMap())
                .build();

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
                "^Back test for '\\[brokerAccountId=%s, ticker=%s, candleInterval=%s, commission=%s, strategyType=%s, strategyParams=\\{\\}\\]' " +
                        "failed within 00:00:00.\\d\\d\\d with error: %s$",
                brokerAccountId1, ticker1, candleInterval1, commission1, strategyType1, mockedExceptionMessage1
        );
        AssertUtils.assertMatchesRegex(backTestResults.get(0).getError(), expectedErrorPattern1);

        final String expectedErrorPattern2 = String.format(
                "^Back test for '\\[brokerAccountId=%s, ticker=%s, candleInterval=%s, commission=%s, strategyType=%s, strategyParams=\\{\\}\\]' " +
                        "failed within 00:00:00.\\d\\d\\d with error: %s$",
                brokerAccountId2, ticker2, candleInterval2, commission2, strategyType2, mockedExceptionMessage2
        );
        AssertUtils.assertMatchesRegex(backTestResults.get(1).getError(), expectedErrorPattern2);
    }

    @Test
    void test_catchesSaveToFileException_andFinishesBackTestsSuccessfully() throws IOException {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);
        final BigDecimal currentBalance = BigDecimal.ZERO;

        final double commission1 = 0.003;
        final Operation operation = TestData.createOperation(
                from.plusMinutes(2),
                OperationTypeWithCommission.BUY,
                100,
                2,
                commission1
        );
        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                commission1,
                balanceConfig,
                interval,
                currentBalance,
                2,
                Map.of(from.plusMinutes(1), 100.0),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                "ticker2",
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

        Assertions.assertNull(backTestResults.get(0).getError());
        Assertions.assertNull(backTestResults.get(1).getError());
    }

    private BotConfig arrangeBackTest(
            final String brokerAccountId,
            final String ticker,
            final double commission,
            final BalanceConfig balanceConfig,
            final Interval interval,
            final BigDecimal currentBalance,
            final Integer positionLotsCount,
            final Map<OffsetDateTime, Double> prices,
            final double currentPrice,
            final Operation operation
    ) throws IOException {
        final BotConfig botConfig = BotConfig.builder()
                .brokerAccountId(brokerAccountId)
                .ticker(ticker)
                .commission(commission)
                .build();

        final FakeBot fakeBot = mockFakeBot(botConfig, balanceConfig, interval.getFrom());

        final MarketInstrument marketInstrument = TestData.createMarketInstrument(ticker, 10);
        Mockito.when(fakeBot.searchMarketInstrument(ticker)).thenReturn(marketInstrument);
        mockBotCandles(botConfig, fakeBot, prices);
        mockNextMinute(fakeBot, interval.getFrom());
        mockInvestments(fakeBot, brokerAccountId, marketInstrument.currency(), interval.getFrom(), balanceConfig.getInitialBalance());
        Mockito.when(fakeBot.getCurrentBalance(brokerAccountId, marketInstrument.currency())).thenReturn(currentBalance);
        if (positionLotsCount != null) {
            mockPortfolioPosition(fakeBot, brokerAccountId, ticker, positionLotsCount);
            mockCurrentPrice(fakeBot, ticker, currentPrice);
        }
        if (operation != null) {
            Mocker.mockTinkoffOperations(fakeBot, brokerAccountId, ticker, interval, operation);
        }

        return botConfig;
    }

    private void mockCurrentPrice(final FakeBot fakeBot, final String ticker, final double currentPrice) throws IOException {
        Mockito.when(fakeBot.getCurrentPrice(ticker))
                .thenReturn(DecimalUtils.setDefaultScale(currentPrice));
    }

    private FakeBot mockFakeBot(final BotConfig botConfig, final BalanceConfig balanceConfig, final OffsetDateTime currentDateTime) throws IOException {
        final FakeBot fakeBot = Mockito.mock(FakeBot.class);
        Mockito.when(fakeBotFactory.createBot(botConfig, balanceConfig, currentDateTime)).thenReturn(fakeBot);
        return fakeBot;
    }

    private void mockBotCandles(final BotConfig botConfig, final FakeBot fakeBot, final Map<OffsetDateTime, Double> prices) throws IOException {
        Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.nullable(OffsetDateTime.class)))
                .thenAnswer(invocation -> {
                    final OffsetDateTime currentDateTime = fakeBot.getCurrentDateTime();
                    if (prices.containsKey(currentDateTime)) {
                        final double closePrice = prices.get(currentDateTime);
                        final Candle candle = TestData.createCandleWithClosePriceAndTime(closePrice, currentDateTime);
                        return List.of(candle);
                    } else {
                        return Collections.emptyList();
                    }
                });
    }

    private void mockNextMinute(final FakeBot fakeBot, final OffsetDateTime from) {
        Mockito.when(fakeBot.getCurrentDateTime()).thenReturn(from);

        Mockito.when(fakeBot.nextMinute()).thenAnswer(invocationOnMock -> {
            final OffsetDateTime currentDateTime = fakeBot.getCurrentDateTime();
            final OffsetDateTime nextMinute = currentDateTime.plusMinutes(1);
            Mockito.when(fakeBot.getCurrentDateTime()).thenReturn(nextMinute);
            return nextMinute;
        });
    }

    private void mockInvestments(
            final FakeBot fakeBot,
            final String brokerAccountId,
            final Currency currency,
            final OffsetDateTime dateTime,
            final BigDecimal initialInvestment
    ) {
        final SortedMap<OffsetDateTime, BigDecimal> investments = new TreeMap<>();
        investments.put(dateTime, initialInvestment);
        Mockito.when(fakeBot.getInvestments(brokerAccountId, currency)).thenReturn(investments);
    }

    private void mockPortfolioPosition(
            final FakeBot fakeBot,
            final String brokerAccountId,
            final String ticker,
            final int positionLotsCount
    ) throws IOException {
        final PortfolioPosition portfolioPosition = TestData.createPortfolioPosition(ticker, positionLotsCount);
        Mockito.when(fakeBot.getPortfolioPositions(brokerAccountId)).thenReturn(List.of(portfolioPosition));
    }

}