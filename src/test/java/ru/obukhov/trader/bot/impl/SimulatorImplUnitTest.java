package ru.obukhov.trader.bot.impl;

import org.apache.commons.compress.utils.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.quartz.CronExpression;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.bot.interfaces.FakeBot;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.obukhov.trader.test.utils.matchers.BigDecimalMatcher;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.obukhov.trader.web.model.pojo.SimulatedPosition;
import ru.obukhov.trader.web.model.pojo.SimulationResult;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.OperationTypeWithCommission;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

class SimulatorImplUnitTest extends BaseMockedTest {

    private static final String DATE_TIME_REGEX_PATTERN = "[\\d\\-\\+\\.:T]+";

    @Mock
    private ExcelService excelService;
    @Mock
    private FakeBotFactory fakeBotFactory;
    @Mock
    private FakeTinkoffService fakeTinkoffService;

    // region constructor tests

    @Test
    void constructor_throwsIllegalArgumentException_whenSimulationThreadCountIsNegative() {
        AssertUtils.assertThrowsWithMessage(
                () -> new SimulatorImpl(excelService, fakeBotFactory, -1),
                IllegalArgumentException.class,
                "simulationThreadCount must be greater than 1"
        );
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenSimulationThreadCountIsZero() {
        AssertUtils.assertThrowsWithMessage(
                () -> new SimulatorImpl(excelService, fakeBotFactory, 0),
                IllegalArgumentException.class,
                "simulationThreadCount must be greater than 1"
        );
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenSimulationThreadCountIsOne() {
        AssertUtils.assertThrowsWithMessage(
                () -> new SimulatorImpl(excelService, fakeBotFactory, 1),
                IllegalArgumentException.class,
                "simulationThreadCount must be greater than 1"
        );
    }

    // endregion

    // region simulate tests

    @Test
    void simulate_throwsIllegalArgumentException_whenFromIsInFuture() throws ParseException {
        final String ticker = "ticker";

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = BigDecimal.valueOf(1000);
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = OffsetDateTime.now().plusDays(1);
        final OffsetDateTime to = from.plusDays(1);
        final Interval interval = Interval.of(from, to);

        final String expectedMessagePattern = String.format(
                "^'from' \\(%1$s\\) can't be in future. Now is %1$s$",
                DATE_TIME_REGEX_PATTERN);

        AssertUtils.assertThrowsWithMessagePattern(
                () -> simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false),
                IllegalArgumentException.class,
                expectedMessagePattern
        );
    }

    @Test
    void simulate_throwsIllegalArgumentException_whenToIsInFuture() throws ParseException {
        final String ticker = "ticker";

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = BigDecimal.valueOf(1000);
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        final OffsetDateTime to = from.plusDays(2);
        final Interval interval = Interval.of(from, to);

        final String expectedMessagePattern = String.format(
                "^'to' \\(%1$s\\) can't be in future. Now is %1$s$",
                DATE_TIME_REGEX_PATTERN);

        AssertUtils.assertThrowsWithMessagePattern(
                () -> simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false),
                RuntimeException.class,
                expectedMessagePattern
        );
    }

    @Test
    void simulate_returnsResultWithEmptyValues_whenTickerNotFound() throws ParseException {
        // arrange

        final String ticker = "ticker";
        final String botName = "botName";

        FakeBot fakeBot = createFakeBotMock(botName);
        Mockito.when(fakeBotFactory.createBots()).thenReturn(Sets.newHashSet(fakeBot));

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = BigDecimal.valueOf(1000);
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 7, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 7, 5, 0);
        final Interval interval = Interval.of(from, to);

        // act

        List<SimulationResult> simulationResults =
                simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertEquals(botName, simulationResult.getBotName());
        Assertions.assertEquals(interval, simulationResult.getInterval());
        AssertUtils.assertEquals(initialBalance, simulationResult.getInitialBalance());
        AssertUtils.assertEquals(initialBalance, simulationResult.getTotalInvestment());
        AssertUtils.assertEquals(0, simulationResult.getFinalTotalBalance());
        AssertUtils.assertEquals(0, simulationResult.getFinalBalance());
        AssertUtils.assertEquals(initialBalance, simulationResult.getWeightedAverageInvestment());
        AssertUtils.assertEquals(0, simulationResult.getAbsoluteProfit());
        AssertUtils.assertEquals(0.0, simulationResult.getRelativeProfit());
        AssertUtils.assertEquals(0.0, simulationResult.getRelativeYearProfit());

        String expectedError = String.format(
                "Simulation for '%1$s' with ticker '%2$s' failed with error: Not found instrument for ticker '%2$s'",
                botName,
                ticker
        );
        Assertions.assertEquals(expectedError, simulationResult.getError());
    }

    @Test
    void simulate_fillsCommonStatistics() throws ParseException {

        // arrange

        final String ticker = "ticker";
        final String botName = "botName";

        FakeBot fakeBot = createFakeBotMock(botName);
        Mockito.when(fakeBotFactory.createBots()).thenReturn(Sets.newHashSet(fakeBot));

        MarketInstrument MarketInstrument = TestDataHelper.createAndMockInstrument(fakeTinkoffService, ticker);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = BigDecimal.valueOf(1000);
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 7, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 7, 5, 0);
        final Interval interval = Interval.of(from, to);

        final Candle candle0 = TestDataHelper.createCandleWithClosePrice(100);
        final DecisionData decisionData1 = TestDataHelper.createDecisionData(candle0);

        final Candle candle1 = TestDataHelper.createCandleWithClosePrice(200);
        final DecisionData decisionData2 = TestDataHelper.createDecisionData(candle1);

        final Candle candle2 = TestDataHelper.createCandleWithClosePrice(300);
        final DecisionData decisionData3 = TestDataHelper.createDecisionData(candle2);

        final Candle candle3 = TestDataHelper.createCandleWithClosePrice(400);
        final DecisionData decisionData4 = TestDataHelper.createDecisionData(candle3);

        final Candle candle4 = TestDataHelper.createCandleWithClosePrice(500);
        final DecisionData decisionData5 = TestDataHelper.createDecisionData(candle4);

        Mockito.when(fakeBot.processTicker(ticker))
                .thenReturn(decisionData1, decisionData2, decisionData3, decisionData4, decisionData5);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, initialBalance);

        final BigDecimal currentBalance = BigDecimal.valueOf(20000);
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(currentBalance);

        final int positionLotsCount = 2;
        final PortfolioPosition portfolioPosition = TestDataHelper.createPortfolioPosition(ticker, positionLotsCount);
        mockPortfolioPositions(portfolioPosition);

        // act

        List<SimulationResult> simulationResults =
                simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertEquals(botName, simulationResult.getBotName());
        Assertions.assertEquals(interval, simulationResult.getInterval());
        AssertUtils.assertEquals(initialBalance, simulationResult.getInitialBalance());
        AssertUtils.assertEquals(initialBalance, simulationResult.getTotalInvestment());

        final BigDecimal positionsPrice = DecimalUtils.multiply(candle4.getClosePrice(), positionLotsCount);
        final BigDecimal expectedFinalTotalBalance = currentBalance.add(positionsPrice);
        AssertUtils.assertEquals(expectedFinalTotalBalance, simulationResult.getFinalTotalBalance());

        AssertUtils.assertEquals(currentBalance, simulationResult.getFinalBalance());
        AssertUtils.assertEquals(initialBalance, simulationResult.getWeightedAverageInvestment());

        final BigDecimal expectedAbsoluteProfit = currentBalance.subtract(initialBalance).add(positionsPrice);
        AssertUtils.assertEquals(expectedAbsoluteProfit, simulationResult.getAbsoluteProfit());

        AssertUtils.assertEquals(1.1, simulationResult.getRelativeProfit());
        AssertUtils.assertEquals(115711.2, simulationResult.getRelativeYearProfit());

        Assertions.assertNull(simulationResult.getError());
    }

    @Test
    void simulate_callsBalanceIncrement() throws ParseException {

        // arrange

        final String ticker = "ticker";
        final String botName = "botName";

        FakeBot fakeBot = createFakeBotMock(botName);
        Mockito.when(fakeBotFactory.createBots()).thenReturn(Sets.newHashSet(fakeBot));

        MarketInstrument MarketInstrument = TestDataHelper.createAndMockInstrument(fakeTinkoffService, ticker);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = BigDecimal.valueOf(1000);
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 7, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 7, 5, 0);
        final Interval interval = Interval.of(from, to);

        final Candle candle0 = TestDataHelper.createCandleWithClosePrice(100);
        final DecisionData decisionData1 = TestDataHelper.createDecisionData(candle0);

        final Candle candle1 = TestDataHelper.createCandleWithClosePrice(200);
        final DecisionData decisionData2 = TestDataHelper.createDecisionData(candle1);

        final Candle candle2 = TestDataHelper.createCandleWithClosePrice(300);
        final DecisionData decisionData3 = TestDataHelper.createDecisionData(candle2);

        final Candle candle3 = TestDataHelper.createCandleWithClosePrice(400);
        final DecisionData decisionData4 = TestDataHelper.createDecisionData(candle3);

        final Candle candle4 = TestDataHelper.createCandleWithClosePrice(500);
        final DecisionData decisionData5 = TestDataHelper.createDecisionData(candle4);

        Mockito.when(fakeBot.processTicker(ticker))
                .thenReturn(decisionData1, decisionData2, decisionData3, decisionData4, decisionData5);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, initialBalance);

        final BigDecimal currentBalance = BigDecimal.valueOf(20000);
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(currentBalance);

        final int positionLotsCount = 2;
        final PortfolioPosition portfolioPosition = TestDataHelper.createPortfolioPosition(ticker, positionLotsCount);
        mockPortfolioPositions(portfolioPosition);

        // act

        List<SimulationResult> simulationResults =
                simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertNull(simulationResult.getError());

        Mockito.verify(fakeTinkoffService, Mockito.times(5))
                .incrementBalance(
                        Mockito.eq(MarketInstrument.getCurrency()),
                        ArgumentMatchers.argThat(BigDecimalMatcher.of(balanceIncrement))
                );
    }

    @Test
    void simulate_fillsPositions() throws ParseException {

        // arrange

        final String ticker = "ticker";
        final String botName = "botName";

        FakeBot fakeBot = createFakeBotMock(botName);
        Mockito.when(fakeBotFactory.createBots()).thenReturn(Sets.newHashSet(fakeBot));

        MarketInstrument MarketInstrument = TestDataHelper.createAndMockInstrument(fakeTinkoffService, ticker);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = BigDecimal.valueOf(1000);
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 7, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 7, 5, 0);
        final Interval interval = Interval.of(from, to);

        final Candle candle0 = TestDataHelper.createCandleWithClosePrice(100);
        final DecisionData decisionData1 = TestDataHelper.createDecisionData(candle0);

        final Candle candle1 = TestDataHelper.createCandleWithClosePrice(200);
        final DecisionData decisionData2 = TestDataHelper.createDecisionData(candle1);

        final Candle candle2 = TestDataHelper.createCandleWithClosePrice(300);
        final DecisionData decisionData3 = TestDataHelper.createDecisionData(candle2);

        final Candle candle3 = TestDataHelper.createCandleWithClosePrice(400);
        final DecisionData decisionData4 = TestDataHelper.createDecisionData(candle3);

        final Candle candle4 = TestDataHelper.createCandleWithClosePrice(500);
        final DecisionData decisionData5 = TestDataHelper.createDecisionData(candle4);

        Mockito.when(fakeBot.processTicker(ticker))
                .thenReturn(decisionData1, decisionData2, decisionData3, decisionData4, decisionData5);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, initialBalance);
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.valueOf(20000));

        final int positionLotsCount = 2;
        final PortfolioPosition portfolioPosition = TestDataHelper.createPortfolioPosition(ticker, positionLotsCount);
        mockPortfolioPositions(portfolioPosition);

        // act

        List<SimulationResult> simulationResults =
                simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        SimulationResult simulationResult = simulationResults.get(0);

        final List<SimulatedPosition> positions = simulationResult.getPositions();
        Assertions.assertEquals(1, positions.size());
        final SimulatedPosition simulatedPosition = positions.get(0);
        Assertions.assertEquals(ticker, simulatedPosition.getTicker());
        Assertions.assertEquals(candle4.getClosePrice(), simulatedPosition.getPrice());
        Assertions.assertEquals(positionLotsCount, simulatedPosition.getQuantity());

        Assertions.assertNull(simulationResult.getError());
    }

    @Test
    void simulate_fillsOperations() throws ParseException {

        // arrange

        final String ticker = "ticker";
        final String botName = "botName";

        FakeBot fakeBot = createFakeBotMock(botName);
        Mockito.when(fakeBotFactory.createBots()).thenReturn(Sets.newHashSet(fakeBot));

        MarketInstrument MarketInstrument = TestDataHelper.createAndMockInstrument(fakeTinkoffService, ticker);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = BigDecimal.valueOf(1000);
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 7, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 7, 5, 0);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestDataHelper.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestDataHelper.createDecisionData(candle);
        Mockito.when(fakeBot.processTicker(ticker)).thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, initialBalance);
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.valueOf(20000));

        final OffsetDateTime operationDateTime = from.plusMinutes(2);
        final OperationTypeWithCommission operationType = OperationTypeWithCommission.BUY;
        final BigDecimal operationPrice = BigDecimal.valueOf(100);
        final int operationQuantity = 2;
        final BigDecimal operationCommission = BigDecimal.valueOf(0.03);
        final Operation operation = TestDataHelper.createTinkoffOperation(
                operationDateTime,
                operationType,
                operationPrice,
                operationQuantity,
                operationCommission
        );
        TestDataHelper.mockTinkoffOperations(fakeTinkoffService, ticker, interval, operation);

        mockPortfolioPositions();

        // act

        List<SimulationResult> simulationResults =
                simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        SimulationResult simulationResult = simulationResults.get(0);

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

    @Test
    void simulate_fillsCandles() throws ParseException {

        // arrange

        final String ticker = "ticker";
        final String botName = "botName";

        FakeBot fakeBot = createFakeBotMock(botName);
        Mockito.when(fakeBotFactory.createBots()).thenReturn(Sets.newHashSet(fakeBot));

        MarketInstrument MarketInstrument = TestDataHelper.createAndMockInstrument(fakeTinkoffService, ticker);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = null;
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 7, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 7, 5, 0);
        final Interval interval = Interval.of(from, to);

        final Candle candle0 = TestDataHelper.createCandleWithClosePrice(100);
        final DecisionData decisionData1 = TestDataHelper.createDecisionData(candle0);

        final Candle candle1 = TestDataHelper.createCandleWithClosePrice(200);
        final DecisionData decisionData2 = TestDataHelper.createDecisionData(candle1);

        final Candle candle2 = TestDataHelper.createCandleWithClosePrice(300);
        final DecisionData decisionData3 = TestDataHelper.createDecisionData(candle2);

        final Candle candle3 = TestDataHelper.createCandleWithClosePrice(400);
        final DecisionData decisionData4 = TestDataHelper.createDecisionData(candle3);

        final Candle candle4 = TestDataHelper.createCandleWithClosePrice(500);
        final DecisionData decisionData5 = TestDataHelper.createDecisionData(candle4);

        Mockito.when(fakeBot.processTicker(ticker))
                .thenReturn(decisionData1, decisionData2, decisionData3, decisionData4, decisionData5);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, initialBalance);
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.valueOf(20000));

        // act

        List<SimulationResult> simulationResults =
                simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        SimulationResult simulationResult = simulationResults.get(0);

        List<Candle> candles = simulationResult.getCandles();
        Assertions.assertEquals(5, candles.size());
        Assertions.assertSame(candle0, candles.get(0));
        Assertions.assertSame(candle1, candles.get(1));
        Assertions.assertSame(candle2, candles.get(2));
        Assertions.assertSame(candle3, candles.get(3));
        Assertions.assertSame(candle4, candles.get(4));

        Assertions.assertNull(simulationResult.getError());

        Mockito.verify(fakeBot, Mockito.times(5)).processTicker(ticker);
        Mockito.verify(fakeTinkoffService, Mockito.never()).incrementBalance(Mockito.any(), Mockito.any());
    }

    @Test
    void simulate_notFillsCandles_whenDecisionDataIsNull() throws ParseException {

        // arrange

        final String ticker = "ticker";
        final String botName = "botName";

        FakeBot fakeBot = createFakeBotMock(botName);
        Mockito.when(fakeBotFactory.createBots()).thenReturn(Sets.newHashSet(fakeBot));

        MarketInstrument MarketInstrument = TestDataHelper.createAndMockInstrument(fakeTinkoffService, ticker);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = null;
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 7, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 7, 5, 0);
        final Interval interval = Interval.of(from, to);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, initialBalance);
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.valueOf(20000));

        // act

        List<SimulationResult> simulationResults =
                simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertTrue(simulationResult.getCandles().isEmpty());
        Assertions.assertNull(simulationResult.getError());

        Mockito.verify(fakeBot, Mockito.times(5)).processTicker(ticker);
        Mockito.verify(fakeTinkoffService, Mockito.never()).incrementBalance(Mockito.any(), Mockito.any());
    }

    @Test
    void simulate_notFillsCandles_whenCurrentCandlesInDecisionDataIsNull() throws ParseException {

        // arrange

        final String ticker = "ticker";
        final String botName = "botName";

        FakeBot fakeBot = createFakeBotMock(botName);
        Mockito.when(fakeBotFactory.createBots()).thenReturn(Sets.newHashSet(fakeBot));

        MarketInstrument MarketInstrument = TestDataHelper.createAndMockInstrument(fakeTinkoffService, ticker);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = null;
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 7, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 7, 5, 0);
        final Interval interval = Interval.of(from, to);

        Mockito.when(fakeBot.processTicker(ticker)).thenReturn(new DecisionData());

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, initialBalance);
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.valueOf(20000));

        // act

        List<SimulationResult> simulationResults =
                simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertTrue(simulationResult.getCandles().isEmpty());
        Assertions.assertNull(simulationResult.getError());

        Mockito.verify(fakeBot, Mockito.times(5)).processTicker(ticker);
        Mockito.verify(fakeTinkoffService, Mockito.never()).incrementBalance(Mockito.any(), Mockito.any());
    }

    @Test
    void simulate_notFillsCandles_whenCurrentCandlesInDecisionDataIsEmpty() throws ParseException {

        // arrange

        final String ticker = "ticker";
        final String botName = "botName";

        FakeBot fakeBot = createFakeBotMock(botName);
        Mockito.when(fakeBotFactory.createBots()).thenReturn(Sets.newHashSet(fakeBot));

        MarketInstrument MarketInstrument = TestDataHelper.createAndMockInstrument(fakeTinkoffService, ticker);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = null;
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 7, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 7, 5, 0);
        final Interval interval = Interval.of(from, to);

        Mockito.when(fakeBot.processTicker(ticker)).thenReturn(TestDataHelper.createDecisionData());

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, initialBalance);
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.valueOf(20000));

        // act

        List<SimulationResult> simulationResults =
                simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertTrue(simulationResult.getCandles().isEmpty());
        Assertions.assertNull(simulationResult.getError());

        Mockito.verify(fakeBot, Mockito.times(5)).processTicker(ticker);
        Mockito.verify(fakeTinkoffService, Mockito.never()).incrementBalance(Mockito.any(), Mockito.any());
    }

    @Test
    void simulate_callsSaveToFile_whenSaveToFilesIsTrue() throws ParseException {

        // arrange

        final String ticker = "ticker";
        final String botName = "botName";

        FakeBot fakeBot = createFakeBotMock(botName);
        Mockito.when(fakeBotFactory.createBots()).thenReturn(Sets.newHashSet(fakeBot));

        MarketInstrument MarketInstrument = TestDataHelper.createAndMockInstrument(fakeTinkoffService, ticker);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = BigDecimal.valueOf(1000);
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 7, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 7, 5, 0);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestDataHelper.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestDataHelper.createDecisionData(candle);
        Mockito.when(fakeBot.processTicker(ticker)).thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, initialBalance);
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.ZERO);

        TestDataHelper.mockTinkoffOperations(fakeTinkoffService, ticker, interval);
        mockPortfolioPositions();

        // act

        List<SimulationResult> simulationResults =
                simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, true);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        SimulationResult simulationResult = simulationResults.get(0);
        Assertions.assertNull(simulationResult.getError());

        Mockito.verify(excelService, Mockito.only()).saveSimulationResults(Mockito.eq(ticker), Mockito.anyCollection());
    }

    @Test
    void simulate_neverCallsSaveToFile_whenSaveToFilesIsFalse() throws ParseException {

        // arrange

        final String ticker = "ticker";
        final String botName = "botName";

        FakeBot fakeBot = createFakeBotMock(botName);
        Mockito.when(fakeBotFactory.createBots()).thenReturn(Sets.newHashSet(fakeBot));

        MarketInstrument MarketInstrument = TestDataHelper.createAndMockInstrument(fakeTinkoffService, ticker);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = BigDecimal.valueOf(1000);
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 7, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 7, 5, 0);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestDataHelper.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestDataHelper.createDecisionData(candle);
        Mockito.when(fakeBot.processTicker(ticker)).thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, initialBalance);
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.ZERO);

        TestDataHelper.mockTinkoffOperations(fakeTinkoffService, ticker, interval);
        mockPortfolioPositions();

        // act

        List<SimulationResult> simulationResults =
                simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        SimulationResult simulationResult = simulationResults.get(0);
        Assertions.assertNull(simulationResult.getError());

        Mockito.verify(excelService, Mockito.never()).saveSimulationResults(Mockito.any(), Mockito.any());
    }

    @Test
    void simulate_catchesSimulationException() throws ParseException {

        // arrange

        final String ticker = "ticker";
        final String botName = "botName";

        FakeBot fakeBot = createFakeBotMock(botName);
        final String mockedExceptionMessage = "mocked exception";
        Mockito.when(fakeBot.getFakeTinkoffService()).thenThrow(new IllegalArgumentException(mockedExceptionMessage));
        Mockito.when(fakeBotFactory.createBots()).thenReturn(Sets.newHashSet(fakeBot));

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = BigDecimal.valueOf(1000);
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 7, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 7, 5, 0);
        final Interval interval = Interval.of(from, to);

        // act

        List<SimulationResult> simulationResults =
                simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        SimulationResult simulationResult = simulationResults.get(0);

        String expectedErrorMessage = String.format("Simulation for '%s' with ticker '%s' failed with error: %s",
                botName, ticker, mockedExceptionMessage);
        Assertions.assertEquals(expectedErrorMessage, simulationResult.getError());
    }

    @Test
    void simulate_catchesSaveToFileException() throws ParseException {

        // arrange

        final String ticker = "ticker";
        final String botName = "botName";

        FakeBot fakeBot = createFakeBotMock(botName);
        Mockito.when(fakeBotFactory.createBots()).thenReturn(Sets.newHashSet(fakeBot));

        MarketInstrument MarketInstrument = TestDataHelper.createAndMockInstrument(fakeTinkoffService, ticker);

        final SimulatorImpl simulator = new SimulatorImpl(excelService, fakeBotFactory, 2);

        final BigDecimal initialBalance = BigDecimal.valueOf(10000);
        final BigDecimal balanceIncrement = BigDecimal.valueOf(1000);
        final CronExpression cronExpression = new CronExpression("0 * * * * ?");

        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 7, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 1, 7, 5, 0);
        final Interval interval = Interval.of(from, to);

        final Candle candle = TestDataHelper.createCandleWithClosePrice(100);
        final DecisionData decisionData = TestDataHelper.createDecisionData(candle);
        Mockito.when(fakeBot.processTicker(ticker)).thenReturn(decisionData);

        mockNextMinute(from);

        mockInitialBalance(MarketInstrument.getCurrency(), from, initialBalance);
        Mockito.when(fakeTinkoffService.getCurrentBalance(MarketInstrument.getCurrency())).thenReturn(BigDecimal.ZERO);

        TestDataHelper.mockTinkoffOperations(fakeTinkoffService, ticker, interval);
        mockPortfolioPositions();

        Mockito.doThrow(new IllegalArgumentException())
                .when(excelService).saveSimulationResults(Mockito.eq(ticker), Mockito.anyCollection());

        // act

        List<SimulationResult> simulationResults =
                simulator.simulate(ticker, initialBalance, balanceIncrement, cronExpression, interval, false);

        // assert

        Assertions.assertEquals(1, simulationResults.size());

        SimulationResult simulationResult = simulationResults.get(0);

        Assertions.assertNull(simulationResult.getError());
    }

    private FakeBot createFakeBotMock(String botName) {
        FakeBot fakeBot = Mockito.mock(FakeBot.class);
        Mockito.when(fakeBot.getStrategyName()).thenReturn(botName);
        Mockito.when(fakeBot.getFakeTinkoffService()).thenReturn(fakeTinkoffService);
        return fakeBot;
    }

    private void mockNextMinute(OffsetDateTime from) {
        Mockito.when(fakeTinkoffService.getCurrentDateTime()).thenReturn(from);

        Mockito.when(fakeTinkoffService.nextMinute()).thenAnswer(invocationOnMock -> {
            OffsetDateTime currentDateTime = fakeTinkoffService.getCurrentDateTime();
            OffsetDateTime nextMinute = currentDateTime.plusMinutes(1);
            Mockito.when(fakeTinkoffService.getCurrentDateTime()).thenReturn(nextMinute);
            return nextMinute;
        });
    }

    private void mockInitialBalance(Currency currency, OffsetDateTime dateTime, BigDecimal balance) {
        final SortedMap<OffsetDateTime, BigDecimal> investments = new TreeMap<>();
        investments.put(dateTime, balance);
        Mockito.when(fakeTinkoffService.getInvestments(currency)).thenReturn(investments);
    }

    private void mockPortfolioPositions(PortfolioPosition... portfolioPositions) {
        Mockito.when(fakeTinkoffService.getPortfolioPositions())
                .thenReturn(List.of(portfolioPositions));
    }

    private SimulationResult assertAndGetSingleSimulationResult(
            Map<String, List<SimulationResult>> tickerToSimulationResults,
            String ticker) {

        Assertions.assertEquals(1, tickerToSimulationResults.size());
        List<SimulationResult> simulationResults = tickerToSimulationResults.get(ticker);
        Assertions.assertEquals(1, simulationResults.size());
        return simulationResults.get(0);
    }

    // endregion

}