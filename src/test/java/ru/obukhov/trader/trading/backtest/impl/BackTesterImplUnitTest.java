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
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.OperationTypeMapper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.test.utils.matchers.BigDecimalMatcher;
import ru.obukhov.trader.trading.bots.impl.FakeBot;
import ru.obukhov.trader.trading.bots.impl.FakeBotFactory;
import ru.obukhov.trader.trading.bots.impl.FakeTinkoffServiceFactory;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.obukhov.trader.trading.model.BackTestPosition;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.OperationTypeWithCommission;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
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
    private FakeTinkoffServiceFactory fakeTinkoffServiceFactory;
    @Mock
    private FakeBotFactory fakeBotFactory;

    private BackTesterImpl backTester;

    @BeforeEach
    void setUp() {
        backTester = new BackTesterImpl(excelService, fakeTinkoffServiceFactory, fakeBotFactory, BACK_TEST_PROPERTIES);
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
    void test_returnsResultWithEmptyValues_whenTickerNotFound() {
        // arrange

        final String ticker = "ticker";
        final Double commission = 0.003;
        final BotConfig botConfig = TestData.createBotConfig("2000124699", ticker, commission);

        mockFakeTinkoffService(commission);

        final BalanceConfig balanceConfig = TestData.createBalanceConfig(10000.0, 1000.0);

        final List<BotConfig> botConfigs = List.of(botConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

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
                "^Back test for '\\[brokerAccountId=2000124699, ticker=%1$s, candleResolution=1min, commission=0.003, strategyType=conservative, " +
                        "strategyParams=\\{\\}\\]' " +
                        "failed within 00:00:00.\\d\\d\\d with error: Not found instrument for ticker '%1$s'$",
                ticker
        );
        AssertUtils.assertMatchesRegex(backTestResult.getError(), expectedErrorPattern);
    }

    @Test
    void test_fillsCommonStatistics() {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;

        final BigDecimal finalBalance1 = BigDecimal.valueOf(2000);
        final int finalPositionLotsCount1 = 8;

        final Map<Double, OffsetDateTime> prices1 = new LinkedHashMap<>();
        prices1.put(1000.0, from.plusMinutes(10));
        prices1.put(1001.0, from.plusMinutes(20));
        prices1.put(1002.0, from.plusMinutes(30));
        prices1.put(1003.0, from.plusMinutes(40));
        final double finalPrice1 = 1004.0;
        prices1.put(finalPrice1, from.plusMinutes(50));

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                0.003,
                initialInvestment,
                interval,
                BigDecimal.valueOf(2000),
                finalPositionLotsCount1,
                prices1,
                finalPrice1,
                null
        );

        final BigDecimal finalBalance2 = BigDecimal.valueOf(100);
        final int finalPositionLotsCount2 = 5;

        final Map<Double, OffsetDateTime> prices2 = new LinkedHashMap<>();
        prices2.put(2000.0, from.plusMinutes(100));
        prices2.put(2001.0, from.plusMinutes(200));
        prices2.put(2002.0, from.plusMinutes(300));
        prices2.put(2003.0, from.plusMinutes(400));
        final double finalPrice2 = 2004.0;
        prices2.put(finalPrice2, from.plusMinutes(500));

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                "ticker2",
                0.001,
                initialInvestment,
                interval,
                finalBalance2,
                finalPositionLotsCount2,
                prices2,
                finalPrice2,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

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
    void test_callsAddInvestment() {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final double balanceIncrement = 1000;

        final String brokerAccountId1 = null;
        final String ticker1 = "ticker1";
        final Double commission1 = 0.003;

        final Map<Double, OffsetDateTime> prices1 = new LinkedHashMap<>();
        prices1.put(100.0, from.plusMinutes(10));
        prices1.put(200.0, from.plusMinutes(20));
        prices1.put(300.0, from.plusMinutes(30));
        prices1.put(400.0, from.plusMinutes(40));
        prices1.put(500.0, from.plusMinutes(50));

        final BigDecimal currentBalance1 = BigDecimal.valueOf(2000);
        final int positionLotsCount1 = 2;

        final FakeTinkoffService fakeTinkoffService1 = mockFakeTinkoffService(commission1);
        final FakeBot fakeBot1 = mockFakeBot(fakeTinkoffService1);
        final MarketInstrument marketInstrument1 = Mocker.createAndMockInstrument(fakeTinkoffService1, ticker1, 10);

        final BotConfig botConfig1 = TestData.createBotConfig(brokerAccountId1, ticker1, commission1);

        mockDecisionDataWithCandles(botConfig1, fakeBot1, prices1);
        mockCurrentPrice(fakeTinkoffService1, ticker1, 500);
        mockNextMinute(fakeTinkoffService1, from);
        mockInvestments(fakeTinkoffService1, marketInstrument1.getCurrency(), from, balanceIncrement);
        Mockito.when(fakeTinkoffService1.getCurrentBalance(brokerAccountId1, marketInstrument1.getCurrency())).thenReturn(currentBalance1);
        mockPortfolioPosition(fakeTinkoffService1, brokerAccountId1, ticker1, positionLotsCount1);

        final String brokerAccountId2 = "2000124699";
        final String ticker2 = "ticker1";
        final Double commission2 = 0.001;

        final Map<Double, OffsetDateTime> prices2 = new LinkedHashMap<>();
        prices2.put(10.0, from.plusMinutes(100));
        prices2.put(20.0, from.plusMinutes(200));
        prices2.put(30.0, from.plusMinutes(300));
        prices2.put(40.0, from.plusMinutes(400));
        prices2.put(50.0, from.plusMinutes(500));

        final BigDecimal currentBalance2 = BigDecimal.valueOf(2000);
        final int positionLotsCount2 = 2;

        final FakeTinkoffService fakeTinkoffService2 = mockFakeTinkoffService(commission2);
        final FakeBot fakeBot2 = mockFakeBot(fakeTinkoffService2);
        final MarketInstrument marketInstrument2 = Mocker.createAndMockInstrument(fakeTinkoffService2, ticker2, 10);

        final BotConfig botConfig2 = TestData.createBotConfig(brokerAccountId2, ticker2, commission2);

        mockDecisionDataWithCandles(botConfig2, fakeBot2, prices2);
        mockCurrentPrice(fakeTinkoffService2, ticker2, 50);
        mockNextMinute(fakeTinkoffService2, from);
        mockInvestments(fakeTinkoffService2, marketInstrument2.getCurrency(), from, balanceIncrement);
        Mockito.when(fakeTinkoffService2.getCurrentBalance(brokerAccountId2, marketInstrument2.getCurrency())).thenReturn(currentBalance2);
        mockPortfolioPosition(fakeTinkoffService2, brokerAccountId2, ticker2, positionLotsCount2);

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, balanceIncrement, BALANCE_INCREMENT_CRON);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        Assertions.assertNull(backTestResults.get(0).getError());
        Assertions.assertNull(backTestResults.get(1).getError());

        Mockito.verify(fakeTinkoffService1, Mockito.times(24))
                .addInvestment(
                        Mockito.eq(brokerAccountId1),
                        Mockito.any(OffsetDateTime.class),
                        Mockito.eq(marketInstrument1.getCurrency()),
                        ArgumentMatchers.argThat(BigDecimalMatcher.of(balanceIncrement))
                );

        Mockito.verify(fakeTinkoffService2, Mockito.times(24))
                .addInvestment(
                        Mockito.eq(brokerAccountId2),
                        Mockito.any(OffsetDateTime.class),
                        Mockito.eq(marketInstrument2.getCurrency()),
                        ArgumentMatchers.argThat(BigDecimalMatcher.of(balanceIncrement))
                );
    }

    @Test
    void test_fillsPositions() {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;

        final String ticker1 = "ticker1";

        final Map<Double, OffsetDateTime> prices1 = new LinkedHashMap<>();
        prices1.put(100.0, from.plusMinutes(10));
        prices1.put(200.0, from.plusMinutes(20));
        prices1.put(300.0, from.plusMinutes(30));
        prices1.put(400.0, from.plusMinutes(40));
        final double currentPrice1 = 500.0;
        prices1.put(currentPrice1, from.plusMinutes(50));

        final int positionLotsCount1 = 2;

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                ticker1,
                0.003,
                initialInvestment,
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
                initialInvestment,
                interval,
                BigDecimal.valueOf(10000),
                positionLotsCount2,
                null,
                currentPrice2,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

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
    void test_fillsOperations() {

        // arrange

        final double initialInvestment = 10000;

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final String ticker1 = "ticker1";
        final double commission1 = 0.003;

        final Map<Double, OffsetDateTime> prices1 = Map.of(100.0, from.plusMinutes(1));
        final BigDecimal currentBalance1 = BigDecimal.valueOf(20000);

        final int positionLotsCount1 = 1;

        final OffsetDateTime operationDateTime1 = from.plusMinutes(2);
        final OperationTypeWithCommission operationType1 = OperationTypeWithCommission.BUY;
        final BigDecimal operationPrice1 = BigDecimal.valueOf(100);
        final int operationQuantity1 = 2;
        final BigDecimal operationCommission1 = BigDecimal.valueOf(commission1);
        final Operation operation1 = TestData.createTinkoffOperation(
                operationDateTime1,
                operationType1,
                operationPrice1,
                operationQuantity1,
                operationCommission1
        );

        final String ticker2 = "ticker2";
        final double commission2 = 0.001;

        final Map<Double, OffsetDateTime> prices2 = Map.of(1000.0, from.plusMinutes(3));
        final BigDecimal currentBalance2 = BigDecimal.valueOf(10000);

        final int positionLotsCount2 = 1;

        final OffsetDateTime operationDateTime2 = from.plusMinutes(3);
        final OperationTypeWithCommission operationType2 = OperationTypeWithCommission.SELL;
        final BigDecimal operationPrice2 = BigDecimal.valueOf(1000);
        final int operationQuantity2 = 4;
        final BigDecimal operationCommission2 = BigDecimal.valueOf(commission2);
        final Operation operation2 = TestData.createTinkoffOperation(
                operationDateTime2,
                operationType2,
                operationPrice2,
                operationQuantity2,
                operationCommission2
        );

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                ticker1,
                commission1,
                initialInvestment,
                interval,
                currentBalance1,
                positionLotsCount1,
                prices1,
                100,
                operation1
        );

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                ticker2,
                commission2,
                initialInvestment,
                interval,
                currentBalance2,
                positionLotsCount2,
                prices2,
                1000,
                operation2
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

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
                operationCommission1
        );

        assertOperation(
                backTestResults.get(1),
                ticker2,
                operationDateTime2,
                operationMapper.map(operationType2),
                operationPrice2,
                operationQuantity2,
                operationCommission2
        );
    }

    private void assertOperation(
            final BackTestResult backTestResult,
            final String expectedTicker,
            final OffsetDateTime expectedOperationDateTime,
            final OperationType expectedOperationType,
            final BigDecimal expectedOperationPrice,
            final int expectedOperationQuantity,
            final BigDecimal expectedOperationCommission
    ) {
        Assertions.assertNull(backTestResult.getError());

        final List<BackTestOperation> resultOperations = backTestResult.getOperations();
        Assertions.assertEquals(1, resultOperations.size());

        final BackTestOperation backTestOperation = resultOperations.get(0);
        Assertions.assertEquals(expectedTicker, backTestOperation.getTicker());
        Assertions.assertEquals(expectedOperationDateTime, backTestOperation.getDateTime());
        Assertions.assertEquals(expectedOperationType, backTestOperation.getOperationType());
        AssertUtils.assertEquals(expectedOperationPrice, backTestOperation.getPrice());
        Assertions.assertEquals(expectedOperationQuantity, backTestOperation.getQuantity());
        AssertUtils.assertEquals(expectedOperationCommission, backTestOperation.getCommission());
    }

    @Test
    void test_fillsCandles() {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;

        final Map<Double, OffsetDateTime> prices1 = new LinkedHashMap<>();
        prices1.put(100.0, from.plusMinutes(1));
        prices1.put(200.0, from.plusMinutes(2));
        prices1.put(300.0, from.plusMinutes(3));
        prices1.put(400.0, from.plusMinutes(4));
        prices1.put(500.0, from.plusMinutes(5));

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                0.003,
                initialInvestment,
                interval,
                BigDecimal.valueOf(20000),
                1,
                prices1,
                500,
                null
        );

        final Map<Double, OffsetDateTime> prices2 = new LinkedHashMap<>();
        prices2.put(1000.0, from.plusMinutes(10));
        prices2.put(2000.0, from.plusMinutes(20));
        prices2.put(3000.0, from.plusMinutes(30));
        prices2.put(4000.0, from.plusMinutes(40));

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                "ticker2",
                0.001,
                initialInvestment,
                interval,
                BigDecimal.valueOf(10000),
                2,
                prices2,
                4000,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());
        assertCandles(backTestResults.get(0), prices1);
        assertCandles(backTestResults.get(1), prices2);
    }

    private void assertCandles(final BackTestResult backTestResult, final Map<Double, OffsetDateTime> prices) {
        Assertions.assertNull(backTestResult.getError());

        final List<Candle> candles = backTestResult.getCandles();
        Assertions.assertEquals(prices.size(), candles.size());

        final Iterator<Candle> candlesIterator = candles.iterator();
        for (final Map.Entry<Double, OffsetDateTime> entry : prices.entrySet()) {
            final Candle candle = candlesIterator.next();
            AssertUtils.assertEquals(entry.getKey(), candle.getClosePrice());
            Assertions.assertEquals(entry.getValue(), candle.getTime());
        }
    }

    @Test
    void test_notFillsCandles_whenCurrentCandlesInDecisionDataIsAlwaysNullOrEmpty() {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                0.003,
                initialInvestment,
                interval,
                BigDecimal.valueOf(20000),
                null,
                null,
                300,
                null
        );

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                "ticker2",
                0.001,
                initialInvestment,
                interval,
                BigDecimal.valueOf(10000),
                null,
                Map.of(),
                200,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

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
    void test_callsSaveToFile_whenSaveToFilesIsTrue() {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BigDecimal currentBalance = BigDecimal.ZERO;

        final double commission1 = 0.001;
        final Operation operation = TestData.createTinkoffOperation(
                from.plusMinutes(2),
                OperationTypeWithCommission.BUY,
                BigDecimal.valueOf(100),
                2,
                BigDecimal.valueOf(commission1)
        );

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                commission1,
                initialInvestment,
                interval,
                currentBalance,
                1,
                Map.of(100.0, from.plusMinutes(1)),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                "ticker2",
                0.003,
                initialInvestment,
                interval,
                currentBalance,
                null,
                null,
                200,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, true);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        Assertions.assertNull(backTestResults.get(0).getError());
        Assertions.assertNull(backTestResults.get(1).getError());

        Mockito.verify(excelService, Mockito.only()).saveBackTestResults(Mockito.anyCollection());
    }

    @Test
    void test_neverCallsSaveToFile_whenSaveToFilesIsFalse() {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BigDecimal currentBalance = BigDecimal.ZERO;

        final double commission1 = 0.001;
        final Operation operation = TestData.createTinkoffOperation(
                from.plusMinutes(2),
                OperationTypeWithCommission.BUY,
                BigDecimal.valueOf(100),
                2,
                BigDecimal.valueOf(commission1)
        );

        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                commission1,
                initialInvestment,
                interval,
                currentBalance,
                1,
                Map.of(100.0, from.plusMinutes(1)),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                "ticker2",
                0.003,
                initialInvestment,
                interval,
                currentBalance,
                null,
                null,
                150,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        Assertions.assertNull(backTestResults.get(0).getError());
        Assertions.assertNull(backTestResults.get(1).getError());

        Mockito.verify(excelService, Mockito.never()).saveBackTestResults(Mockito.any());
    }

    @Test
    void test_returnsZeroInvestmentsAndProfits_whenNoInvestments() {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 0;
        final BigDecimal currentBalance = BigDecimal.valueOf(20000);

        final double commission1 = 0.003;
        final Operation operation = TestData.createTinkoffOperation(
                from.plusMinutes(2),
                OperationTypeWithCommission.BUY,
                BigDecimal.valueOf(100),
                2,
                BigDecimal.valueOf(commission1)
        );
        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                commission1,
                initialInvestment,
                interval,
                currentBalance,
                10,
                Map.of(100.0, from.plusMinutes(1)),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                null,
                "ticker2",
                0.001,
                initialInvestment,
                interval,
                currentBalance,
                null,
                null,
                300,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);
        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment);

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
    void test_catchesBackTestException() {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final String brokerAccountId1 = null;
        final String ticker1 = "ticker1";
        final Double commission1 = 0.003;

        mockFakeTinkoffService(commission1);

        final BotConfig botConfig1 = TestData.createBotConfig(brokerAccountId1, ticker1, commission1);
        final String mockedExceptionMessage1 = "mocked exception 1";
        Mockito.when(fakeBotFactory.createBot(Mockito.eq(botConfig1), Mockito.any(FakeTinkoffService.class)))
                .thenThrow(new IllegalArgumentException(mockedExceptionMessage1));

        final String brokerAccountId2 = "2000124699";
        final String ticker2 = "ticker2";
        final Double commission2 = 0.001;

        mockFakeTinkoffService(commission2);

        final BotConfig botConfig2 = TestData.createBotConfig(brokerAccountId2, ticker2, commission2);
        final String mockedExceptionMessage2 = "mocked exception 2";
        Mockito.when(fakeBotFactory.createBot(Mockito.eq(botConfig2), Mockito.any(FakeTinkoffService.class)))
                .thenThrow(new IllegalArgumentException(mockedExceptionMessage2));

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        final BalanceConfig balanceConfig = TestData.createBalanceConfig(10000.0, 1000.0);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(2, backTestResults.size());

        final String expectedErrorPattern1 = String.format(
                "^Back test for '\\[brokerAccountId=%s, ticker=%s, candleResolution=1min, commission=%s, strategyType=conservative, " +
                        "strategyParams=\\{\\}\\]' failed within 00:00:00.\\d\\d\\d with error: %s$",
                brokerAccountId1, ticker1, commission1, mockedExceptionMessage1
        );
        AssertUtils.assertMatchesRegex(backTestResults.get(0).getError(), expectedErrorPattern1);

        final String expectedErrorPattern2 = String.format(
                "^Back test for '\\[brokerAccountId=%s, ticker=%s, candleResolution=1min, commission=%s, strategyType=conservative, " +
                        "strategyParams=\\{\\}\\]' failed within 00:00:00.\\d\\d\\d with error: %s$",
                brokerAccountId2, ticker2, commission2, mockedExceptionMessage2
        );
        AssertUtils.assertMatchesRegex(backTestResults.get(1).getError(), expectedErrorPattern2);
    }

    @Test
    void test_catchesSaveToFileException_andFinishesBackTestsSuccessfully() {

        // arrange

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final double initialInvestment = 10000;
        final BigDecimal currentBalance = BigDecimal.ZERO;

        final double commission1 = 0.003;
        final Operation operation = TestData.createTinkoffOperation(
                from.plusMinutes(2),
                OperationTypeWithCommission.BUY,
                BigDecimal.valueOf(100),
                2,
                BigDecimal.valueOf(commission1)
        );
        final BotConfig botConfig1 = arrangeBackTest(
                null,
                "ticker1",
                commission1,
                initialInvestment,
                interval,
                currentBalance,
                2,
                Map.of(100.0, from.plusMinutes(1)),
                100,
                operation
        );

        final BotConfig botConfig2 = arrangeBackTest(
                "2000124699",
                "ticker2",
                0.001,
                initialInvestment,
                interval,
                currentBalance,
                null,
                null,
                50,
                null
        );

        final List<BotConfig> botConfigs = List.of(botConfig1, botConfig2);

        final BalanceConfig balanceConfig = TestData.createBalanceConfig(initialInvestment, 1000.0, BALANCE_INCREMENT_CRON);

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
            final double initialInvestment,
            final Interval interval,
            final BigDecimal currentBalance,
            final Integer positionLotsCount,
            final Map<Double, OffsetDateTime> prices,
            final double currentPrice,
            final Operation operation
    ) {
        final BotConfig botConfig = TestData.createBotConfig(brokerAccountId, ticker, commission);

        final FakeTinkoffService fakeTinkoffService = mockFakeTinkoffService(botConfig.getCommission());
        final FakeBot fakeBot = mockFakeBot(fakeTinkoffService);

        final MarketInstrument marketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);
        mockDecisionDataWithCandles(botConfig, fakeBot, prices);
        mockNextMinute(fakeTinkoffService, interval.getFrom());
        mockInvestments(fakeTinkoffService, marketInstrument.getCurrency(), interval.getFrom(), initialInvestment);
        Mockito.when(fakeTinkoffService.getCurrentBalance(brokerAccountId, marketInstrument.getCurrency())).thenReturn(currentBalance);
        if (positionLotsCount != null) {
            mockPortfolioPosition(fakeTinkoffService, brokerAccountId, ticker, positionLotsCount);
            mockCurrentPrice(fakeTinkoffService, ticker, currentPrice);
        }
        if (operation != null) {
            Mocker.mockTinkoffOperations(fakeTinkoffService, brokerAccountId, ticker, interval, operation);
        }

        return botConfig;
    }

    private void mockCurrentPrice(final FakeTinkoffService fakeTinkoffService, final String ticker, final double currentPrice) {
        Mockito.when(fakeTinkoffService.getCurrentPrice(ticker))
                .thenReturn(DecimalUtils.setDefaultScale(currentPrice));
    }

    private FakeTinkoffService mockFakeTinkoffService(final Double commission) {
        final FakeTinkoffService fakeTinkoffService = Mockito.mock(FakeTinkoffService.class);
        Mockito.doReturn(fakeTinkoffService).when(fakeTinkoffServiceFactory).createService(Mockito.same(commission));
        return fakeTinkoffService;
    }

    private FakeBot mockFakeBot(final FakeTinkoffService fakeTinkoffService) {
        final FakeBot fakeBot = Mockito.mock(FakeBot.class);
        Mockito.when(fakeBotFactory.createBot(Mockito.any(BotConfig.class), Mockito.eq(fakeTinkoffService))).thenReturn(fakeBot);
        return fakeBot;
    }

    private void mockDecisionDataWithCandles(final BotConfig botConfig, final FakeBot fakeBot, final Map<Double, OffsetDateTime> prices) {
        if (prices == null) {
            Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.nullable(OffsetDateTime.class), Mockito.any(OffsetDateTime.class)))
                    .thenReturn(new DecisionData());
        } else if (prices.isEmpty()) {
            Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.nullable(OffsetDateTime.class), Mockito.any(OffsetDateTime.class)))
                    .thenReturn(TestData.createDecisionData());
        } else {
            Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.nullable(OffsetDateTime.class), Mockito.any(OffsetDateTime.class)))
                    .thenReturn(new DecisionData());

            for (final Map.Entry<Double, OffsetDateTime> entry : prices.entrySet()) {
                final OffsetDateTime dateTime = entry.getValue();
                final Candle candle = TestData.createCandleWithClosePriceAndTime(entry.getKey(), dateTime);
                final DecisionData decisionData = TestData.createDecisionData(candle);

                Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.nullable(OffsetDateTime.class), Mockito.eq(dateTime)))
                        .thenReturn(decisionData);
            }
        }
    }

    private void mockNextMinute(final FakeTinkoffService fakeTinkoffService, final OffsetDateTime from) {
        Mockito.when(fakeTinkoffService.getCurrentDateTime()).thenReturn(from);

        Mockito.when(fakeTinkoffService.nextMinute()).thenAnswer(invocationOnMock -> {
            final OffsetDateTime currentDateTime = fakeTinkoffService.getCurrentDateTime();
            final OffsetDateTime nextMinute = currentDateTime.plusMinutes(1);
            Mockito.when(fakeTinkoffService.getCurrentDateTime()).thenReturn(nextMinute);
            return nextMinute;
        });
    }

    private void mockInvestments(
            final FakeTinkoffService fakeTinkoffService,
            final Currency currency,
            final OffsetDateTime dateTime,
            final double initialInvestment
    ) {
        final SortedMap<OffsetDateTime, BigDecimal> investments = new TreeMap<>();
        investments.put(dateTime, DecimalUtils.setDefaultScale(initialInvestment));
        Mockito.when(fakeTinkoffService.getInvestments(currency)).thenReturn(investments);
    }

    private void mockPortfolioPosition(
            final FakeTinkoffService fakeTinkoffService,
            final String brokerAccountId,
            final String ticker,
            final int positionLotsCount
    ) {
        final PortfolioPosition portfolioPosition = TestData.createPortfolioPosition(ticker, positionLotsCount);
        Mockito.when(fakeTinkoffService.getPortfolioPositions(brokerAccountId)).thenReturn(List.of(portfolioPosition));
    }

}