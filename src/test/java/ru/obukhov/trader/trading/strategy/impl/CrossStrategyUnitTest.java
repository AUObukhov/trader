package ru.obukhov.trader.trading.strategy.impl;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.common.service.impl.SimpleMovingAverager;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.model.CrossStrategyParams;
import ru.obukhov.trader.trading.model.Crossover;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.DecisionsData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CrossStrategyUnitTest {

    @Mock
    private ExtMarketDataService extMarketDataService;
    @Mock
    private MovingAverager averager;

    // region decide tests

    @Test
    void decide_throwsIllegalArgumentException_whenNoDecisionData() {
        final Share share = TestShares.SBER.share();

        final DecisionsData data = new DecisionsData();
        data.setDecisionDataList(Collections.emptyList());

        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final String accountId = TestAccounts.TINKOFF.account().id();
        final List<String> figies = List.of(share.figi());
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Executable executable = () -> strategy.decide(data, strategy.initCache(botConfig, interval));
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "Cross strategy supports 1 instrument only");
    }

    @Test
    void decide_throwsIllegalArgumentException_whenMultipleDecisionData() {
        final Share share = TestShares.SBER.share();

        final DecisionsData data = new DecisionsData();
        data.setDecisionDataList(List.of(new DecisionData(), new DecisionData()));

        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final String accountId = TestAccounts.TINKOFF.account().id();
        final List<String> figies = List.of(share.figi());
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Executable executable = () -> strategy.decide(data, strategy.initCache(botConfig, interval));
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "Cross strategy supports 1 instrument only");
    }

    @Test
    void decide_returnsWait_whenExistsOperationInProgress() {
        final Share share = TestShares.SBER.share();

        final Operation operation1 = TestData.newOperation(OperationState.OPERATION_STATE_EXECUTED);
        final Operation operation2 = TestData.newOperation(OperationState.OPERATION_STATE_UNSPECIFIED);
        final Operation operation3 = TestData.newOperation(OperationState.OPERATION_STATE_CANCELED);

        final DecisionData decisionData = new DecisionData();
        decisionData.setShare(share);
        decisionData.setLastOperations(List.of(operation1, operation2, operation3));

        final DecisionsData data = new DecisionsData();
        data.setCommission(DecimalUtils.setDefaultScale(0.003));
        data.setDecisionDataList(List.of(decisionData));

        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final String accountId = TestAccounts.TINKOFF.account().id();
        final List<String> figies = List.of(share.figi());
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Map<String, Decision> decisions = strategy.decide(data, strategy.initCache(botConfig, interval));

        Assertions.assertEquals(1, decisions.size());
        Assertions.assertEquals(DecisionAction.WAIT, decisions.get(share.figi()).getAction());
        Assertions.assertNull(decisions.get(share.figi()).getQuantity());
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsNone() {
        final Share share = TestShares.SBER.share();
        final String currency = share.currency();

        final DecisionData decisionData = TestData.newDecisionData(share, 1L);

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setCommission(DecimalUtils.setDefaultScale(0.003));
        decisionsData.setDecisionDataList(List.of(decisionData));

        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5);
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.NONE)) {
            final String accountId = TestAccounts.TINKOFF.account().id();
            final List<String> figies = List.of(share.figi());
            final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
            final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, Map.of());
            final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
            final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
            final Interval interval = Interval.of(from, to);

            final Map<String, Decision> decisions = strategy.decide(decisionsData, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(1, decisions.size());
            Assertions.assertEquals(DecisionAction.WAIT, decisions.get(share.figi()).getAction());
            Assertions.assertNull(decisions.get(share.figi()).getQuantity());
        }
    }

    @Test
    void decide_returnsBuy_whenCrossoverIsBelow_andThereAreAvailableLots() {
        final Share share = TestShares.SBER.share();

        final long availableLots = 9;

        final DecisionData decisionData = TestData.newDecisionData(share, availableLots);

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setCommission(DecimalUtils.setDefaultScale(0.003));
        decisionsData.setDecisionDataList(List.of(decisionData));

        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final String accountId = TestAccounts.TINKOFF.account().id();
        final List<String> figies = List.of(share.figi());
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        try (@SuppressWarnings("unused") final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.BELOW)) {
            final Map<String, Decision> decisions = strategy.decide(decisionsData, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(1, decisions.size());
            Assertions.assertEquals(DecisionAction.BUY, decisions.get(share.figi()).getAction());
            AssertUtils.assertEquals(availableLots, decisions.get(share.figi()).getQuantity());
        }
    }

    @Test
    void decide_returnsWait_whenCrossoverIsBelow_andThereAreNoAvailableLots() {
        final Share share = TestShares.SBER.share();

        final DecisionData decisionData = TestData.newDecisionData(share, 0L);

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setCommission(DecimalUtils.setDefaultScale(0.003));
        decisionsData.setDecisionDataList(List.of(decisionData));

        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        try (@SuppressWarnings("unused") final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.BELOW)) {
            final String accountId = TestAccounts.TINKOFF.account().id();
            final List<String> figies = List.of(share.figi());
            final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
            final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, Map.of());
            final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
            final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
            final Interval interval = Interval.of(from, to);

            final Map<String, Decision> decisions = strategy.decide(decisionsData, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(1, decisions.size());
            Assertions.assertEquals(DecisionAction.WAIT, decisions.get(share.figi()).getAction());
            Assertions.assertNull(decisions.get(share.figi()).getQuantity());
        }
    }

    @Test
    void decide_returnsSell_whenCrossoverIsAbove_andSellProfitIsGreaterThanMinimum() {
        final Share share = TestShares.SBER.share();

        final long availableLots = 1;

        final DecisionData decisionData = TestData.newDecisionData(share, availableLots);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(100)
                .setQuantity(availableLots)
                .build();
        decisionData.setPosition(position);

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setCommission(DecimalUtils.setDefaultScale(0.003));
        decisionsData.setDecisionDataList(List.of(decisionData));

        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final String accountId = TestAccounts.TINKOFF.account().id();
        final List<String> figies = List.of(share.figi());
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(120));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(share.figi(), interval, candleInterval)).thenReturn(candles);

        try (@SuppressWarnings("unused") final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Map<String, Decision> decisions = strategy.decide(decisionsData, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(1, decisions.size());
            Assertions.assertEquals(DecisionAction.SELL, decisions.get(share.figi()).getAction());
            AssertUtils.assertEquals(availableLots, decisions.get(share.figi()).getQuantity());
        }
    }

    @Test
    void decide_returnsWait_whenCrossoverIsAbove_andMinimumProfitIsNegative() {
        final Share share = TestShares.SBER.share();

        final long availableLots = 1;

        final DecisionData decisionData = TestData.newDecisionData(share, availableLots);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(100)
                .build();
        decisionData.setPosition(position);

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setCommission(DecimalUtils.setDefaultScale(0.003));
        decisionsData.setDecisionDataList(List.of(decisionData));

        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                -0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );

        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final List<String> figies = List.of(share.figi());
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final String accountId = TestAccounts.TINKOFF.account().id();
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Candle candle = new Candle().setClose(BigDecimal.ZERO);
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(share.figi(), interval, candleInterval)).thenReturn(candles);

        try (@SuppressWarnings("unused") final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Map<String, Decision> decisions = strategy.decide(decisionsData, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(1, decisions.size());
            Assertions.assertEquals(DecisionAction.WAIT, decisions.get(share.figi()).getAction());
            Assertions.assertNull(decisions.get(share.figi()).getQuantity());
        }
    }

    @Test
    void decide_returnsBuy_whenCrossoverIsAbove_andSellProfitIsLowerThanMinimum_andThereAreAvailableLots_andGreedy() {
        final Share share = TestShares.SBER.share();

        final long availableLots = 4;

        final DecisionData decisionData = TestData.newDecisionData(share, availableLots);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(199)
                .build();
        decisionData.setPosition(position);

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setCommission(DecimalUtils.setDefaultScale(0.003));
        decisionsData.setDecisionDataList(List.of(decisionData));

        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                true,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final String accountId = TestAccounts.TINKOFF.account().id();
        final List<String> figies = List.of(share.figi());
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Candle candle = new Candle().setClose(BigDecimal.ZERO);
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(share.figi(), interval, candleInterval)).thenReturn(candles);

        try (@SuppressWarnings("unused") final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Map<String, Decision> decisions = strategy.decide(decisionsData, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(1, decisions.size());
            Assertions.assertEquals(DecisionAction.BUY, decisions.get(share.figi()).getAction());
            AssertUtils.assertEquals(1, decisions.get(share.figi()).getQuantity());
        }
    }

    @Test
    void decide_returnsWait_whenCrossoverIsAbove_andSellProfitIsLowerThanMinimum_andThereAreAvailableLots_andNotGreedy() {
        final Share share = TestShares.SBER.share();

        final long availableLots = 1;

        final DecisionData decisionData = TestData.newDecisionData(share, availableLots);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(199)
                .build();
        decisionData.setPosition(position);

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setCommission(DecimalUtils.setDefaultScale(0.003));
        decisionsData.setDecisionDataList(List.of(decisionData));

        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final String accountId = TestAccounts.TINKOFF.account().id();
        final List<String> figies = List.of(share.figi());
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Candle candle = new Candle().setClose(BigDecimal.ZERO);
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(share.figi(), interval, candleInterval)).thenReturn(candles);

        try (@SuppressWarnings("unused") final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Map<String, Decision> decisions = strategy.decide(decisionsData, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(1, decisions.size());
            Assertions.assertEquals(DecisionAction.WAIT, decisions.get(share.figi()).getAction());
            Assertions.assertNull(decisions.get(share.figi()).getQuantity());
        }
    }

    @Test
    void decide_returnsWait_whenCrossoverIsAbove_andSellProfitIsLowerThanMinimum_andThereAreNoAvailableLots() {
        final Share share = TestShares.SBER.share();

        final long availableLots = 0;

        final DecisionData decisionData = TestData.newDecisionData(share, availableLots);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(199)
                .build();
        decisionData.setPosition(position);

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setCommission(DecimalUtils.setDefaultScale(0.003));
        decisionsData.setDecisionDataList(List.of(decisionData));

        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final List<String> figies = List.of(share.figi());
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final String accountId = TestAccounts.TINKOFF.account().id();
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, DecimalUtils.ZERO, StrategyType.CROSS, Map.of());
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Candle candle = new Candle().setClose(BigDecimal.ZERO);
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(share.figi(), interval, candleInterval)).thenReturn(candles);

        try (@SuppressWarnings("unused") final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Map<String, Decision> decisions = strategy.decide(decisionsData, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(1, decisions.size());
            Assertions.assertEquals(DecisionAction.WAIT, decisions.get(share.figi()).getAction());
            Assertions.assertNull(decisions.get(share.figi()).getQuantity());
        }
    }

    // endregion

    @Test
    void initCache_returnsNotNull() {
        final Share share = TestShares.SBER.share();

        final List<String> figies = List.of(share.figi());
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figies,
                candleInterval,
                DecimalUtils.ZERO,
                StrategyType.CONSERVATIVE,
                Map.of()
        );
        final OffsetDateTime from = DateTimeTestData.newDateTime(2023, 9, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 9, 11);
        final Interval interval = Interval.of(from, to);

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(100));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(share.figi(), interval, candleInterval)).thenReturn(candles);

        final CrossStrategyParams strategyParams = new CrossStrategyParams();
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, new SimpleMovingAverager());

        Assertions.assertNotNull(strategy.initCache(botConfig, interval));
    }

    private static MockedStatic<TrendUtils> mock_TrendUtils_getCrossoverIfLast(Crossover crossover) {
        final MockedStatic<TrendUtils> trendUtilsStaticMock = Mockito.mockStatic(TrendUtils.class, Mockito.CALLS_REAL_METHODS);

        trendUtilsStaticMock.when(() -> TrendUtils.getCrossoverIfLast(Mockito.anyList(), Mockito.anyList(), Mockito.anyInt()))
                .thenReturn(crossover);

        return trendUtilsStaticMock;
    }

}