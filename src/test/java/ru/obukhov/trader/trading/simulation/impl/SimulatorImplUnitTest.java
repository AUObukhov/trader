package ru.obukhov.trader.trading.simulation.impl;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import ru.obukhov.trader.config.properties.SimulationProperties;
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
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.model.TradingStrategyParams;
import ru.obukhov.trader.trading.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.SimulatedOperation;
import ru.obukhov.trader.web.model.SimulatedPosition;
import ru.obukhov.trader.web.model.SimulationResult;
import ru.obukhov.trader.web.model.TradingConfig;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.OperationTypeWithCommission;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@ExtendWith(MockitoExtension.class)
class SimulatorImplUnitTest {

    private static final String DATE_TIME_REGEX_PATTERN = "[\\d\\-\\+\\.:T]+";

    private static final ConservativeStrategy CONSERVATIVE_STRATEGY = new ConservativeStrategy(
            StringUtils.EMPTY,
            new TradingStrategyParams(0.1f),
            null
    );

    private static final CronExpression BALANCE_INCREMENT_CRON = TestData.createCronExpression();

    @Mock
    private ExcelService excelService;
    @Mock
    private FakeBotFactory fakeBotFactory;
    @Mock
    private FakeTinkoffService fakeTinkoffService;
    @Mock
    private TradingStrategyFactory strategyFactory;

    private final SimulationProperties simulationProperties = new SimulationProperties(2);

    // region simulate tests

    @Test
    void simulate_throwsIllegalArgumentException_whenFromIsInFuture() {
        final String ticker = "ticker";

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = Collections.emptyList();

        final OffsetDateTime from = OffsetDateTime.now().plusDays(1);
        final OffsetDateTime to = from.plusDays(1);
        final Interval interval = Interval.of(from, to);

        final String expectedMessagePattern = String.format("^'from' \\(%1$s\\) can't be in future. Now is %1$s$", DATE_TIME_REGEX_PATTERN);

        AssertUtils.assertThrowsWithMessagePattern(
                () -> simulator.simulate(tradingConfigs, balanceConfig, interval, false),
                IllegalArgumentException.class,
                expectedMessagePattern
        );
    }

    @Test
    void simulate_throwsIllegalArgumentException_whenToIsInFuture() {
        final String ticker = "ticker";

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = Collections.emptyList();

        final OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        final OffsetDateTime to = from.plusDays(2);
        final Interval interval = Interval.of(from, to);

        final String expectedMessagePattern = String.format("^'to' \\(%1$s\\) can't be in future. Now is %1$s$", DATE_TIME_REGEX_PATTERN);

        AssertUtils.assertThrowsWithMessagePattern(
                () -> simulator.simulate(tradingConfigs, balanceConfig, interval, false),
                RuntimeException.class,
                expectedMessagePattern
        );
    }

    @Test
    void simulate_returnsResultWithEmptyValues_whenTickerNotFound() {
        // arrange

        final String ticker = "ticker";
        final TradingConfig tradingConfig = new TradingConfig("2000124699", ticker, CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        mockStrategy(tradingConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(TradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = List.of(tradingConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 7);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 1, 7, 5);
        final Interval interval = Interval.of(from, to);

        // act

        final List<SimulationResult> simulationResults = simulator.simulate(tradingConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        final SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertEquals(tradingConfigs.get(0), simulationResult.getTradingConfig());
        Assertions.assertEquals(interval, simulationResult.getInterval());
        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), simulationResult.getInitialBalance());
        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), simulationResult.getTotalInvestment());
        AssertUtils.assertEquals(0, simulationResult.getFinalTotalBalance());
        AssertUtils.assertEquals(0, simulationResult.getFinalBalance());
        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), simulationResult.getWeightedAverageInvestment());
        AssertUtils.assertEquals(0, simulationResult.getAbsoluteProfit());
        AssertUtils.assertEquals(0.0, simulationResult.getRelativeProfit());
        AssertUtils.assertEquals(0.0, simulationResult.getRelativeYearProfit());

        final String expectedErrorPattern = String.format(
                "^Simulation for '\\[brokerAccountId=2000124699, ticker=%1$s, candleResolution=1min, strategyType=conservative, " +
                        "strategyParams=\\{\\}\\]' " +
                        "failed within 00:00:00.\\d\\d\\d with error: Not found instrument for ticker '%1$s'$",
                ticker
        );
        AssertUtils.assertMatchesRegex(simulationResult.getError(), expectedErrorPattern);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void simulate_fillsCommonStatistics(@Nullable final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final TradingConfig tradingConfig = new TradingConfig(brokerAccountId, ticker, CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        mockStrategy(tradingConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(TradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = List.of(tradingConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 7);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 1, 7, 5);
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

        Mockito.when(fakeBot.processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData1, decisionData2, decisionData3, decisionData4, decisionData5);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());

        final BigDecimal currentBalance = BigDecimal.valueOf(20000);
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(currentBalance);

        final int positionLotsCount = 2;
        final PortfolioPosition portfolioPosition = TestData.createPortfolioPosition(ticker, positionLotsCount);
        mockPortfolioPositions(brokerAccountId, portfolioPosition);

        // act

        final List<SimulationResult> simulationResults = simulator.simulate(tradingConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        final SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertEquals(tradingConfigs.get(0), simulationResult.getTradingConfig());
        Assertions.assertEquals(interval, simulationResult.getInterval());
        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), simulationResult.getInitialBalance());
        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), simulationResult.getTotalInvestment());

        final BigDecimal positionsPrice = DecimalUtils.multiply(candle4.getClosePrice(), positionLotsCount);
        final BigDecimal expectedFinalTotalBalance = currentBalance.add(positionsPrice);
        AssertUtils.assertEquals(expectedFinalTotalBalance, simulationResult.getFinalTotalBalance());

        AssertUtils.assertEquals(currentBalance, simulationResult.getFinalBalance());
        AssertUtils.assertEquals(balanceConfig.getInitialBalance(), simulationResult.getWeightedAverageInvestment());

        final BigDecimal expectedAbsoluteProfit = currentBalance.subtract(balanceConfig.getInitialBalance()).add(positionsPrice);
        AssertUtils.assertEquals(expectedAbsoluteProfit, simulationResult.getAbsoluteProfit());

        AssertUtils.assertEquals(1.1, simulationResult.getRelativeProfit());
        AssertUtils.assertEquals(115711.2, simulationResult.getRelativeYearProfit());

        Assertions.assertNull(simulationResult.getError());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void simulate_callsBalanceIncrement(@Nullable final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final TradingConfig tradingConfig = new TradingConfig(brokerAccountId, ticker, CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        mockStrategy(tradingConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(TradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = List.of(tradingConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 7);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 1, 7, 5);
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

        Mockito.when(fakeBot.processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData1, decisionData2, decisionData3, decisionData4, decisionData5);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());

        final BigDecimal currentBalance = BigDecimal.valueOf(20000);
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(currentBalance);

        final int positionLotsCount = 2;
        final PortfolioPosition portfolioPosition = TestData.createPortfolioPosition(ticker, positionLotsCount);
        mockPortfolioPositions(brokerAccountId, portfolioPosition);

        // act

        final List<SimulationResult> simulationResults = simulator.simulate(tradingConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        final SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertNull(simulationResult.getError());

        Mockito.verify(fakeTinkoffService, Mockito.times(5))
                .incrementBalance(
                        Mockito.eq(MarketInstrument.getCurrency()),
                        ArgumentMatchers.argThat(BigDecimalMatcher.of(balanceConfig.getBalanceIncrement()))
                );
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void simulate_fillsPositions(@Nullable final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final TradingConfig tradingConfig = new TradingConfig(brokerAccountId, ticker, CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        mockStrategy(tradingConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(TradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = List.of(tradingConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 7);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 1, 7, 5);
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

        Mockito.when(fakeBot.processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData1, decisionData2, decisionData3, decisionData4, decisionData5);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.valueOf(20000));

        final int positionLotsCount = 2;
        final PortfolioPosition portfolioPosition = TestData.createPortfolioPosition(ticker, positionLotsCount);
        mockPortfolioPositions(brokerAccountId, portfolioPosition);

        // act

        final List<SimulationResult> simulationResults = simulator.simulate(tradingConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        final SimulationResult simulationResult = simulationResults.get(0);

        final List<SimulatedPosition> positions = simulationResult.getPositions();
        Assertions.assertEquals(1, positions.size());
        final SimulatedPosition simulatedPosition = positions.get(0);
        Assertions.assertEquals(ticker, simulatedPosition.getTicker());
        Assertions.assertEquals(candle4.getClosePrice(), simulatedPosition.getPrice());
        Assertions.assertEquals(positionLotsCount, simulatedPosition.getQuantity());

        Assertions.assertNull(simulationResult.getError());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void simulate_fillsOperations(@Nullable final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final TradingConfig tradingConfig = new TradingConfig(brokerAccountId, ticker, CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        mockStrategy(tradingConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(TradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = List.of(tradingConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 7);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 1, 7, 5);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestData.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestData.createDecisionData(candle);
        Mockito.when(fakeBot.processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.valueOf(20000));

        final OffsetDateTime operationDateTime = from.plusMinutes(2);
        final OperationTypeWithCommission operationType = OperationTypeWithCommission.BUY;
        final BigDecimal operationPrice = BigDecimal.valueOf(100);
        final int operationQuantity = 2;
        final BigDecimal operationCommission = BigDecimal.valueOf(0.03);
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

        final List<SimulationResult> simulationResults = simulator.simulate(tradingConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        final SimulationResult simulationResult = simulationResults.get(0);

        List<SimulatedOperation> resultOperations = simulationResult.getOperations();
        Assertions.assertEquals(1, resultOperations.size());
        final SimulatedOperation simulatedOperation = resultOperations.get(0);
        Assertions.assertEquals(ticker, simulatedOperation.getTicker());
        Assertions.assertEquals(operationDateTime, simulatedOperation.getDateTime());
        Assertions.assertEquals(OperationType.BUY, simulatedOperation.getOperationType());
        AssertUtils.assertEquals(operationPrice, simulatedOperation.getPrice());
        Assertions.assertEquals(operationQuantity, simulatedOperation.getQuantity());
        AssertUtils.assertEquals(operationCommission, simulatedOperation.getCommission());

        Assertions.assertNull(simulationResult.getError());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void simulate_fillsCandles(@Nullable final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final TradingConfig tradingConfig = new TradingConfig(brokerAccountId, ticker, CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        mockStrategy(tradingConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(TradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), null, BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = List.of(tradingConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 7);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 1, 7, 5);
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

        Mockito.when(fakeBot.processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData1, decisionData2, decisionData3, decisionData4, decisionData5);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency()))
                .thenReturn(BigDecimal.valueOf(20000));

        // act

        final List<SimulationResult> simulationResults = simulator.simulate(tradingConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        final SimulationResult simulationResult = simulationResults.get(0);

        List<Candle> candles = simulationResult.getCandles();
        Assertions.assertEquals(5, candles.size());
        Assertions.assertSame(candle0, candles.get(0));
        Assertions.assertSame(candle1, candles.get(1));
        Assertions.assertSame(candle2, candles.get(2));
        Assertions.assertSame(candle3, candles.get(3));
        Assertions.assertSame(candle4, candles.get(4));

        Assertions.assertNull(simulationResult.getError());

        Mockito.verify(fakeBot, Mockito.times(5))
                .processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class));
        Mockito.verify(fakeTinkoffService, Mockito.never()).incrementBalance(Mockito.any(), Mockito.any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void simulate_notFillsCandles_whenCurrentCandlesInDecisionDataIsNull(@Nullable final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final TradingConfig tradingConfig = new TradingConfig(brokerAccountId, ticker, CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        mockStrategy(tradingConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(TradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), null, BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = List.of(tradingConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 7);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 1, 7, 5);
        final Interval interval = Interval.of(from, to);

        Mockito.when(fakeBot.processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(new DecisionData());

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.valueOf(20000));

        // act

        final List<SimulationResult> simulationResults = simulator.simulate(tradingConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        final SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertTrue(simulationResult.getCandles().isEmpty());
        Assertions.assertNull(simulationResult.getError());

        Mockito.verify(fakeBot, Mockito.times(5))
                .processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class));
        Mockito.verify(fakeTinkoffService, Mockito.never()).incrementBalance(Mockito.any(), Mockito.any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void simulate_notFillsCandles_whenCurrentCandlesInDecisionDataIsEmpty(@Nullable final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final TradingConfig tradingConfig = new TradingConfig(brokerAccountId, ticker, CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        mockStrategy(tradingConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(TradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), null, BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = List.of(tradingConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 7);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 1, 7, 5);
        final Interval interval = Interval.of(from, to);

        Mockito.when(fakeBot.processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(TestData.createDecisionData());

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency()))
                .thenReturn(BigDecimal.valueOf(20000));

        // act

        final List<SimulationResult> simulationResults = simulator.simulate(tradingConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        final SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertTrue(simulationResult.getCandles().isEmpty());
        Assertions.assertNull(simulationResult.getError());

        Mockito.verify(fakeBot, Mockito.times(5))
                .processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class));
        Mockito.verify(fakeTinkoffService, Mockito.never()).incrementBalance(Mockito.any(), Mockito.any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void simulate_callsSaveToFile_whenSaveToFilesIsTrue(@Nullable final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final TradingConfig tradingConfig = new TradingConfig(brokerAccountId, ticker, CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        mockStrategy(tradingConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(TradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = List.of(tradingConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 7);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 1, 7, 5);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestData.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestData.createDecisionData(candle);
        Mockito.when(fakeBot.processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.ZERO);

        Mocker.mockTinkoffOperations(fakeTinkoffService, brokerAccountId, ticker, interval);
        mockPortfolioPositions(brokerAccountId);

        // act

        final List<SimulationResult> simulationResults = simulator.simulate(tradingConfigs, balanceConfig, interval, true);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        final SimulationResult simulationResult = simulationResults.get(0);
        Assertions.assertNull(simulationResult.getError());

        Mockito.verify(excelService, Mockito.only()).saveSimulationResults(Mockito.anyCollection());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void simulate_neverCallsSaveToFile_whenSaveToFilesIsFalse(@Nullable final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final TradingConfig tradingConfig = new TradingConfig(brokerAccountId, ticker, CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        mockStrategy(tradingConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(TradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = List.of(tradingConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 7);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 1, 7, 5);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestData.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestData.createDecisionData(candle);
        Mockito.when(fakeBot.processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.ZERO);

        Mocker.mockTinkoffOperations(fakeTinkoffService, brokerAccountId, ticker, interval);
        mockPortfolioPositions(brokerAccountId);

        // act

        final List<SimulationResult> simulationResults = simulator.simulate(tradingConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        final SimulationResult simulationResult = simulationResults.get(0);
        Assertions.assertNull(simulationResult.getError());

        Mockito.verify(excelService, Mockito.never()).saveSimulationResults(Mockito.any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void simulate_returnsZeroInvestmentsAndProfits_whenNoInvestments(@Nullable final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final TradingConfig tradingConfig = new TradingConfig(brokerAccountId, ticker, CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        mockStrategy(tradingConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(TradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.ZERO, null, BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = List.of(tradingConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 7);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 1, 7, 5);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestData.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestData.createDecisionData(candle);

        Mockito.when(fakeBot.processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency()))
                .thenReturn(BigDecimal.valueOf(20000));

        // act

        final List<SimulationResult> simulationResults = simulator.simulate(tradingConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        final SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertNull(simulationResult.getError());

        AssertUtils.assertEquals(0, simulationResult.getTotalInvestment());
        AssertUtils.assertEquals(0, simulationResult.getWeightedAverageInvestment());
        AssertUtils.assertEquals(0, simulationResult.getRelativeProfit());
        AssertUtils.assertEquals(0, simulationResult.getRelativeYearProfit());

        Mockito.verify(fakeTinkoffService, Mockito.never()).incrementBalance(Mockito.any(), Mockito.any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void simulate_catchesSimulationException(@Nullable final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final TradingConfig tradingConfig = new TradingConfig(brokerAccountId, ticker, CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        mockStrategy(tradingConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        final String mockedExceptionMessage = "mocked exception";
        Mockito.when(fakeBot.getFakeTinkoffService()).thenThrow(new IllegalArgumentException(mockedExceptionMessage));
        Mockito.when(fakeBotFactory.createBot(Mockito.any(TradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = List.of(tradingConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 7);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 1, 7, 5);
        final Interval interval = Interval.of(from, to);

        // act

        final List<SimulationResult> simulationResults = simulator.simulate(tradingConfigs, balanceConfig, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        final SimulationResult simulationResult = simulationResults.get(0);

        final String expectedErrorPattern = String.format(
                "^Simulation for '\\[brokerAccountId=%s, ticker=%s, candleResolution=1min, strategyType=conservative, strategyParams=\\{\\}\\]'" +
                        " failed within 00:00:00.\\d\\d\\d with error: %s$",
                brokerAccountId, ticker, mockedExceptionMessage
        );
        AssertUtils.assertMatchesRegex(simulationResult.getError(), expectedErrorPattern);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void simulate_catchesSaveToFileException(@Nullable final String brokerAccountId) {

        // arrange

        final String ticker = "ticker";
        final TradingConfig tradingConfig = new TradingConfig(brokerAccountId, ticker, CandleResolution._1MIN, StrategyType.CONSERVATIVE);
        mockStrategy(tradingConfig, CONSERVATIVE_STRATEGY);

        final FakeBot fakeBot = createFakeBotMock();
        Mockito.when(fakeBotFactory.createBot(Mockito.any(TradingStrategy.class), Mockito.any(CandleResolution.class)))
                .thenReturn(fakeBot);

        final MarketInstrument MarketInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, strategyFactory, simulationProperties);

        final BalanceConfig balanceConfig = new BalanceConfig(BigDecimal.valueOf(10000), BigDecimal.valueOf(1000), BALANCE_INCREMENT_CRON);

        final List<TradingConfig> tradingConfigs = List.of(tradingConfig);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 7);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 1, 7, 5);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestData.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestData.createDecisionData(candle);
        Mockito.when(fakeBot.processTicker(Mockito.eq(brokerAccountId), Mockito.eq(ticker), Mockito.isNull(), Mockito.any(OffsetDateTime.class)))
                .thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, balanceConfig.getInitialBalance());
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.ZERO);

        Mocker.mockTinkoffOperations(fakeTinkoffService, brokerAccountId, ticker, interval);
        mockPortfolioPositions(brokerAccountId);

        Mockito.doThrow(new IllegalArgumentException())
                .when(excelService)
                .saveSimulationResults(Mockito.anyCollection());

        // act

        final List<SimulationResult> simulationResults = simulator.simulate(tradingConfigs, balanceConfig, interval, true);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        final SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertNull(simulationResult.getError());
    }

    private void mockStrategy(TradingConfig tradingConfig, TradingStrategy strategy) {
        Mockito.when(strategyFactory.createStrategy(tradingConfig.getStrategyType(), tradingConfig.getStrategyParams())).thenReturn(strategy);
    }

    private FakeBot createFakeBotMock() {
        final FakeBot fakeBot = Mockito.mock(FakeBot.class);
        Mockito.when(fakeBot.getFakeTinkoffService()).thenReturn(fakeTinkoffService);
        return fakeBot;
    }

    private void mockNextMinute(OffsetDateTime from) {
        Mockito.when(fakeTinkoffService.getCurrentDateTime()).thenReturn(from);

        Mockito.when(fakeTinkoffService.nextMinute()).thenAnswer(invocationOnMock -> {
            final OffsetDateTime currentDateTime = fakeTinkoffService.getCurrentDateTime();
            final OffsetDateTime nextMinute = currentDateTime.plusMinutes(1);
            Mockito.when(fakeTinkoffService.getCurrentDateTime()).thenReturn(nextMinute);
            return nextMinute;
        });
    }

    private void mockInitialBalance(Currency currency, OffsetDateTime dateTime, BigDecimal balance) {
        final SortedMap<OffsetDateTime, BigDecimal> investments = new TreeMap<>();
        investments.put(dateTime, balance);
        Mockito.when(fakeTinkoffService.getInvestments(currency)).thenReturn(investments);
    }

    private void mockPortfolioPositions(@Nullable final String brokerAccountId, PortfolioPosition... portfolioPositions) {
        Mockito.when(fakeTinkoffService.getPortfolioPositions(brokerAccountId)).thenReturn(List.of(portfolioPositions));
    }

    // endregion

}