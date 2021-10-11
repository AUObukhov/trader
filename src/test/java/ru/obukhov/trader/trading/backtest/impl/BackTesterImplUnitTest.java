package ru.obukhov.trader.trading.backtest.impl;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.properties.BackTestProperties;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.test.utils.matchers.BigDecimalMatcher;
import ru.obukhov.trader.trading.bots.impl.FakeBotFactory;
import ru.obukhov.trader.trading.bots.interfaces.FakeBot;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.obukhov.trader.trading.model.BackTestPosition;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;
import ru.obukhov.trader.trading.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.OperationTypeWithCommission;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@ExtendWith(MockitoExtension.class)
class BackTesterImplUnitTest {

    private static final String DATE_TIME_REGEX_PATTERN = "[\\d\\-\\+\\.:T]+";

    private static final ConservativeStrategy CONSERVATIVE_STRATEGY = new ConservativeStrategy(StringUtils.EMPTY, 0.0);

    private static final CronExpression BALANCE_INCREMENT_CRON = TestData.createCronExpression();

    @Mock
    private ExcelService excelService;
    @Mock
    private FakeBotFactory fakeBotFactory;
    @Mock
    private FakeTinkoffService fakeTinkoffService;
    @Mock
    private TradingStrategyFactory strategyFactory;

    private final BackTestProperties backTestProperties = new BackTestProperties(2);

    // region test tests

    @Test
    void test_throwsIllegalArgumentException_whenFromIsInFuture() {
        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

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
        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

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
        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

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
        final BotConfig botConfig = new BotConfig(
                "2000124699",
                ticker,
                CandleResolution._1MIN,
                0.003,
                StrategyType.CONSERVATIVE
        );
        mockStrategy(botConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(AbstractTradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void test_fillsCommonStatistics(final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final BotConfig botConfig = new BotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);
        mockStrategy(botConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(AbstractTradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BigDecimal initialInvestment = BigDecimal.valueOf(10000);
        final BalanceConfig balanceConfig = new BalanceConfig(initialInvestment, BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<BotConfig> botConfigs = List.of(botConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.any(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(new DecisionData());

        mockDecisionDataWithCandle(botConfig, fakeBot, 1000, from.plusMinutes(10));
        mockDecisionDataWithCandle(botConfig, fakeBot, 1001, from.plusMinutes(20));
        mockDecisionDataWithCandle(botConfig, fakeBot, 1002, from.plusMinutes(30));
        mockDecisionDataWithCandle(botConfig, fakeBot, 1003, from.plusMinutes(40));
        final Candle lastCandle = mockDecisionDataWithCandle(botConfig, fakeBot, 1004,
                from.plusMinutes(50));

        mockNextMinute(from);
        mockInitialInvestment(MarketInstrument.getCurrency(), from, initialInvestment);

        final BigDecimal currentBalance = BigDecimal.valueOf(2000);
        Mockito.when(fakeTinkoffService.getCurrentBalance(brokerAccountId, MarketInstrument.getCurrency())).thenReturn(currentBalance);

        final int positionLotsCount = 8;
        final PortfolioPosition portfolioPosition = TestData.createPortfolioPosition(ticker, positionLotsCount);
        mockPortfolioPositions(brokerAccountId, portfolioPosition);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());

        final BackTestResult backTestResult = backTestResults.get(0);

        Assertions.assertEquals(botConfigs.get(0), backTestResult.getBotConfig());
        Assertions.assertEquals(interval, backTestResult.getInterval());
        AssertUtils.assertEquals(initialInvestment, backTestResult.getBalances().getInitialInvestment());
        AssertUtils.assertEquals(initialInvestment, backTestResult.getBalances().getTotalInvestment());

        final BigDecimal positionsPrice = DecimalUtils.multiply(lastCandle.getClosePrice(), positionLotsCount);
        final BigDecimal expectedFinalTotalSavings = currentBalance.add(positionsPrice);
        AssertUtils.assertEquals(expectedFinalTotalSavings, backTestResult.getBalances().getFinalTotalSavings());

        AssertUtils.assertEquals(currentBalance, backTestResult.getBalances().getFinalBalance());
        AssertUtils.assertEquals(initialInvestment, backTestResult.getBalances().getWeightedAverageInvestment());

        final BigDecimal expectedAbsoluteProfit = currentBalance.subtract(initialInvestment).add(positionsPrice);
        AssertUtils.assertEquals(expectedAbsoluteProfit, backTestResult.getProfits().getAbsolute());
        AssertUtils.assertEquals(0.0032, backTestResult.getProfits().getRelative());
        AssertUtils.assertEquals(2.21213, backTestResult.getProfits().getRelativeAnnual());

        Assertions.assertNull(backTestResult.getError());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void test_callsBalanceIncrement(final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final BotConfig botConfig = new BotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);
        mockStrategy(botConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(AbstractTradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<BotConfig> botConfigs = List.of(botConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final Candle candle0 = TestData.createCandleWithClosePrice(100);
        final DecisionData decisionData1 = TestData.createDecisionData(candle0);

        final Candle candle1 = TestData.createCandleWithClosePrice(200);
        final DecisionData decisionData2 = TestData.createDecisionData(candle1);

        final Candle candle2 = TestData.createCandleWithClosePrice(300);
        final DecisionData decisionData3 = TestData.createDecisionData(candle2);

        final Candle candle3 = TestData.createCandleWithClosePrice(400);
        final DecisionData decisionData4 = TestData.createDecisionData(candle3);

        final Candle candle4 = TestData.createCandleWithClosePrice(500);
        final DecisionData decisionData5 = TestData.createDecisionData(candle4);

        Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData1, decisionData2, decisionData3, decisionData4, decisionData5);

        mockNextMinute(from);

        mockInitialInvestment(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());

        final BigDecimal currentBalance = BigDecimal.valueOf(20000);
        Mockito.when(fakeTinkoffService.getCurrentBalance(brokerAccountId, MarketInstrument.getCurrency())).thenReturn(currentBalance);

        final int positionLotsCount = 2;
        final PortfolioPosition portfolioPosition = TestData.createPortfolioPosition(ticker, positionLotsCount);
        mockPortfolioPositions(brokerAccountId, portfolioPosition);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());

        final BackTestResult backTestResult = backTestResults.get(0);

        Assertions.assertNull(backTestResult.getError());

        Mockito.verify(fakeTinkoffService, Mockito.times(24))
                .incrementBalance(
                        Mockito.eq(brokerAccountId),
                        Mockito.eq(MarketInstrument.getCurrency()),
                        ArgumentMatchers.argThat(BigDecimalMatcher.of(balanceConfig.getBalanceIncrement()))
                );
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void test_fillsPositions(final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final BotConfig botConfig = new BotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);
        mockStrategy(botConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(AbstractTradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<BotConfig> botConfigs = List.of(botConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final Candle candle0 = TestData.createCandleWithClosePrice(100);
        final DecisionData decisionData1 = TestData.createDecisionData(candle0);

        final Candle candle1 = TestData.createCandleWithClosePrice(200);
        final DecisionData decisionData2 = TestData.createDecisionData(candle1);

        final Candle candle2 = TestData.createCandleWithClosePrice(300);
        final DecisionData decisionData3 = TestData.createDecisionData(candle2);

        final Candle candle3 = TestData.createCandleWithClosePrice(400);
        final DecisionData decisionData4 = TestData.createDecisionData(candle3);

        final Candle candle4 = TestData.createCandleWithClosePrice(500);
        final DecisionData decisionData5 = TestData.createDecisionData(candle4);

        Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData1, decisionData2, decisionData3, decisionData4, decisionData5);

        mockNextMinute(from);

        mockInitialInvestment(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(brokerAccountId, MarketInstrument.getCurrency())).thenReturn(BigDecimal.valueOf(20000));

        final int positionLotsCount = 2;
        final PortfolioPosition portfolioPosition = TestData.createPortfolioPosition(ticker, positionLotsCount);
        mockPortfolioPositions(brokerAccountId, portfolioPosition);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());

        final BackTestResult backTestResult = backTestResults.get(0);

        final List<BackTestPosition> positions = backTestResult.getPositions();
        Assertions.assertEquals(1, positions.size());
        final BackTestPosition backTestPosition = positions.get(0);
        Assertions.assertEquals(ticker, backTestPosition.getTicker());
        Assertions.assertEquals(candle4.getClosePrice(), backTestPosition.getPrice());
        Assertions.assertEquals(positionLotsCount, backTestPosition.getQuantity());

        Assertions.assertNull(backTestResult.getError());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void test_fillsOperations(final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final double commission = 0.003;
        final BotConfig botConfig = new BotConfig(brokerAccountId, ticker, CandleResolution._1MIN, commission, StrategyType.CONSERVATIVE);
        mockStrategy(botConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(AbstractTradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<BotConfig> botConfigs = List.of(botConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestData.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestData.createDecisionData(candle);
        Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialInvestment(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(brokerAccountId, MarketInstrument.getCurrency())).thenReturn(BigDecimal.valueOf(20000));

        final OffsetDateTime operationDateTime = from.plusMinutes(2);
        final OperationTypeWithCommission operationType = OperationTypeWithCommission.BUY;
        final BigDecimal operationPrice = BigDecimal.valueOf(100);
        final int operationQuantity = 2;
        final BigDecimal operationCommission = BigDecimal.valueOf(commission);
        final Operation operation = TestData.createTinkoffOperation(
                operationDateTime,
                operationType,
                operationPrice,
                operationQuantity,
                operationCommission
        );
        Mocker.mockTinkoffOperations(fakeTinkoffService, brokerAccountId, ticker, interval, operation);

        mockPortfolioPositions(brokerAccountId);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());

        final BackTestResult backTestResult = backTestResults.get(0);

        List<BackTestOperation> resultOperations = backTestResult.getOperations();
        Assertions.assertEquals(1, resultOperations.size());
        final BackTestOperation backTestOperation = resultOperations.get(0);
        Assertions.assertEquals(ticker, backTestOperation.getTicker());
        Assertions.assertEquals(operationDateTime, backTestOperation.getDateTime());
        Assertions.assertEquals(OperationType.BUY, backTestOperation.getOperationType());
        AssertUtils.assertEquals(operationPrice, backTestOperation.getPrice());
        Assertions.assertEquals(operationQuantity, backTestOperation.getQuantity());
        AssertUtils.assertEquals(operationCommission, backTestOperation.getCommission());

        Assertions.assertNull(backTestResult.getError());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void test_fillsCandles(final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final BotConfig botConfig = new BotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);
        mockStrategy(botConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(AbstractTradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), null, BALANCE_INCREMENT_CRON);

        final List<BotConfig> botConfigs = List.of(botConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final List<Candle> candles = mockDecisionDataWithCandles(botConfig, fakeBot, from, to);

        mockNextMinute(from);

        mockInitialInvestment(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(brokerAccountId, MarketInstrument.getCurrency()))
                .thenReturn(BigDecimal.valueOf(20000));

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());
        final BackTestResult backTestResult = backTestResults.get(0);
        AssertUtils.assertListsAreEqual(candles, backTestResult.getCandles());
        Assertions.assertNull(backTestResult.getError());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void test_notFillsCandles_whenCurrentCandlesInDecisionDataIsNull(final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final BotConfig botConfig = new BotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);
        mockStrategy(botConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(AbstractTradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), null, BALANCE_INCREMENT_CRON);

        final List<BotConfig> botConfigs = List.of(botConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(new DecisionData());

        mockNextMinute(from);

        mockInitialInvestment(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(brokerAccountId, MarketInstrument.getCurrency())).thenReturn(BigDecimal.valueOf(20000));

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());

        final BackTestResult backTestResult = backTestResults.get(0);

        Assertions.assertTrue(backTestResult.getCandles().isEmpty());
        Assertions.assertNull(backTestResult.getError());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void test_notFillsCandles_whenCurrentCandlesInDecisionDataIsEmpty(final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final BotConfig botConfig = new BotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);
        mockStrategy(botConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(AbstractTradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), null, BALANCE_INCREMENT_CRON);

        final List<BotConfig> botConfigs = List.of(botConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(TestData.createDecisionData());

        mockNextMinute(from);

        mockInitialInvestment(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(brokerAccountId, MarketInstrument.getCurrency()))
                .thenReturn(BigDecimal.valueOf(20000));

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());

        final BackTestResult backTestResult = backTestResults.get(0);

        Assertions.assertTrue(backTestResult.getCandles().isEmpty());
        Assertions.assertNull(backTestResult.getError());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void test_callsSaveToFile_whenSaveToFilesIsTrue(final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final BotConfig botConfig = new BotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);
        mockStrategy(botConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(AbstractTradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<BotConfig> botConfigs = List.of(botConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestData.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestData.createDecisionData(candle);
        Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialInvestment(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(brokerAccountId, MarketInstrument.getCurrency())).thenReturn(BigDecimal.ZERO);

        Mocker.mockTinkoffOperations(fakeTinkoffService, brokerAccountId, ticker, interval);
        mockPortfolioPositions(brokerAccountId);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, true);

        // assert

        Assertions.assertEquals(1, backTestResults.size());

        final BackTestResult backTestResult = backTestResults.get(0);
        Assertions.assertNull(backTestResult.getError());

        Mockito.verify(excelService, Mockito.only()).saveBackTestResults(Mockito.anyCollection());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void test_neverCallsSaveToFile_whenSaveToFilesIsFalse(final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final BotConfig botConfig = new BotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.093, StrategyType.CONSERVATIVE);
        mockStrategy(botConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(AbstractTradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<BotConfig> botConfigs = List.of(botConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestData.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestData.createDecisionData(candle);
        Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialInvestment(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(brokerAccountId, MarketInstrument.getCurrency())).thenReturn(BigDecimal.ZERO);

        Mocker.mockTinkoffOperations(fakeTinkoffService, brokerAccountId, ticker, interval);
        mockPortfolioPositions(brokerAccountId);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());

        final BackTestResult backTestResult = backTestResults.get(0);
        Assertions.assertNull(backTestResult.getError());

        Mockito.verify(excelService, Mockito.never()).saveBackTestResults(Mockito.any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void test_returnsZeroInvestmentsAndProfits_whenNoInvestments(final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final BotConfig botConfig = new BotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);
        mockStrategy(botConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(AbstractTradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.ZERO, null, BALANCE_INCREMENT_CRON);

        final List<BotConfig> botConfigs = List.of(botConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestData.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestData.createDecisionData(candle);

        Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialInvestment(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(brokerAccountId, MarketInstrument.getCurrency()))
                .thenReturn(BigDecimal.valueOf(20000));

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());

        final BackTestResult backTestResult = backTestResults.get(0);

        Assertions.assertNull(backTestResult.getError());

        AssertUtils.assertEquals(0, backTestResult.getBalances().getTotalInvestment());
        AssertUtils.assertEquals(0, backTestResult.getBalances().getWeightedAverageInvestment());
        AssertUtils.assertEquals(0, backTestResult.getProfits().getRelative());
        AssertUtils.assertEquals(0, backTestResult.getProfits().getRelativeAnnual());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void test_catchesBackTestException(final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final BotConfig botConfig = new BotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);
        mockStrategy(botConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        final String mockedExceptionMessage = "mocked exception";
        Mockito.when(fakeBot.getFakeTinkoffService()).thenThrow(new IllegalArgumentException(mockedExceptionMessage));
        Mockito.when(fakeBotFactory.createBot(Mockito.any(AbstractTradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<BotConfig> botConfigs = List.of(botConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, backTestResults.size());

        final BackTestResult backTestResult = backTestResults.get(0);

        final String expectedErrorPattern = String.format(
                "^Back test for '\\[brokerAccountId=%s, ticker=%s, candleResolution=1min, commission=0.003, strategyType=conservative, " +
                        "strategyParams=\\{\\}\\]' failed within 00:00:00.\\d\\d\\d with error: %s$",
                brokerAccountId, ticker, mockedExceptionMessage
        );
        AssertUtils.assertMatchesRegex(backTestResult.getError(), expectedErrorPattern);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void test_catchesSaveToFileException(final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final BotConfig botConfig = new BotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.003, StrategyType.CONSERVATIVE);
        mockStrategy(botConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(AbstractTradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final BackTesterImpl backTester = new BackTesterImpl(excelService, fakeBotFactory, strategyFactory, backTestProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<BotConfig> botConfigs = List.of(botConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestData.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestData.createDecisionData(candle);
        Mockito.when(fakeBot.processBotConfig(Mockito.eq(botConfig), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialInvestment(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(brokerAccountId, MarketInstrument.getCurrency())).thenReturn(BigDecimal.ZERO);

        Mocker.mockTinkoffOperations(fakeTinkoffService, brokerAccountId, ticker, interval);
        mockPortfolioPositions(brokerAccountId);

        Mockito.doThrow(new IllegalArgumentException())
                .when(excelService)
                .saveBackTestResults(Mockito.anyCollection());

        // act

        final List<BackTestResult> backTestResults = backTester.test(botConfigs, balanceConfig, interval, true);

        // assert

        Assertions.assertEquals(1, backTestResults.size());

        final BackTestResult backTestResult = backTestResults.get(0);

        Assertions.assertNull(backTestResult.getError());
    }

    private void mockStrategy(final BotConfig botConfig, final AbstractTradingStrategy strategy) {
        Mockito.when(strategyFactory.createStrategy(botConfig)).thenReturn(strategy);
    }

    private FakeBot createFakeBotMock() {
        final FakeBot fakeBot = Mockito.mock(FakeBot.class);
        Mockito.when(fakeBot.getFakeTinkoffService()).thenReturn(fakeTinkoffService);
        return fakeBot;
    }

    private Candle mockDecisionDataWithCandle(final BotConfig botConfig, final FakeBot fakeBot, final double closePrice, final OffsetDateTime time) {
        final Candle candle = TestData.createCandleWithClosePriceAndTime(closePrice, time);
        final DecisionData decisionData = TestData.createDecisionData(candle);

        Mockito.when(fakeBot.processBotConfig(botConfig, null, time)).thenReturn(decisionData);

        return candle;
    }

    private List<Candle> mockDecisionDataWithCandles(
            final BotConfig botConfig,
            final FakeBot fakeBot,
            final OffsetDateTime from,
            final OffsetDateTime to
    ) {
        final List<Candle> candles = new ArrayList<>();

        // mocking first DecisionData with previousStartTime = null
        candles.add(new Candle().setTime(from));
        DecisionData decisionData = new DecisionData().setCurrentCandles(new ArrayList<>(candles));
        Mockito.when(fakeBot.processBotConfig(botConfig, null, from))
                .thenReturn(decisionData);

        // mocking DecisionData for all last minutes
        for (OffsetDateTime dateTime = from.plusMinutes(1); dateTime.isBefore(to); dateTime = dateTime.plusMinutes(1)) {
            candles.add(new Candle().setTime(dateTime));
            decisionData = new DecisionData().setCurrentCandles(new ArrayList<>(candles));
            Mockito.when(fakeBot.processBotConfig(botConfig, from, dateTime))
                    .thenReturn(decisionData);
        }

        return candles;
    }

    private void mockNextMinute(final OffsetDateTime from) {
        Mockito.when(fakeTinkoffService.getCurrentDateTime()).thenReturn(from);

        Mockito.when(fakeTinkoffService.nextMinute()).thenAnswer(invocationOnMock -> {
            final OffsetDateTime currentDateTime = fakeTinkoffService.getCurrentDateTime();
            final OffsetDateTime nextMinute = currentDateTime.plusMinutes(1);
            Mockito.when(fakeTinkoffService.getCurrentDateTime()).thenReturn(nextMinute);
            return nextMinute;
        });
    }

    private void mockInitialInvestment(final Currency currency, final OffsetDateTime dateTime, final BigDecimal initialInvestment) {
        final SortedMap<OffsetDateTime, BigDecimal> investments = new TreeMap<>();
        investments.put(dateTime, initialInvestment);
        Mockito.when(fakeTinkoffService.getInvestments(currency)).thenReturn(investments);
    }

    private void mockPortfolioPositions(final String brokerAccountId, final PortfolioPosition... portfolioPositions) {
        Mockito.when(fakeTinkoffService.getPortfolioPositions(brokerAccountId)).thenReturn(List.of(portfolioPositions));
    }

    // endregion

}